# Phase 1.2 + 1.3 proposal: provenance and cost columns

**Approved 2026-08-11.** The owner confirmed all three open questions below as recommended:
`unitCost`/`currency` mirror the existing per-unit nutrition-snapshot pattern (not literal resolved
totals); legacy `Measurement`/`ManualDiaryEntry` provenance stays `NULL` where genuinely unknown;
recipe-backed legacy `Measurement` rows are backfilled `sourceKind = 'recipe'` since that is
knowable, not guessed. Implementation: migration v32→33, see the commit that touches
`FoodYouDatabase.kt`. Scope is the migration itself only — PRD 1.2's "subtle visual marker" UI and
capturing `sourceKind` on new writes going forward remain follow-up work, as flagged below.

Combined into one migration per the recommendation in `docs/phase-1.1-proposal.md` (1.2's
`confidence` column already covers 1.1's "flag backfilled entries" need, so there's no reason to
split them).

## A design tension to resolve before approving this

PRD's "Log entry" model (§4) says entries must persist "resolved energy and macronutrients for
that quantity, stored as values on the entry" and "`unit_cost` and `currency`, nullable,
**snapshotted the same way as nutrition**."

I checked how nutrition is actually snapshotted today, and it is **not** stored resolved on the
entry. `FoodDiaryEntry.nutritionFacts` is a computed property
(`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/fooddiary/domain/entity/FoodDiaryEntry.kt:39`):
`food.nutritionFacts * (weight / 100)` — it multiplies the immutable per-100g snapshot on
`DiaryProductEntity`/`DiaryRecipeEntity` by the logged quantity, at read time, every time it's
displayed. It is never written to a column on `MeasurementEntity`.

This still satisfies PRD 1.1's actual safety property — the inputs to that multiplication
(`food.nutritionFacts`, `weight`) are both immutable once written, so the result is deterministic
and can never change when a catalog food is edited later. But it does not satisfy the literal
"stored as values on the entry" wording.

**I'm not proposing to change this.** Storing a pre-multiplied resolved total *in addition to* the
per-unit snapshot would create two sources of truth for the same number with no safety benefit —
if they ever drifted (a rounding difference, a future bug), you'd have a genuinely ambiguous entry.
Given PRD's own preference for extending existing patterns over adding parallel ones, I'm proposing
that `unit_cost`/`currency` follow the **existing** nutrition-snapshot pattern — i.e., land on
`DiaryProductEntity`/`DiaryRecipeEntity` (per-unit, like nutrients) rather than literally on
`MeasurementEntity`/`ManualDiaryEntryEntity` as pre-resolved totals.

**This is a PRD-interpretation call, not a pure technical one — please confirm before I treat this
as approved.** If you want literal resolved-value storage on the entry regardless of the
duplication, say so and I'll redesign this section.

## Proposed columns

### `source_kind` and `confidence` (PRD 1.2)

These are per-log-event attributes with no natural "per-unit" analogue (unlike nutrition/cost), so
they go directly on the entry tables — no ambiguity here.

| Table | Column | Type | Nullable | Backfill |
|---|---|---|---|---|
| `Measurement` | `sourceKind` | TEXT (`barcode`\|`database_search`\|`custom_food`\|`recipe`) | yes | `NULL` — no historical record of which UI action was used; see note below |
| `Measurement` | `confidence` | TEXT (`measured`\|`estimated`) | yes | `NULL` — see note below |
| `Measurement` | `originProductId` | INTEGER, **no FK** | yes | `NULL` — never recorded historically, unrecoverable |
| `Measurement` | `originRecipeId` | INTEGER, **no FK** | yes | `NULL` — same |
| `ManualDiaryEntry` | `sourceKind` | TEXT | **NOT NULL**, default `'manual_estimate'` | fully deterministic — every row in this table is a manual estimate by definition |
| `ManualDiaryEntry` | `confidence` | TEXT | **NOT NULL**, default `'estimated'` | fully deterministic — matches PRD's explicit "manual entries default to estimated" |

`originProductId`/`originRecipeId` intentionally have **no foreign key**, per PRD: "Nothing on a
read path may depend on it resolving." A hard FK would force cascade behavior on catalog deletes
that PRD explicitly doesn't want for this field.

`Recipe`-backed `Measurement` rows (where `recipeId != null`) can deterministically get
`sourceKind = 'recipe'` on backfill — that much *is* knowable from existing data. I left it as
`NULL` in the table above for simplicity but can include this one deterministic backfill case if
you'd like it; it's a one-line addition to the migration (`WHERE recipeId IS NOT NULL`).

**Note on legacy `confidence`/`sourceKind` for existing `Measurement` rows:** it's tempting to
derive `confidence` from `DiaryProductEntity.sourceType` (`User` → `estimated`, else `measured`),
since that data does exist. I'm deliberately **not** proposing this as the default — a
user-sourced catalog product isn't necessarily less accurate than an OFF one, so this derivation
would encode a guess as fact. Recommend leaving legacy rows `NULL` ("unknown provenance") rather
than backfilling a guess. Happy to add the derived backfill if you'd rather have an imperfect
guess than a `NULL`.

### `price_per_unit` / `currency` on the food record (PRD 1.3)

| Table | Column | Type | Nullable |
|---|---|---|---|
| `Product` | `pricePerUnit` | REAL | yes |
| `Product` | `currency` | TEXT (ISO 4217, e.g. `EUR`) | yes |

Only `Product`, not `Recipe` — PRD's Recipe section doesn't mention price columns on recipes
themselves (a recipe's cost is implicitly the sum of its ingredients' prices, a Phase 4 concern).

**Open question, not resolved by this proposal:** PRD 4.1 says "per unit, with the unit matching
how the food is logged" — this is a different model from nutrition, which is always stored
per-100g on `Product` regardless of the unit used at logging time. If `pricePerUnit` should instead
mean "price per gram" (consistent with the nutrition pattern) versus "price in whatever unit this
specific food is normally logged in" (literal PRD reading), the column's semantics and any future
UI built on it will differ. Since PRD 1.3 explicitly says "do not build cost reporting in Phase 1,"
I'm proposing to add the column now and defer resolving this semantic question to whichever phase
actually builds price entry UI (PRD 4.1) — the column itself works either way as a nullable REAL.

### `unit_cost` / `currency` snapshot (PRD 1.3, "snapshotted the same way as nutrition")

| Table | Column | Type | Nullable |
|---|---|---|---|
| `DiaryProduct` | `unitCost` | REAL | yes |
| `DiaryProduct` | `currency` | TEXT | yes |
| `DiaryRecipe` | `unitCost` | REAL | yes |
| `DiaryRecipe` | `currency` | TEXT | yes |
| `ManualDiaryEntry` | `unitCost` | REAL | yes |
| `ManualDiaryEntry` | `currency` | TEXT | yes |

All `NULL` on backfill (no prices exist yet). `ManualDiaryEntry` gets its own columns directly
(unlike products/recipes, it has no separate "diary copy" table to snapshot onto — it's already a
flat table).

## Migration sketch (not applied)

One manual `Migration` object (structural changes are simple additive `ALTER TABLE`s, but the
`ManualDiaryEntry` backfill needs a data-writing `UPDATE`, so this can't be a pure `AutoMigration`):

```sql
-- Measurement: provenance, all nullable, no backfill possible
ALTER TABLE Measurement ADD COLUMN sourceKind TEXT DEFAULT NULL;
ALTER TABLE Measurement ADD COLUMN confidence TEXT DEFAULT NULL;
ALTER TABLE Measurement ADD COLUMN originProductId INTEGER DEFAULT NULL;
ALTER TABLE Measurement ADD COLUMN originRecipeId INTEGER DEFAULT NULL;

-- ManualDiaryEntry: provenance, deterministic backfill + cost columns
ALTER TABLE ManualDiaryEntry ADD COLUMN sourceKind TEXT NOT NULL DEFAULT 'manual_estimate';
ALTER TABLE ManualDiaryEntry ADD COLUMN confidence TEXT NOT NULL DEFAULT 'estimated';
ALTER TABLE ManualDiaryEntry ADD COLUMN unitCost REAL DEFAULT NULL;
ALTER TABLE ManualDiaryEntry ADD COLUMN currency TEXT DEFAULT NULL;

-- Product: price on the food record
ALTER TABLE Product ADD COLUMN pricePerUnit REAL DEFAULT NULL;
ALTER TABLE Product ADD COLUMN currency TEXT DEFAULT NULL;

-- DiaryProduct / DiaryRecipe: cost snapshot, mirroring the nutrition pattern
ALTER TABLE DiaryProduct ADD COLUMN unitCost REAL DEFAULT NULL;
ALTER TABLE DiaryProduct ADD COLUMN currency TEXT DEFAULT NULL;
ALTER TABLE DiaryRecipe ADD COLUMN unitCost REAL DEFAULT NULL;
ALTER TABLE DiaryRecipe ADD COLUMN currency TEXT DEFAULT NULL;
```

Every statement is a plain additive `ALTER TABLE ADD COLUMN` with a constant default — no temp-table
rebuild needed (unlike the historical migrations that changed column meaning or dropped columns).

## Rollback path

Purely additive, non-destructive — no data is deleted, moved, or reinterpreted for any existing
row. A rollback (if ever needed) means dropping the added columns; SQLite's `DROP COLUMN` support
depends on the bundled SQLite version, so the safer down-migration would rebuild each affected
table without the new columns, following the temp-table pattern already used in
`LegacyMigrations.kt` (`ProductEntity_temp`, etc.), rather than relying on `DROP COLUMN`.

Per PRD §4's migration policy ("No destructive migration without an automatic pre-migration
export"): this migration is not destructive, so that specific rule doesn't block it. It's still
worth sequencing PRD 1.4 (export) before this if you want a safety net regardless — your call.

## What existing data is affected

No existing values are changed or deleted. Every new column is additive with a constant default.
The only rows that get a *meaningful* (non-`NULL`) backfilled value are `ManualDiaryEntry` rows,
which deterministically become `sourceKind = 'manual_estimate'`, `confidence = 'estimated'` —
accurately describing what they already are.

## Fixture test plan

Following the existing `Abstract*MigrationTest` pattern
(`app/src/commonTest/kotlin/.../migration/`, platform-`actual` implementations under
`app/src/androidInstrumentedTest/`): load a v32 fixture database, run the new migration, assert:
- row counts unchanged across all seven affected tables
- every pre-existing `ManualDiaryEntry` row has `sourceKind = 'manual_estimate'` and
  `confidence = 'estimated'`
- every pre-existing `Measurement`/`Product`/`DiaryProduct`/`DiaryRecipe` row has `NULL` in all new
  columns
- a sample of pre-existing nutrient/quantity values on all seven tables are byte-for-byte unchanged

## Not covered by this proposal

- UI surfacing of `confidence` (the "subtle visual marker" PRD 1.2 asks for) — application code,
  not a schema question, would follow once these columns exist
- Populating `sourceKind` meaningfully for *new* `Measurement` rows going forward (distinguishing
  barcode vs. database-search vs. custom-food at the moment of logging) requires plumbing the
  actual UI entry point through to `RoomFoodDiaryEntryRepository.insert()` — application code, out
  of scope for a schema proposal, but flagging so it isn't assumed to be "free" once the column exists
- Any cost reporting or price-entry UI (explicitly deferred to Phase 4 by the PRD)

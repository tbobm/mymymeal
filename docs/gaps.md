# Feature gaps against PRD §5

For each PRD §5 feature: exists, partial, or absent, with file references. Data-model specifics
(entities, columns, migration history) live in `docs/schema.md` — this document doesn't repeat
them, only cross-references. Licence/OFF specifics live in `docs/licence.md` and are summarized
here only where they affect a §5 feature directly.

## Phase 1: correctness of the foundation

**1.1 Entry snapshotting — EXISTS.**
Diary entries already snapshot nutrition at write time into `DiaryProductEntity`/`DiaryRecipeEntity`
copies with no FK back to the catalog. See `docs/schema.md` §"The load-bearing finding." No work
needed for the snapshot mechanism itself.

**1.2 Provenance (`source_kind`, `confidence`) — ABSENT, with a coincidental partial.**
No `source_kind` or `confidence` column exists on `MeasurementEntity` or `ManualDiaryEntryEntity`
(`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/fooddiary/infrastructure/room/`). Quick Add
entries (`ManualDiaryEntryEntity`) do get a lightning-bolt badge in the diary UI
(`app/ui/home/meals/card/`, confirmed on-device, `docs/tap-audit-screenshots/04-quickadd-logged.png`)
— visually this satisfies PRD 1.2's "subtle visual marker" ask, but it marks "was this a Quick Add"
rather than a real `confidence` field, and nothing marks barcode vs. search vs. custom-food
provenance at all.

**1.3 Cost columns — ABSENT.**
No `price_per_unit`/`currency` on `ProductEntity`, no `unit_cost`/`currency` on `MeasurementEntity`
or `ManualDiaryEntryEntity`. Confirmed by full read of both entity files (`docs/schema.md`).

**1.4 Export — PARTIAL.**
`ExportCsvProductsUseCase`
(`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/importexport/domain/usecase/ExportCsvProductsUseCase.kt`)
exports the food catalog (`Product` table) to CSV with a configurable field list. There is:
- no export of diary entry history (`Measurement`, `ManualDiaryEntry`)
- no export of recipes
- no weight log to export (see 3.2 below — the entity doesn't exist)
- no zip bundling of multiple CSVs into one action

The existing exporter is a reasonable pattern to extend (same module, same CSV-writer utility)
rather than a parallel mechanism to add.

## Phase 2: migration off MyFitnessPal

**2.1 Importer — ABSENT.** No MFP-specific import code found anywhere in the tree.

Separately: the MFP export data available in this repo
(`File-Export-2025-06-08-to-2026-08-10/`) is the **free-tier daily-summary export**
(`Nutrition-Summary-*.csv`: one row per day+meal with aggregate macros, no per-food-item detail;
`Measurement-Summary-*.csv`: date/body-fat/weight; `Exercise-Summary-*.csv`: workouts) — **not**
the Premium per-food-item export with barcode-level detail that PRD §2.1 describes ("per-meal
macros and timestamps" at the food level, fuzzy-matchable against Open Food Facts). If the owner's
actual MFP account only produces this free-tier format, PRD 2.1's matching strategy (exact/fuzzy
name match against individual foods) has no per-food rows to match against — only pre-aggregated
daily/meal totals. This changes the shape of the importer needed and should be confirmed with the
owner before Phase 2 design.

**2.2 Review screen — ABSENT.**

## Phase 3: the reason this fork exists

**3.1 Rolling weekly budget — ABSENT.**
`WeeklyGoals` exists (`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/goals/domain/entity/WeeklyGoals.kt`)
but is a **different concept**: a fixed daily calorie/macro target *per weekday* (e.g. a different
Monday target than Saturday), with no rolling 7-day balance, no carryover, no cumulative
surplus/deficit tracking. This is a false-friend name match — worth flagging explicitly so a future
session doesn't assume 3.1 is already done.

**3.2 Expenditure from observed data — ABSENT, and blocked.**
No regression/estimation logic exists, and there is **no weight log entity at all** in the schema
(confirmed: zero matches for `Weight`/`BodyWeight`/`BodyMeasurement` across `app/src/commonMain`).
PRD's own "Weight log" data model requirement (§4, "Required by Phase 3") is therefore also absent
— this blocks 3.2 entirely until a weight-log entity is designed and gated per PRD §2. The MFP
`Measurement-Summary` CSV (date, body fat %, weight) is exactly the data 3.2 needs and has nowhere
to land today.

**3.3 Meal templates and fast re-log — PARTIAL, and measured short of the target.**
`MeasurementSuggestionEntity`/`LatestMeasurementSuggestion` (schema.md) already record the
most-recently-used quantity per food and pre-fill it on `AddEntryScreen`. The diary search screen
already has "Recent" and "Your food" tabs. But there is no *named template* (a saved combination of
multiple entries logged in one action), and the on-device tap audit
(`docs/tap-audit.md`) measured **3 taps** to re-log a previously-logged food from the home screen —
one more than the PRD's 2-tap acceptance target. Closing this gap is 3.3's actual job.

**3.4 Calendar view — mostly ABSENT.**
`CalendarCard` (`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/app/ui/home/calendar/CalendarCard.kt`)
on the home screen is a horizontal day-strip plus a standard Material `DatePickerDialog` for
jumping to an arbitrary date — a day-navigation control, not PRD 3.4's month grid colored by
position against target, and it has no long-press-for-summary interaction.

**3.5 Categorisation (tags) — ABSENT.** No tag entity, column, or UI found.

**3.6 Weekly report — ABSENT.** No report screen or export found.

## Phase 4: cost tracking

**All of 4.1–4.4 — ABSENT.** Depends entirely on 1.3's cost columns, which don't exist yet.

## Phase 5: distribution

**5.1 Licence obligations — N/A yet, addressed in `docs/licence.md`.**
Nothing is currently owed (no distribution has happened); the obligations and their trigger point
are documented so they aren't missed later.

**5.2 Google limited distribution account — N/A, not started.** No code dependency; a registration/process task for the owner when Phase 5 begins.

**5.3 First-run flow — PARTIAL.**
A first-run onboarding flow does exist (`docs/tap-audit-screenshots/01-home-empty.png` shows the
"Before you start" privacy screen encountered on this session's fresh install), followed by a
"Food database" step offering Open Food Facts / USDA FoodData Central / Swiss Food Composition
Database as **opt-in external services** — this is opt-in-to-live-API, not "seed a filtered OFF
subset into local SQLite with FTS5" as PRD §6 specifies (no bundled/downloaded local mirror step
was observed). No "choose targets" step was seen in onboarding itself (goals are presumably
configured separately in Settings — not verified in this session). No MFP import step exists in
onboarding (matches 2.1's absence).

**5.4 Local crash log capture — mostly EXISTS.**
`CrashReportScreen` (`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/app/ui/crash/CrashReportScreen.kt`)
and `CrashReportActivity` (`app/src/androidMain/kotlin/dev/tbobm/mymymeal/app/app/infrastructure/android/CrashReportActivity.kt`)
already implement exactly PRD 5.4's shape: shows the crash message locally, one explicit
user-initiated action ("Copy and open bug report" — copies the report to clipboard and opens an
issue-tracker URL), **no automatic upload**. Two small fork-specific gaps: it targets the upstream
GitHub issue tracker URL (should point at `tbobm/FoodYou` or be reconfigured for local-only use
before Phase 5), and it only shows the crash that just happened, not a persisted historical log
across sessions (PRD says "capture," which could mean either — worth clarifying with the owner
whether history is actually needed for a single-owner app that will see the crash immediately).

**5.5 `MAINTENANCE.md` — ABSENT.** To be authored in Phase 5, per PRD §5.5's own list of required contents.

# Health Connect nutrition export

> GitHub issues are disabled on `tbobm/mymymeal`; this doc is the issue-equivalent scope record,
> following the repo convention (cf. `docs/phase-2.1-proposal.md`, `docs/phase-3-diary-widget-proposal.md`).

## Summary

Write mymymeal's logged nutrition (energy, protein, carbohydrate, fat) to Android's Health
Connect, so other apps that read Health Connect (Fitbit's own app among them) can see it.
Write-only: nothing is read back from Health Connect. This is a deviation from PRD scope (PRD §5
doesn't list it, and PRD §2 says "do not add features not listed here") — it is built anyway
because the owner asked for it directly.

## Why Health Connect and not Google Fit or the Fitbit API

- Google Fit's write APIs (`com.google.android.gms.fitness`) are deprecated; there is no supported
  nutrition write path through them any more.
- The Fitbit Web API's food-log endpoints are on a hard cutoff (September 2026) with new developer
  registrations already closed — not usable for a new integration.
- Health Connect (`androidx.health.connect:connect-client`) is the only remaining supported route
  for writing nutrition data into the Android health ecosystem.

**Known limitation, stated up front:** Fitbit is
[community-reported](https://community.fitbit.com/t5/Third-Party-Integrations/Fitbit-Health-Connect-Nutrition-and-Calories/td-p/5722203?nobounce)
not to read nutrition from Health Connect today — it reads activity, but shows its own separate
food log. This work makes the data correctly available and verifiable in Health Connect's own data
browser; whether Fitbit's "Food and drink" card ever reflects it is outside this app's control.

## Dependency gate (PRD §2)

| | |
|---|---|
| Proposed change | `androidx.health.connect:connect-client:1.1.0`, `androidMain` only. |
| Why | Only supported API for writing nutrition to the Android health ecosystem. |
| Rollback | Remove the version-catalog entry, the `androidMain` dependency, the manifest permission block, and the `healthconnect` packages. Nothing else depends on it. |
| Data affected | None. No Room entity, DAO, or migration change (see below). |

No schema gate is triggered — deliberate, see below.

## Design

One `NutritionRecord` per (date, meal), not per diary entry — this matches how MyFitnessPal syncs
to Health Connect (its own docs describe posting "a meal summary, not individual foods"). Diary
entries carry no time-of-day of their own (`Measurement`/`ManualDiaryEntry` store `epochDay` plus a
`createdAt` write timestamp, not a consumption time); `Meal.from`/`Meal.to` is the only real
time-of-day source, so the meal's window becomes the record's start/end instants.

Each record's `clientRecordId` is derived as `meal:<mealId>:<epochDay>` — stable and
recomputable, so:

- re-syncing a day **upserts** rather than duplicates,
- a meal that becomes empty is deleted by that same id,
- **no Health Connect record id is stored in mymymeal's own database** — no new column, no Room
  migration, no PRD §2 schema gate.

`WRITE_NUTRITION` alone is sufficient to insert, update, and delete records this app itself wrote
(Health Connect grants an app read/update/delete over its own previously-written data without also
requiring the read permission).

Turning sync off leaves already-written Health Connect records in place; they're the user's own
data, and Health Connect's own UI can remove them.

Mymymeal's meals are user-definable; Health Connect's meal type is a fixed enum. The mapper matches
the default names (`Breakfast`/`Lunch`/`Dinner`/`Snack`) case-insensitively and falls back to
`MEAL_TYPE_UNKNOWN` for anything else — the same degradation MyFitnessPal applies to custom meal
headings. The meal's real name still goes in the record's `name` field.

## Sync triggers

1. **Live, today only** — a process-lifetime coroutine (registered `createdAtStart` in Koin, no
   WorkManager dependency) collects the diary for today and re-syncs on every change, while the
   sync-enabled preference is on.
2. **On-demand backfill** — a "Sync last 30 days" button in Settings → Database → Health Connect.

## Files

- `gradle/libs.versions.toml`, `app/build.gradle.kts` — dependency.
- `app/src/androidMain/AndroidManifest.xml` — `WRITE_NUTRITION` permission, the permissions-
  rationale intent filter on `MainActivity` (points at the main app screen — this is a
  single-user personal app with no separate policy page to show), the `activity-alias` Health
  Connect needs on API 34+, and a `<queries>` entry for the Health Connect package.
- `fooddiary/domain/healthexport/` (commonMain) — `NutritionExportRecord` + `DiaryMeal.toExportRecord()`
  mapping (pure, unit-tested without the Health Connect dependency) and `HealthConnectPreferences`
  (feature-scoped DataStore preference, same shape as `goals/domain/entity/RollingBudgetPreferences`).
- `fooddiary/infrastructure/healthexport/` (commonMain) — the DataStore-backed preferences repository.
- `app/infrastructure/android/healthconnect/` (androidMain) — `HealthConnectAvailability`
  (SDK/permission status), `HealthConnectNutritionWriter` (maps to `NutritionRecord`, inserts/
  deletes), `HealthConnectSyncService` (the two sync triggers above).
- `app/ui/database/healthconnect/` (commonMain expect + androidMain actual) — settings screen,
  ViewModel, Koin module. Entry point is a new row on the Database settings screen.

## Verification

1. `./gradlew --offline assembleDebug` and `./gradlew --offline test` — the commonTest mapper
   test covers a normal meal window, a midnight-crossing window (asserting the end instant
   deliberately lands on the next day), an all-day window, an empty meal, clientRecordId
   stability, and meal-type mapping.
2. On-device (`mymymeal_api34`, `rtk proxy` for adb): enable sync, grant the Health Connect
   permission, log a test meal.
3. Open Health Connect → Data and access → Nutrition and confirm the record's meal name, time, and
   **energy/macro numbers match the in-app Goals card for the same day exactly** (catches a
   kcal/kJ or gram-unit mixup that "a record appears" alone would not).
4. Edit the entry and re-check the record updates rather than duplicating; delete all entries in a
   meal and confirm the record disappears.
5. Press "Sync last 30 days" and confirm past days populate.
6. Informational only: check Fitbit's own "Food and drink" card. If it stays empty, that's the
   known Fitbit-side limitation above, not a bug in this work.

**On-device results (`mymymeal_api34`, 2026-09-18):** steps 1–5 above all passed, confirmed by
querying Health Connect's own `nutrition_record_table` directly. This caught one real bug before
merge: the first pass used `Metadata.manualEntryWithId(id = ...)`, which sets the record's own
UUID, not `clientRecordId` — the column stayed empty and every re-sync would have inserted a
duplicate. Fixed to `Metadata.manualEntry(clientRecordId = ...)`; re-verified upsert (re-syncing
today left exactly one row, same id), backfill (a second historical day synced correctly), and
delete (removing the diary entry removed only that day's Health Connect record). Step 6 (Fitbit's
own UI) was not exercised on the emulator — no Fitbit account is configured there — and per the
known limitation above is not expected to show anything even if it were.

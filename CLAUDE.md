Read PRD.md before any work. It is the source of truth for scope, data model rules, and gates.

# Module map — Food You fork (mymymeal)

Fork of [maksimowiczm/FoodYou](https://github.com/maksimowiczm/FoodYou) (GPL-3.0, Kotlin/Compose
Multiplatform, Android-only build currently). `origin` = `tbobm/mymymeal` (this project's fork,
renamed from `tbobm/FoodYou`), `upstream` = `maksimowiczm/FoodYou`. App ID: `dev.tbobm.mymymeal.app`
(rebranded from `com.maksimowiczm.foodyou`).

Gradle modules (declared in `settings.gradle.kts`): `:app`, `:shared:barcodescanner`,
`:shared:resources`. Per upstream decision log
(`docs/development/decision-log/0002-minimize-gradle-modules.md`), the module count is deliberately
minimized — almost everything lives in `:app`, organized by package instead of by Gradle module.

## `:app` — everything

Kotlin Multiplatform source sets: `commonMain` (nearly all logic and UI), `androidMain` (Android
glue), `commonTest`, `androidInstrumentedTest`. Packages under
`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/`:

| Package | Purpose | This project touches it? |
|---|---|---|
| `app` | App shell: navigation graphs, DI wiring, Room `FoodYouDatabase` definition, top-level UI (home screen, database management, backup/restore UI). | **Yes** — nav host, home screen (diary), database module, backup/export flows (PRD 1.4, 2, gates for schema). |
| `fooddiary` | The food diary: `Meal`, `Measurement` (the actual log entry), `ManualDiaryEntry`, diary-local snapshot copies (`DiaryProduct`, `DiaryRecipe`, `DiaryRecipeIngredient`). Domain/infrastructure split (`domain/entity`, `domain/repository`, `domain/usecase`, `infrastructure/room`, `infrastructure/repository`). | **Yes** — core of PRD Phase 1 (snapshotting, provenance) and Phase 3 (templates, weekly budget). |
| `food` | Food catalog: `Product`, `Recipe`, `RecipeIngredient`, food search (`food/search/...`, FTS, Open Food Facts / USDA paging keys), food events, measurement suggestions (recently-used amounts, i.e. existing fast-re-log infra). | **Yes** — cost columns (PRD 1.3), price entry (Phase 4), extends existing OFF/USDA mirror (PRD §6) rather than adding a parallel one. |
| `goals` | Daily nutrition targets. | Read for Phase 3 rolling-budget work; do not touch coaching logic (non-goal). |
| `settings` | App settings, including `HomeCard` ordering (which cards appear on the modular home screen). | Minor — settings entries for new features (e.g. weekly-budget config) land here. |
| `importexport` | Existing import/export mechanism. | **Yes** — PRD 1.4 export and Phase 2 MFP importer extend this rather than adding a parallel pipeline. |
| `sponsorship` | Ko-fi/BTC/ETH sponsor list display. Unrelated to nutrition tracking. | No. |
| `poll` | In-app user polls (upstream feature-request mechanism). | No. |
| `changelog` | In-app changelog viewer. | No. |
| `theme` | Material You theming. | No. |
| `common` | Cross-cutting: `Nutrients`/`Vitamins`/`Minerals` value classes, `MeasurementType`, `FoodSourceType`, transaction plumbing, Room converters. | **Yes** — shared value classes touched by cost/provenance columns. |

`app/schemas/` holds Room's exported JSON schema for every version 1–32 (see `docs/schema.md`).
`app/src/androidMain` has the Android manifest, activities, and platform-specific DI.

## `shared:barcodescanner`

Standalone module isolated from the KMP/Compose-first codebase because it uses the older Android
View/XML layout system (per decision log 0002). Wraps barcode (EAN-13/UPC) scanning — the existing
scanner PRD §1 constraint 4 requires reusing. **Touch**: only if the scan flow needs wiring changes;
do not reimplement scanning.

## `shared:resources`

Static resources module: strings, images, fonts (Compose Multiplatform resources). **Touch**:
routine, whenever new UI needs strings/assets (e.g. new "estimated" marker icon, PRD 1.2).

## `dev/`

Not a Gradle module — maintainer shell scripts for string-table housekeeping
(`find-unused-strings.sh`, `fix-strings.sh`, `test-meals-localization.bash`). Not part of the app.

## `docs/` — upstream's public documentation site

Zensical-based static site (`docs/zensical.toml`), **not** related to this project's discovery
deliverables. Contains `docs/docs/*.md` (site pages: index, contribute, privacy policy) and
`docs/development/decision-log/*.md` (upstream's own architecture decisions — useful background,
e.g. `0003-create-local-mirror-for-external-food-databases.md` explains the existing OFF/USDA local
mirror PRD §6 says to extend). This project's PRD-mandated deliverables (`schema.md`, `licence.md`,
`tap-audit.md`, `gaps.md`) are placed directly under `docs/` per the PRD's literal paths; they are
not part of the zensical `nav` and will not appear on the built doc site.

## Navigation graph and where the diary is composed

Entry point: `app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/app/navigation/FoodYouAppNavHost.kt`
— a single `NavHost` with `Home` (private, `@Serializable object`) as `startDestination`, plus
routes for `Settings`, `About`, `Language`, `ThemeSettings`, `Sponsor`, and others. A second, small
nested nav host (`DownloadProductAppNavHost.kt`) handles the "create product from URL" dialog flow.

**The diary screen** is `HomeScreen`
(`app/src/commonMain/kotlin/dev/tbobm/mymymeal/app/app/ui/home/master/HomeScreen.kt`). It is a
"modular home screen" (per README) assembled from cards, ordered by user preference
(`settings.domain.entity.HomeCard`):

- `MealsCards` (`app/ui/home/meals/card/MealsCards.kt`) — per-meal diary entries for the selected
  day. Exposes `onMealCardAddClick` (open food search) and `onMealCardQuickAddClick` (re-log
  without searching — existing fast-relog infrastructure, relevant to PRD 3.3).
- `GoalsCard` — daily targets vs. logged totals.
- `CalendarCard` — small calendar affordance (relevant to PRD 3.4, may already partially exist).
- `PollsCard` — unrelated in-app polls.

Editing a diary entry routes through `onEditDiaryEntryClick(foodEntryId, manualEntryId)` —
note the two separate ID spaces, confirming `Measurement` (food-backed entries) and
`ManualDiaryEntry` (manual estimates) are distinct tables/flows (see `docs/schema.md`).

## Toolchain notes for this project

- Gradle 8.13, AGP 8.13.2, Kotlin 2.3.10, compileSdk/targetSdk 36, minSdk 28.
- Build JDK: use Homebrew `openjdk@21` (`JAVA_HOME=/opt/homebrew/opt/openjdk@21`) — Gradle 8.13
  cannot run on JDK 25 (e.g. Android Studio's bundled JBR). Do not use the Studio JBR for CLI builds.
- Android SDK: `/opt/homebrew/share/android-commandlinetools` (`ANDROID_HOME`), platform android-34
  installed; project needs compileSdk 36 — install `platforms;android-36` if a build step fails
  needing it.
- First build must be online to populate the Gradle cache (`kotlin-test` and other test-only deps
  are not pulled by `assembleDebug` alone); the PRD's `./gradlew --offline assembleDebug test` loop
  works once the cache is warm.
- AVDs available: `mymymeal_api34`, `oneplus_12_api34` (neither running by default).

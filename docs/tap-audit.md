# Tap audit

Measured on-device (emulator `mymymeal_api34`, API 34, debug build, schema v32) on a fresh install
with no seeded Open Food Facts / USDA data (opted out at first-run onboarding). Tap counts start
from the diary home screen (`HomeScreen`, the app's launch destination after onboarding), per PRD
§3. Screenshots for each flow are in `docs/tap-audit-screenshots/`.

**Method note.** Coordinates were verified against `uiautomator dump` bounds, not eyeballed from
screenshots — several early attempts missed because the visually-estimated coordinate for a FAB or
icon didn't match its actual hit-test bounds. All tap counts below are from taps that produced a
confirmed state change (screenshot evidence).

## Summary

| Flow | Taps (from home) | PRD §1.6 target | Meets target? |
|---|---|---|---|
| Log a previously logged food (product) | **3** | ≤2 | **No** |
| Log a saved recipe | **3** (inferred, same UI path) | ≤2 | **No** |
| Manual entry with estimated calories | **2** | not covered by §1.6 | n/a |
| Log a barcode-scanned product | **2 to reach scanner**, then physical scan + **1** to confirm = **3+scan** | not covered by §1.6 (§1.6 only names "previously logged") | n/a, but same 3-tap confirm pattern |

The PRD's two-tap acceptance criterion (§1.6, re-measured for §3.3) applies specifically to
**"log a previously eaten meal."** That flow currently takes **3 taps**, not 2. This is the
concrete gap PRD 3.3 (meal templates / fast re-log surfaced on the diary screen) exists to close.

## 1. Log a previously logged food (product)

Precondition: a product ("TestApple") had already been logged once via search-and-create.

1. **Tap** the meal card's **"+"** button on `HomeScreen` → opens `DiaryFoodSearchScreen` for that meal/date, showing the product under both "Recent" and "Your food" tabs with zero further input.
2. **Tap** the product's list entry → navigates to `AddEntryScreen`, pre-filled with today's date, the originating meal, and the last-used quantity (100 g).
3. **Tap** the "Save" FAB → entry is written; toast "Got it! We've added your measurement"; returns to the search screen.

**3 taps.** No search text entry was needed (the item was already visible), so this is the
best case for this flow today. Screenshots: `05-relog-search-recent.png` (state after tap 1),
`06-relog-confirm-screen.png` (state after tap 2), `07-relog-done.png` (state after tap 3).

There is no shortcut that logs a previously-eaten food in fewer taps. The meal card's lightning-bolt
icon — the only other affordance on `HomeScreen` — opens **Quick Add** (a manual-estimate form, see
below), not a "repeat last entry" action. `MeasurementSuggestionEntity`/`LatestMeasurementSuggestion`
exist in the schema (recording the last-used quantity per food) but are only consumed to pre-fill
the quantity field on `AddEntryScreen`, not to power a one-tap re-log button.

## 2. Log a saved recipe

Not independently re-measured end-to-end (recipe creation was interrupted by a keyboard-dismiss
navigation quirk during this session — see note below) but the search screen (`DiaryFoodSearchScreen`)
is shared and food-type-agnostic: a saved recipe appears in the same "Recent"/"Your food" lists as a
product and is selected and confirmed through the identical `AddEntryScreen` → Save path. **Expected
3 taps**, by the same mechanism as flow 1. No feature exists that would make a recipe's re-log path
shorter than a product's.

## 3. Log a manual entry with estimated calories ("Quick Add")

1. **Tap** the meal card's **lightning-bolt icon** on `HomeScreen` → opens `CreateQuickAddScreen` ("Quick Add lets you log without searching for specific foods. It's a fast way to record meals when you already know the calorie count or want a simple estimate.").
2. Enter a name (required) and at least one nutrient/energy value (required to save) — text entry, not a tap.
3. **Tap** the Save icon (top app bar) → entry written as a `ManualDiaryEntryEntity`; appears in the diary with a lightning-bolt badge next to its name.

**2 taps** (plus mandatory text entry). Screenshots: `03-quickadd-form.png`, `04-quickadd-logged.png`.
The lightning-bolt badge on the logged entry is a genuine, if coincidental, partial implementation
of PRD 1.2's "surface `estimated` entries with a subtle visual marker" — see `docs/gaps.md`.

## 4. Log a barcode-scanned product

1. **Tap** the meal card's "+" button → search screen.
2. **Tap** the barcode icon inside the search bar → camera permission prompt, then live scanner (`08-barcode-scanner-launch.png`).

**2 taps to reach the scanner.** Decoding an actual barcode was not exercised: the emulator has no
real camera feed and no physical/virtual EAN-13 barcode was available to present to it. Based on
the code path (barcode decode routes to the same `Create product` → `AddEntryScreen` → Save flow
observed for manual product creation, pre-filling the barcode field), a successful scan of an
**already-known** barcode would add **1 more tap** (confirm/Save) on top of the 2 to reach the
scanner — consistent with the 3-tap pattern of flows 1–2. A scan of an **unknown** barcode instead
requires filling the full "Create product" form (many required fields), then Save — much more
than 3 taps, as observed when creating "TestApple" in this session.

## Observations feeding into other Phase 0 deliverables

- **No pre-shipped Open Food Facts/USDA subset**: onboarding presents OFF/USDA/Swiss DB as opt-in
  external services, not a bundled local mirror (see `docs/gaps.md`, PRD §6).
- **Home screen already has**: a calendar/day-strip (partial PRD 3.4), per-meal quick-add lightning
  icon, user-visible meal slots including a custom "Snacks" (all-day) slot.
- **UI automation gotcha worth recording**: Compose's `uiautomator` accessibility tree bounds do not
  always match visual screenshot pixel estimates (a scale-factor eyeball guess was off by ~600px
  vertically in one case). Always cross-check tap coordinates against `adb shell uiautomator dump`
  bounds rather than estimating from a downscaled screenshot.

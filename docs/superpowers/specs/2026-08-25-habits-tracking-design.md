# Habits tracking: coffee + supplements + reminders

Date: 2026-08-25
Status: approved, not yet planned/implemented

## Context

The owner asked to extend mymymeal (this fork) to also cover:

- Coffee intake
- Supplement ("complements") intake: free add, daily tracking, optional twice-daily
  reminders

This is new scope: it is not covered by `PRD.md` Phases 1–4, and the app currently has
zero notification/scheduling infrastructure. Investigation during brainstorming found
that coffee tracking is largely already supported (products have a `caffeine` nutrient
field with a daily goal already tracked in `GoalsScreen`), which changed the shape of
the coffee half of this work from "new feature" to "small UI addition on top of existing
data."

This spec covers three logically separate pieces, designed together because they share
one UI surface (a new home card) but are otherwise independent:

1. A coffee quick-log + summary, built entirely on existing diary/nutrient data.
2. A supplement adherence checklist (new, small domain).
3. A reminder/notification subsystem (new, using WorkManager).

## Decisions from brainstorming

- Coffee: dedicated widget showing **both** cup count and caffeine mg for today.
  Logging is **one-tap using a pre-configured default coffee** (food + measurement),
  not a trip through food search each time.
- Supplements: **adherence only** (taken / not taken per day), not dose/amount logging.
  Managed **inline on the card** (add via dialog, remove via a trailing delete icon) —
  no separate settings screen.
- Reminders: **two shared times** for everything (not per-supplement), **configurable
  with sensible defaults**. If everything is already taken when a reminder time hits,
  **skip silently** — no notification. Tapping a reminder notification **opens Home**
  (where the Habits card lives — see UI placement below; this supersedes an earlier,
  now-inapplicable answer of "open the supplement screen," since no separate screen
  exists).
- UI placement: **one combined "Habits" home card** holding both the coffee row and
  the supplement checklist, rather than two separate cards or a dedicated screen.
- Scheduling approach: **WorkManager**, chosen over the initially-recommended
  `AlarmManager` + `NotificationManager` (which would have needed no new dependency).
  The owner explicitly chose WorkManager for its built-in reboot-survival handling.

## Data model

### Coffee — no new persistent entity

Coffee reuses existing infrastructure entirely:

- **Caffeine mg (today):** the existing per-day caffeine aggregate already computed for
  `GoalsCard` (`nutritionFacts.caffeine` summed across today's diary entries). Do not
  recompute this separately — read the same aggregate so the two views can never
  disagree.
- **Cup count (today):** count of today's `FoodDiaryEntry` rows whose `foodId` equals
  the configured default-coffee food.
- **New preference only:** which food + measurement is "the" default coffee. Added to a
  new `HabitsPreferences` value (below), not a new table.

### Supplements — new `habits` package

Mirrors the existing `goals` package's domain/infrastructure split.

```kotlin
// domain/entity
data class Supplement(val id: Long, val name: String, val sortOrder: Int)

// domain/entity — presence of a row means "taken that day"; no dose, no timestamp
data class SupplementIntake(val supplementId: Long, val date: LocalDate)
```

Room tables (new, migration version 34 → 35):

- `Supplement(id, name, sortOrder)`
- `SupplementIntake(supplementId, date)` — composite primary key `(supplementId,
  date)`; toggling a checkbox inserts or deletes this row. Foreign key to `Supplement`
  with `onDelete = CASCADE` so removing a supplement drops its history.

Per the project's migration convention: bump `FoodYouDatabase.VERSION` to 35, export the
new schema JSON, write a `Migration(34, 35)`, and add a fixture test for it (see
`docs/schema.md` for the existing pattern to follow).

### Reminders + coffee default — one preferences value

```kotlin
data class HabitsPreferences(
    val remindersEnabled: Boolean = false,
    val morningTime: LocalTime = LocalTime(8, 0),
    val eveningTime: LocalTime = LocalTime(20, 0),
    val defaultCoffeeFoodId: FoodId? = null,
    val defaultCoffeeMeasurement: Measurement? = null,
)
```

Stored via the existing `UserPreferencesRepository<T>` pattern (same mechanism as
`MealsPreferences` / rolling-budget preferences) — DataStore-backed, no new persistence
mechanism introduced.

## UI

One new `HomeCard.Habits` entry in the existing `HomeCard` enum, orderable and
toggleable through the existing home-personalization settings exactly like
`Calendar`/`Goals`/`Meals`.

**Coffee row:** cup count and caffeine mg for today, with a "+" button.
- If a default coffee is already configured: tapping "+" calls
  `CreateFoodDiaryEntryUseCase` directly with the fixed food + measurement — one tap,
  no navigation (same use case the removed meal-card quick-relog row used, just fed a
  fixed value instead of a user-picked recent food).
- If no default coffee is configured yet: tapping "+" opens food search once; the food
  and measurement picked there becomes the new default, saved to `HabitsPreferences`.

**Supplement checklist:** one row per `Supplement`, each a checkbox (checked iff a
`SupplementIntake` row exists for today) + name + a small trailing delete icon, always
visible. Toggling the checkbox inserts/deletes today's `SupplementIntake` row directly —
no confirmation. Deleting a supplement (the trailing icon) shows the same style of
confirmation `AlertDialog` `MealCard.kt` already uses for deleting a diary entry. A
"+ Add supplement" row at the bottom opens a small text-input dialog to name a new one.

**Reminder settings:** a small settings/gear icon on the card opens a dialog with: an
on/off toggle, a morning time picker, an evening time picker. Turning the toggle on
triggers the Android 13+ `POST_NOTIFICATIONS` runtime permission request right there
(not at app launch, since reminders are opt-in). If the permission is denied, the
toggle reverts to off and `remindersEnabled` stays `false` — no silently-broken "on"
state where the preference says on but no notification can ever fire.

## Reminder scheduling (WorkManager)

Two independent `PeriodicWorkRequest`s, unique work names `"habits-reminder-morning"`
and `"habits-reminder-evening"`, each with a 24-hour period and an initial delay
computed to the next occurrence of the configured `LocalTime`. Enqueued with
`enqueueUniquePeriodicWork(..., ExistingPeriodicWorkPolicy.UPDATE, ...)` so changing a
time or re-enabling reminders simply re-enqueues with a fresh initial delay. Both are
cancelled by name when `remindersEnabled` is turned off. WorkManager persists work
requests across reboots on its own — no boot `BroadcastReceiver` needed.

This is a new Gradle dependency (`androidx.work:work-runtime-ktx`) — the only new
dependency this feature introduces.

### Worker logic, split for testability

The decision of whether to notify, and with what text, is a plain function with no
Android dependency:

```kotlin
fun buildReminderNotification(
    supplements: List<Supplement>,
    todaysIntakes: Set<Long>, // supplement IDs already taken today
): ReminderNotificationContent? // null => nothing pending, skip silently
```

`HabitsReminderWorker` (in `androidMain`) is a thin wrapper: it loads supplements +
today's intakes, calls this function, and if it returns non-null, posts it via
`NotificationManagerCompat` on a new `"habits_reminders"` notification channel (created
once at app startup, alongside however the existing export/import notifications
register their channel). Tapping the notification opens `Home` — the Habits card is
wherever the user has it ordered, same as any other home card.

## Testing

- `buildReminderNotification`: empty supplement list, all-taken, some-pending cases —
  pure unit tests, no Android/WorkManager involved.
- Supplement repository: add, remove (cascades intake rows), toggle intake for a date.
- `HabitsPreferences` round-trip through the preferences repository.
- Room migration fixture test for 34 → 35, per the existing migration-test convention
  referenced in `docs/schema.md`.

## Non-goals (explicitly out of scope for this spec)

- Dose/amount tracking for supplements (adherence only, per decision above).
- Per-supplement reminder times (one shared pair of times for everything).
- Inline "mark all taken" action button on the notification itself (tap opens Home;
  marking happens on the card).
- Any change to the existing caffeine goal/target logic in `GoalsScreen` — the coffee
  row reads that aggregate, it does not modify it.

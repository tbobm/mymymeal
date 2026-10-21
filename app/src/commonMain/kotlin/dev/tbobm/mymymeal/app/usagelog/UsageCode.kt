package dev.tbobm.mymymeal.app.usagelog

/**
 * Stable ids for usage events. Never renumber or reuse an id: stored rows and exported CSVs refer
 * to them. Only action/navigation codes -- never user content.
 */
enum class UsageCode(val id: Int) {
    NAV_HOME(1),
    NAV_SETTINGS(2),
    NAV_STATS(3),
    NAV_FOOD_SEARCH(4),
    NAV_QUICK_ADD(5),
    NAV_ADD_ENTRY(6),
    NAV_EDIT_ENTRY(7),
    NAV_CALENDAR(8),
    NAV_GOALS(9),
    NAV_DATABASE(10),
    NAV_OTHER(99),
    ACT_ADD_CLICK(100),
    ACT_QUICKADD_CLICK(101),
    ACT_LOG_ENTRY(102),
    ACT_LOG_MANUAL(103),
    ACT_LOG_BARCODE(104),
    ACT_EDIT_ENTRY(105),
    ACT_DELETE_ENTRY(106);

    companion object {
        private val byId = entries.associateBy { it.id }

        fun fromId(id: Int): UsageCode? = byId[id]

        private val navByRoute =
            mapOf(
                "Home" to NAV_HOME,
                "Settings" to NAV_SETTINGS,
                "Stats" to NAV_STATS,
                "FoodDiarySearch" to NAV_FOOD_SEARCH,
                "FoodDiaryCreateQuickAdd" to NAV_QUICK_ADD,
                "UpdateQuickAdd" to NAV_QUICK_ADD,
                "FoodDiaryCreateEntry" to NAV_ADD_ENTRY,
                "FoodDiaryUpdateEntry" to NAV_EDIT_ENTRY,
                "CalendarMonth" to NAV_CALENDAR,
                "Goals" to NAV_GOALS,
                "DatabaseSettings" to NAV_DATABASE,
            )

        /** Maps a type-safe nav route (`pkg.Class/{arg}`) to a code; unknown screens are NAV_OTHER. */
        fun fromRoute(route: String?): UsageCode =
            navByRoute[route?.substringBefore('/')?.substringBefore('?')?.substringAfterLast('.')]
                ?: NAV_OTHER
    }
}

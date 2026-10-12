package dev.tbobm.mymymeal.app.settings.domain.entity

enum class EnergyFormat {
    Kilocalories,
    Kilojoules;

    companion object {
        val DEFAULT = Kilocalories
    }
}

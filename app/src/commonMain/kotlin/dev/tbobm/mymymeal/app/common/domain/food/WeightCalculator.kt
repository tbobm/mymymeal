package dev.tbobm.mymymeal.app.common.domain.food

import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement

object WeightCalculator {
    fun calculateWeight(
        measurement: Measurement,
        totalWeight: Double?,
        servingWeight: Double?,
    ): Double? =
        when (measurement) {
            is Measurement.ImmutableMeasurement -> measurement.metric
            is Measurement.Package -> totalWeight?.times(measurement.quantity)
            is Measurement.Serving -> servingWeight?.times(measurement.quantity)
        }

    fun calculateWeight(
        measurement: Measurement,
        totalWeight: Double,
        servingWeight: Double,
    ): Double =
        when (measurement) {
            is Measurement.ImmutableMeasurement -> measurement.metric
            is Measurement.Package -> totalWeight * measurement.quantity
            is Measurement.Serving -> servingWeight * measurement.quantity
        }
}

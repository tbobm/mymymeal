package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.food.domain.entity.RemoteNutritionFacts
import com.maksimowiczm.foodyou.food.domain.entity.RemoteProduct
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Natural-unit JSON the AI is asked to return. Key suffix = unit (g, mg, ug, kcal). */
@Serializable
private data class AiProductJson(
    val name: String? = null,
    val brand: String? = null,
    val isLiquid: Boolean? = null,
    @SerialName("servingWeight_g") val servingWeight: Double? = null,
    @SerialName("packageWeight_g") val packageWeight: Double? = null,
    @SerialName("energy_kcal") val energy: Double? = null,
    @SerialName("proteins_g") val proteins: Double? = null,
    @SerialName("carbohydrates_g") val carbohydrates: Double? = null,
    @SerialName("fats_g") val fats: Double? = null,
    @SerialName("saturatedFats_g") val saturatedFats: Double? = null,
    @SerialName("transFats_g") val transFats: Double? = null,
    @SerialName("monounsaturatedFats_g") val monounsaturatedFats: Double? = null,
    @SerialName("polyunsaturatedFats_g") val polyunsaturatedFats: Double? = null,
    @SerialName("omega3_g") val omega3: Double? = null,
    @SerialName("omega6_g") val omega6: Double? = null,
    @SerialName("sugars_g") val sugars: Double? = null,
    @SerialName("addedSugars_g") val addedSugars: Double? = null,
    @SerialName("salt_g") val salt: Double? = null,
    @SerialName("dietaryFiber_g") val dietaryFiber: Double? = null,
    @SerialName("solubleFiber_g") val solubleFiber: Double? = null,
    @SerialName("insolubleFiber_g") val insolubleFiber: Double? = null,
    @SerialName("cholesterol_mg") val cholesterol: Double? = null,
    @SerialName("caffeine_mg") val caffeine: Double? = null,
    @SerialName("vitaminA_ug") val vitaminA: Double? = null,
    @SerialName("vitaminB1_mg") val vitaminB1: Double? = null,
    @SerialName("vitaminB2_mg") val vitaminB2: Double? = null,
    @SerialName("vitaminB3_mg") val vitaminB3: Double? = null,
    @SerialName("vitaminB5_mg") val vitaminB5: Double? = null,
    @SerialName("vitaminB6_mg") val vitaminB6: Double? = null,
    @SerialName("vitaminB7_ug") val vitaminB7: Double? = null,
    @SerialName("vitaminB9_ug") val vitaminB9: Double? = null,
    @SerialName("vitaminB12_ug") val vitaminB12: Double? = null,
    @SerialName("vitaminC_mg") val vitaminC: Double? = null,
    @SerialName("vitaminD_ug") val vitaminD: Double? = null,
    @SerialName("vitaminE_mg") val vitaminE: Double? = null,
    @SerialName("vitaminK_ug") val vitaminK: Double? = null,
    @SerialName("manganese_mg") val manganese: Double? = null,
    @SerialName("magnesium_mg") val magnesium: Double? = null,
    @SerialName("potassium_mg") val potassium: Double? = null,
    @SerialName("calcium_mg") val calcium: Double? = null,
    @SerialName("copper_mg") val copper: Double? = null,
    @SerialName("zinc_mg") val zinc: Double? = null,
    @SerialName("sodium_mg") val sodium: Double? = null,
    @SerialName("iron_mg") val iron: Double? = null,
    @SerialName("phosphorus_mg") val phosphorus: Double? = null,
    @SerialName("selenium_ug") val selenium: Double? = null,
    @SerialName("iodine_ug") val iodine: Double? = null,
    @SerialName("chromium_ug") val chromium: Double? = null,
)

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/** Prompt to paste into an AI assistant; its JSON reply is accepted by [parseAiProduct]. */
fun aiProductPrompt(foodName: String): String {
    val descriptor = AiProductJson.serializer().descriptor
    val keys = (0 until descriptor.elementsCount).joinToString(", ") { descriptor.getElementName(it) }
    val food = foodName.trim().ifBlank { "<FOOD NAME>" }
    return """
        Give the average nutritional values of the generic food "$food", based on typical average products (not a specific brand).
        Reply with a single JSON object only, no prose, no markdown.
        Values are per 100 g (per 100 ml if the food is a liquid, then set isLiquid to true).
        Use exactly these keys: $keys.
        The key suffix is the unit: _g grams, _mg milligrams, _ug micrograms, _kcal kilocalories. Numbers only.
        "name" is a short name, "brand" must be null. servingWeight_g is a typical serving in grams, packageWeight_g must be null.
        Use null for any value you do not know instead of guessing.
        """
        .trimIndent()
}

/** Parses an AI reply (JSON possibly wrapped in prose or code fences); null if unusable. */
fun parseAiProduct(text: String): RemoteProduct? {
    val start = text.indexOf('{')
    val end = text.lastIndexOf('}')
    if (start < 0 || end <= start) return null

    val ai =
        try {
            json.decodeFromString<AiProductJson>(text.substring(start, end + 1))
        } catch (e: Exception) {
            return null
        }

    // energy is required by the form; without it the reply is not a nutrition answer
    if (ai.energy == null) return null

    fun Double?.mg() = this?.div(1_000.0)

    fun Double?.ug() = this?.div(1_000_000.0)

    return RemoteProduct(
        name = ai.name,
        brand = ai.brand,
        barcode = null,
        nutritionFacts =
            RemoteNutritionFacts(
                proteins = ai.proteins,
                carbohydrates = ai.carbohydrates,
                fats = ai.fats,
                energy = ai.energy,
                saturatedFats = ai.saturatedFats,
                transFats = ai.transFats,
                monounsaturatedFats = ai.monounsaturatedFats,
                polyunsaturatedFats = ai.polyunsaturatedFats,
                omega3 = ai.omega3,
                omega6 = ai.omega6,
                sugars = ai.sugars,
                addedSugars = ai.addedSugars,
                salt = ai.salt,
                dietaryFiber = ai.dietaryFiber,
                solubleFiber = ai.solubleFiber,
                insolubleFiber = ai.insolubleFiber,
                cholesterol = ai.cholesterol.mg(),
                caffeine = ai.caffeine.mg(),
                vitaminA = ai.vitaminA.ug(),
                vitaminB1 = ai.vitaminB1.mg(),
                vitaminB2 = ai.vitaminB2.mg(),
                vitaminB3 = ai.vitaminB3.mg(),
                vitaminB5 = ai.vitaminB5.mg(),
                vitaminB6 = ai.vitaminB6.mg(),
                vitaminB7 = ai.vitaminB7.ug(),
                vitaminB9 = ai.vitaminB9.ug(),
                vitaminB12 = ai.vitaminB12.ug(),
                vitaminC = ai.vitaminC.mg(),
                vitaminD = ai.vitaminD.ug(),
                vitaminE = ai.vitaminE.mg(),
                vitaminK = ai.vitaminK.ug(),
                manganese = ai.manganese.mg(),
                magnesium = ai.magnesium.mg(),
                potassium = ai.potassium.mg(),
                calcium = ai.calcium.mg(),
                copper = ai.copper.mg(),
                zinc = ai.zinc.mg(),
                sodium = ai.sodium.mg(),
                iron = ai.iron.mg(),
                phosphorus = ai.phosphorus.mg(),
                selenium = ai.selenium.ug(),
                iodine = ai.iodine.ug(),
                chromium = ai.chromium.ug(),
            ),
        packageWeight = ai.packageWeight,
        servingWeight = ai.servingWeight,
        source = FoodSource(FoodSource.Type.User),
        isLiquid = ai.isLiquid ?: false,
    )
}

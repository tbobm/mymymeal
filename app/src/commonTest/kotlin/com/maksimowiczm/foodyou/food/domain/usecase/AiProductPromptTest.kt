package com.maksimowiczm.foodyou.food.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiProductPromptTest {
    @Test
    fun parsesFencedJsonWrappedInProseAndConvertsUnits() {
        val reply =
            """
            Sure! Here you go:
            ```json
            {"name": "Banana", "energy_kcal": 89, "proteins_g": 1.1, "sodium_mg": 400,
             "vitaminB12_ug": 2.4, "isLiquid": false, "unknown_key": 1}
            ```
            Hope it helps.
            """
                .trimIndent()

        val product = assertNotNull(parseAiProduct(reply))
        val facts = assertNotNull(product.nutritionFacts)

        assertEquals("Banana", product.name)
        assertEquals(89.0, facts.energy)
        assertEquals(0.4, facts.sodium!!, 1e-9)
        assertEquals(2.4e-6, facts.vitaminB12!!, 1e-12)
        assertNull(facts.fats)
        assertNull(facts.vitaminC)
    }

    @Test
    fun rejectsGarbageAndEmptyObjects() {
        assertNull(parseAiProduct("no json here"))
        assertNull(parseAiProduct("{not valid"))
        assertNull(parseAiProduct("{}"))
    }

    @Test
    fun promptListsEveryKeyAndFoodName() {
        val prompt = aiProductPrompt("Banana")
        assertTrue("Banana" in prompt)
        assertTrue("vitaminB12_ug" in prompt)
        assertTrue("energy_kcal" in prompt)
    }
}

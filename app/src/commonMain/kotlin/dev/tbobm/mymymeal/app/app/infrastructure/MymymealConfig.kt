package dev.tbobm.mymymeal.app.app.infrastructure

import dev.tbobm.mymymeal.app.app.BuildConfig
import dev.tbobm.mymymeal.app.common.config.AppConfig
import dev.tbobm.mymymeal.app.common.config.NetworkConfig

internal class MymymealConfig : AppConfig, NetworkConfig {
    override val versionName: String = BuildConfig.VERSION_NAME
    override val contactEmailUri: String =
        "mailto:tbobm@protonmail.com?subject=mymymeal Feedback&body=mymymeal Version: $versionName\n"
    override val translationUri: String = "https://crowdin.com/project/food-you"
    override val sourceCodeUri: String = "https://github.com/tbobm/mymymeal"
    override val issueTrackerUri: String = "https://github.com/tbobm/mymymeal/issues"
    override val privacyPolicyUri: String = "https://tbobm.github.io/mymymeal/privacy-policy/"
    override val openFoodFactsTermsOfUseUri: String = "https://world.openfoodfacts.org/terms-of-use"
    override val openFoodFactsPrivacyPolicyUri: String = "https://world.openfoodfacts.org/privacy"
    override val foodDataCentralPrivacyPolicyUri: String = "https://www.usda.gov/privacy-policy"

    override val userAgent: String = "mymymeal/$versionName (tbobm@protonmail.com)"
}

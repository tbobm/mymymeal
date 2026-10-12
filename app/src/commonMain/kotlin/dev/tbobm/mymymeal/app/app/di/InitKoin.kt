package dev.tbobm.mymymeal.app.app.di

import dev.tbobm.mymymeal.app.app.ui.uiModule
import dev.tbobm.mymymeal.app.changelog.changelogModule
import dev.tbobm.mymymeal.app.food.foodModule
import dev.tbobm.mymymeal.app.food.search.foodSearchModule
import dev.tbobm.mymymeal.app.fooddiary.foodDiaryModule
import dev.tbobm.mymymeal.app.goals.goalsModule
import dev.tbobm.mymymeal.app.habits.habitsModule
import dev.tbobm.mymymeal.app.importexport.importExportModule
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.importExportSwissFoodCompositionDatabaseModule
import dev.tbobm.mymymeal.app.poll.pollModule
import dev.tbobm.mymymeal.app.settings.settingsModule
import dev.tbobm.mymymeal.app.sponsorship.sponsorshipModule
import dev.tbobm.mymymeal.app.theme.themeModule
import kotlinx.coroutines.CoroutineScope
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(applicationCoroutineScope: CoroutineScope, config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)

        modules(appModule(applicationCoroutineScope))
        modules(uiModule)
        modules(
            changelogModule,
            foodModule,
            foodSearchModule,
            foodDiaryModule,
            goalsModule,
            habitsModule,
            importExportModule,
            importExportSwissFoodCompositionDatabaseModule,
            pollModule,
            settingsModule,
            sponsorshipModule,
            themeModule,
        )
    }

package dev.tbobm.mymymeal.app.sponsorship

import dev.tbobm.mymymeal.app.sponsorship.domain.sponsorshipDomainModule
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.sponsorshipInfrastructureModule
import org.koin.dsl.module

val sponsorshipModule = module {
    sponsorshipDomainModule()
    sponsorshipInfrastructureModule()
}

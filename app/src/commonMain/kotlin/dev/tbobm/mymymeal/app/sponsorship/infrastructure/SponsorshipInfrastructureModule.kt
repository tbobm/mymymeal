package dev.tbobm.mymymeal.app.sponsorship.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.common.infrastructure.network.RateLimiter
import dev.tbobm.mymymeal.app.sponsorship.domain.repository.SponsorRepository
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.github.GithubSponsorsApiClient
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.room.SponsorshipDatabase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.onClose

internal fun Module.sponsorshipInfrastructureModule() {
    single(named("ktorSponsorshipHttpClient")) {
            HttpClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        }
        .onClose { it?.close() }

    single(named("sponsorshipRateLimiter")) {
        RateLimiter(dateProvider = get(), maxRequests = 10, timeWindow = 1.seconds)
    }
    factory {
            GithubSponsorsApiClient(
                httpClient = get(named("ktorSponsorshipHttpClient")),
                config = get(),
                rateLimiter = get(named("sponsorshipRateLimiter")),
            )
        }
        .bind<SponsorsNetworkDataSource>()

    userPreferencesRepositoryOf(::DataStoreSponsorshipPreferencesDataSource)

    factory {
            SponsorRepositoryImpl(
                sponsorshipDao = get(),
                networkDataSource = get(),
                preferences = userPreferencesRepository(),
                logger = get(),
            )
        }
        .bind<SponsorRepository>()

    factory { database.sponsorshipDao }
}

private val Scope.database: SponsorshipDatabase
    get() = get()

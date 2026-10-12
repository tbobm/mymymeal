package dev.tbobm.mymymeal.app.sponsorship.infrastructure.github

import dev.tbobm.mymymeal.app.common.config.NetworkConfig
import dev.tbobm.mymymeal.app.common.infrastructure.network.RateLimiter
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.NetworkSponsorship
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.SponsorsNetworkDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.userAgent
import kotlinx.datetime.YearMonth

internal class GithubSponsorsApiClient(
    private val httpClient: HttpClient,
    private val config: NetworkConfig,
    private val rateLimiter: RateLimiter,
) : SponsorsNetworkDataSource {
    override suspend fun getSponsorships(yearMonth: YearMonth): List<NetworkSponsorship> {
        if (rateLimiter.canMakeRequest()) rateLimiter.recordRequest()
        else error("Rate limit exceeded")

        val baseUrl = API_URL
        val month = yearMonth.month.ordinal + 1
        val path = "${yearMonth.year}/$month.json"
        val url = "${baseUrl}/$path"

        val response = httpClient.get(url) { userAgent(config.userAgent) }

        return if (response.status == HttpStatusCode.NotFound) listOf()
        else response.body<List<NetworkSponsorship>>()
    }

    private companion object {
        private const val API_URL = "https://sponsors.maksimowiczm.com"
    }
}

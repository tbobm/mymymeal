package dev.tbobm.mymymeal.app.sponsorship.infrastructure

import kotlinx.datetime.YearMonth

internal interface SponsorsNetworkDataSource {
    suspend fun getSponsorships(yearMonth: YearMonth): List<NetworkSponsorship>
}

package dev.tbobm.mymymeal.app.common.domain.database

interface TransactionScope<T> {

    suspend fun rollback(result: T)
}

package dev.tbobm.mymymeal.app.common.infrastructure.room

import androidx.room.TransactionScope

class RoomTransactionScope<T>(private val scope: TransactionScope<T>) :
    dev.tbobm.mymymeal.app.common.domain.database.TransactionScope<T> {
    override suspend fun rollback(result: T) = scope.rollback(result)
}

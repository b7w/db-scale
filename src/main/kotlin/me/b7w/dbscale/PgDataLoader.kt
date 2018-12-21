package me.b7w.dbscale

import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult
import java.util.*
import kotlin.random.Random


suspend fun PgPool.begin(): PgTransaction {
    return awaitResult {
        this.begin(it)
    }
}

suspend fun PgPool.queryAwait(sql: String): PgRowSet {
    return awaitResult {
        this.query(sql, it)
    }
}

suspend fun PgPool.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
    return awaitResult {
        this.preparedQuery(sql, arguments, it)
    }
}

suspend fun PgTransaction.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
    return awaitResult {
        this.preparedBatch(sql, batch, it)
    }
}


suspend fun PgTransaction.commitAwait() {
    awaitResult<Void> {
        this.commit(it)
    }
}

private var CACHE_USERS = listOf<UUID>()

class PgDataLoaderNew(val client: PgPool) {

    suspend fun select(): PgRowSet {
        if (CACHE_USERS.isEmpty()) {
            CACHE_USERS = client
                .preparedQueryAwait("SELECT * FROM users", Tuple.tuple())
                .map { it.getUUID("id") }
        }
        return client.preparedQueryAwait("SELECT * FROM users WHERE id=$1", Tuple.of(CACHE_USERS.random()))
    }

    suspend fun countUsers(): PgRowSet {
        return client.queryAwait("SELECT COUNT(id) FROM users")
    }

    suspend fun deleteUsers(): PgRowSet {
        return client.queryAwait("DELETE FROM users")
    }

    suspend fun insertUser(): PgRowSet {
        val params = createUser()
        return client.preparedQueryAwait("INSERT INTO users VALUES ($1, $2)", params)
    }


    suspend fun insertUsers(count: Long): Pair<Boolean, String> {
        val tx = client.begin()
        return try {
            val params = range(count, start = 1).map { createUser() }
            val rowSet = tx.preparedBatchAwait("INSERT INTO users VALUES ($1, $2)", params)
            tx.commitAwait()
            Pair(true, "Success ${rowSet.size()}")
        } catch (e: Exception) {
            tx.rollback()
            Pair(false, "Error: ${e.message}")
        }
    }

    private fun createUser() = Tuple.of(UUID.randomUUID(), Random.nextLong(999999999999999999L).toString())

}

package me.b7w.dbscale

import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import java.util.*
import kotlin.random.Random


class PgDataLoaderNew(val client: PgPool) {

    suspend fun select(usersCache: List<String>): PgRowSet {
        return client.preparedQueryAwait(
            "SELECT * FROM users WHERE id=$1",
            Tuple.of(UUID.fromString(usersCache.random()))
        )
    }

    suspend fun countUsers(): PgRowSet {
        return client.queryAwait("SELECT COUNT(id) FROM users")
    }

    suspend fun deleteUsers(): PgRowSet {
        return client.queryAwait("DELETE FROM users")
    }

    suspend fun truncateUsers(): PgRowSet {
        return client.queryAwait("TRUNCATE TABLE users CASCADE;")
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

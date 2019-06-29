package me.b7w.dbscale.archive

import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import me.b7w.dbscale.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class PgGenerator(val client: PgPool, val counter: AtomicInteger) {

    suspend fun createTables(): PgRowSet {
        LOG.info("Create tables")
        return client.queryAwait(
            """
            CREATE TABLE IF NOT EXISTS users(
                id VARCHAR(64) PRIMARY KEY,
                username VARCHAR(64) UNIQUE NOT NULL,
                desertion VARCHAR(256) UNIQUE NOT NULL
            );
            """
        )
    }

    suspend fun select(): PgRowSet {
        val id = Random.nextInt(counter.get()).toString()
        return client.preparedQueryAwait(
            "SELECT * FROM users WHERE id=$1",
            Tuple.of(id)
        )
    }

    suspend fun countUsers(): PgRowSet {
        return client.queryAwait("SELECT COUNT(id) FROM users")
    }

    suspend fun truncateUsers(): PgRowSet {
        return client.queryAwait("TRUNCATE TABLE users CASCADE;")
    }

    suspend fun insertUser(): PgRowSet {
        val params = createUser()
        return client.preparedQueryAwait("INSERT INTO users VALUES ($1, $2, $3)", params)
    }

    suspend fun insertUsers(count: Long): Pair<Boolean, String> {
        val tx = client.begin()
        return try {
            val params = range(count, start = 1).map { createUser() }
            val rowSet = tx.preparedBatchAwait("INSERT INTO users VALUES ($1, $2, $3)", params)
            tx.commitAwait()
            Pair(true, "Success ${rowSet.size()}")
        } catch (e: Exception) {
            tx.rollback()
            Pair(false, "Error: ${e.message}")
        }
    }

    private fun createUser() = Tuple.of(
        counter.getAndIncrement().toString(),
        UUID.randomUUID().toString(),
        Random.nextLong(999999999999999999L).toString()
    )

}

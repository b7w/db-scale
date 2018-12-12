package me.b7w.dbscale

import io.vertx.core.json.JsonArray
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.random.Random

class PgDataLoader(val client: AsyncSQLClient) {

    suspend fun countUsers(): JsonArray? {
        return client.querySingleAwait("SELECT COUNT(id) FROM users")
    }

    suspend fun deleteUsers(): JsonArray? {
        return client.querySingleAwait("DELETE FROM users")
    }

    suspend fun insertUser(): JsonArray? {
        val params = JsonArray(UUID.randomUUID(), Random.nextLong(999999999999999999L))
        return client.querySingleWithParamsAwait("INSERT INTO users VALUES (?, ?)", params)
    }


    suspend fun insertUsers(count: Long): String = coroutineScope {
        val connection = client.getConnectionAwait()
        connection.setAutoCommitAwait(false)
        for (i in 1..count) {
            val params = JsonArray(UUID.randomUUID(), Random.nextLong(999999999999999999L))
            LOG.trace("Insert params: {}", params)
            connection.querySingleWithParamsAwait("INSERT INTO users VALUES (?, ?)", params)
        }
        connection.commitAwait()
        connection.setAutoCommitAwait(true)
        connection.closeAwait()
        return@coroutineScope "Success $count"
    }

}

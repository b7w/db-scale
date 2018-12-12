package me.b7w.dbscale

import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.ext.sql.commitAwait
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.setAutoCommitAwait
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.random.Random

class PgDataLoader(val client: AsyncSQLClient) {

    suspend fun insertUsers(count: Long): String = coroutineScope {
        val connection = client.getConnectionAwait()
        connection.setAutoCommitAwait(false)
        for (i in 0..count) {
            val params = JsonArray(UUID.randomUUID(), Random.nextLong(999999999999999999L))
            LOG.trace("Insert params: {}", params)
            connection.querySingleWithParamsAwait("INSERT INTO users VALUES (?, ?)", params)
        }
        connection.commitAwait()
        return@coroutineScope "Success $count"
    }

}

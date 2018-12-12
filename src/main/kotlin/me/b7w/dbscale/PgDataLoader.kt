package me.b7w.dbscale

import io.vertx.core.json.JsonArray
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.coroutines.awaitResult
import java.util.*
import kotlin.random.Random

class PgDataLoader(val client: AsyncSQLClient) {

    suspend fun insertUsers(count: Long): String {
        awaitResult<JsonArray> {
            val params = JsonArray(UUID.randomUUID(), Random.nextLong(999999999999999999L))
            LOG.trace("Insert params: {}", params)
            client.querySingleWithParams("INSERT INTO users VALUES (?, ?)", params, it)
        }
        return "Success $count"
    }

}

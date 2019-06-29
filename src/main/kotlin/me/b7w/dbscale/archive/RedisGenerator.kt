package me.b7w.dbscale.archive

import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.redis.client.dbsizeAwait
import io.vertx.kotlin.redis.client.delAwait
import io.vertx.kotlin.redis.client.getAwait
import io.vertx.kotlin.redis.client.setAwait
import io.vertx.redis.client.RedisAPI
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.b7w.dbscale.range
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class RedisGenerator(val client: RedisAPI, val counter: AtomicInteger) {

    suspend fun count(): String? {
        return client.dbsizeAwait().toString()
    }

    suspend fun insertUser(): String? {
        val params = createUser()
        val id = params.get<String>("id")
        client.setAwait(listOf("users:$id", params.toString()))
        return id
    }

    suspend fun insertUsers(count: Long) = coroutineScope {
        range(count, start = 1)
            .map { async { createUser() } }
            .awaitAll()
    }

    suspend fun select(): String? {
        val id = Random.nextInt(counter.get()).toString()
        return client.getAwait("users:$id").toString()
    }

    suspend fun select(count: Int): String? {
        val id = Random.nextInt(count).toString()
        return client.getAwait("users:$id").toString()
    }

    suspend fun drop() {
        client.delAwait(listOf("*")).toString()
        counter.set(1)
    }

    private fun createUser() = jsonObjectOf(
        "id" to counter.getAndIncrement().toString(),
        "username" to UUID.randomUUID().toString(),
        "desertion" to Random.nextLong(999999999999999999L).toString()
    )

}

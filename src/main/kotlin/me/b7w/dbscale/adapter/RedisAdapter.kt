package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.kotlin.redis.client.connectAwait
import io.vertx.kotlin.redis.client.flushallAwait
import io.vertx.kotlin.redis.client.getAwait
import io.vertx.kotlin.redis.client.setAwait
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import me.b7w.dbscale.Properties
import kotlin.concurrent.getOrSet


class RedisAdapter(val properties: Properties) : IAdapter {

    val client = ThreadLocal<RedisAPI>()

    override suspend fun name(): String = "redis"

    override suspend fun connect(vertx: Vertx) {
        if (client.get() == null) {
            val options = properties.redis()
            if (options != null) {
                val c = client.getOrSet {
                    val pool = Redis.createClient(vertx, options).connectAwait()
                    RedisAPI.api(pool)
                }
                val sample = sampleData(sampleKey())
                c.setAwait(listOf(sample.key, sample.value.encode()))
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val result = this.client.get().getAwait(sampleKey()).toString()
        return Tuple.of(sampleKey(), result)
    }

    override suspend fun removeAll() {
        this.client.get().flushallAwait(listOf())
    }

}

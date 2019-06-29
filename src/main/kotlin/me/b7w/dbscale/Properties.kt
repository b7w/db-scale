package me.b7w.dbscale

import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.cassandra.CassandraClientOptions
import io.vertx.config.ConfigRetriever
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.redis.client.RedisOptions

class Properties(val retriever: ConfigRetriever) {

    data class ClickHouseOptions(
        val url: String = "",
        val database: String = "",
        val username: String = "",
        val password: String = ""
    )

    suspend fun pg(): PgPoolOptions? = retriever
        .getConfigAwait()
        .getJsonObject("pg")
        ?.mapTo(PgPoolOptions::class.java)

    suspend fun cockroach(): PgPoolOptions? = retriever
        .getConfigAwait()
        .getJsonObject("cockroach")
        ?.mapTo(PgPoolOptions::class.java)

    suspend fun mongo(): JsonObject? = retriever
        .getConfigAwait()
        .getJsonObject("mongo")

    suspend fun clickhouse(): ClickHouseOptions? = retriever
        .getConfigAwait()
        .getJsonObject("clickhouse")
        ?.mapTo(ClickHouseOptions::class.java)

    suspend fun cassandra(): CassandraClientOptions? = retriever
        .getConfigAwait()
        .getJsonObject("cassandra")
        ?.let { CassandraClientOptions(it) }

    suspend fun scylla(): CassandraClientOptions? = retriever
        .getConfigAwait()
        .getJsonObject("scylla")
        ?.let { CassandraClientOptions(it) }


    suspend fun redis(): RedisOptions? = retriever
        .getConfigAwait()
        .getJsonObject("redis")
        ?.let {
            val addr = it.getString("address").split(":")
            RedisOptions(it).setEndpoint(SocketAddress.inetSocketAddress(addr.get(1).toInt(), addr.get(0)))
        }

}

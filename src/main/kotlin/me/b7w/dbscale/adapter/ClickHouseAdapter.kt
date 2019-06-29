package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait
import me.b7w.dbscale.Properties


class ClickHouseAdapter(val properties: Properties) : IAdapter {

    var client: ClickHouseClient? = null

    class ClickHouseClient(val web: WebClient, val options: Properties.ClickHouseOptions) {

        suspend fun execute(sql: String): String {
            val response = web
                .getAbs(options.url)
                .addQueryParam("database", options.database)
                .addQueryParam("query", sql)
                .putHeader("X-ClickHouse-User", options.username)
                .putHeader("X-ClickHouse-Key", options.password)
                .sendAwait()
            return response.bodyAsString()
        }

    }

    override suspend fun name(): String = "clickhouse"

    override suspend fun connect(vertx: Vertx) {
        if (client == null) {
            val options = properties.clickhouse()
            if (options != null) {
                client = ClickHouseClient(WebClient.create(vertx), options)
                val sample = sampleData(sampleKey())
                client.getOrFail().execute("SELECT now()")
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val result = client.getOrFail().execute("SELECT now()")
        return Tuple.of(sampleKey(), result)
    }

    override suspend fun removeAll() {
        throw NotImplementedError()
    }

}

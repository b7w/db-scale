package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendBufferAwait
import me.b7w.dbscale.Properties


class ClickHouseAdapter(val properties: Properties) : IAdapter {

    var client: ClickHouseClient? = null

    class ClickHouseClient(val web: WebClient, val options: Properties.ClickHouseOptions) {

        suspend fun execute(sql: String): String {
            val response = web
                .postAbs(options.url)
                .addQueryParam("database", options.database)
                .putHeader("X-ClickHouse-User", options.username)
                .putHeader("X-ClickHouse-Key", options.password)
                .sendBufferAwait(Buffer.buffer(sql).appendString("\n"))
            if (response.statusCode() != 200) {
                throw Exception(response.bodyAsString())
            }
            if (response.body() != null && response.body().length() > 0) {
                return response.bodyAsString()
            }
            return "Ok"
        }

    }

    override suspend fun name(): String = "clickhouse"

    override suspend fun connect(vertx: Vertx) {
        if (client == null) {
            val options = properties.clickhouse()
            if (options != null) {
                client = ClickHouseClient(WebClient.create(vertx), options)
                val sample = sampleData(sampleKey())
                client.getOrFail()
                    .execute(
                        """
                        CREATE TABLE IF NOT EXISTS sample (id String, value String)
                        ENGINE = MergeTree ORDER BY id
                        """
                    )
                val count = client.getOrFail().execute("SELECT count(id) FROM sample WHERE id = '${sample.key}'")
                if ("0" == count.trim()) {
                    client.getOrFail()
                        .execute("INSERT INTO sample VALUES ('${sample.key}', '${sample.value}')")
                }
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val id = sampleKey()
        val result = client.getOrFail().execute("SELECT value FROM sample WHERE id = '$id'").trim()
        return Tuple.of(id, result)
    }

    override suspend fun removeAll() {
        client.getOrFail().execute("DROP DATABASE IF EXISTS sample")
    }

}

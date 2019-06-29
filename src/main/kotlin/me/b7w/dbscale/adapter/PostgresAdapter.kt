package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.*
import io.vertx.core.Vertx
import me.b7w.dbscale.Properties
import me.b7w.dbscale.preparedQueryAwait
import me.b7w.dbscale.queryAwait


open class PostgresAdapter(val properties: Properties) : IAdapter {

    private var client: PgPool? = null

    override suspend fun name(): String = "postgres"

    open suspend fun clientOptions(): PgPoolOptions? = properties.pg()

    override suspend fun connect(vertx: Vertx) {
        if (client == null) {
            val options = clientOptions()
            if (options != null) {
                client = PgClient.pool(vertx, options)
                client.getOrFail().queryAwait(
                    """
                CREATE TABLE IF NOT EXISTS sample(
                    key VARCHAR(36) PRIMARY KEY,
                    value jsonb NOT NULL
                );
                """
                )
                val sample = sampleData(sampleKey())
                val arguments = Tuple.of(sample.key, sample.value.encode())
                try {
                    client.getOrFail().preparedQueryAwait("INSERT INTO sample VALUES ($1, $2)", arguments)
                } catch (e: PgException) {
                }
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val res = client.getOrFail().preparedQueryAwait(
            "SELECT * FROM sample WHERE key=$1",
            Tuple.of(sampleKey())
        )
        return res.map {
            Tuple.of(it.getString("key"), it.getJson("value"))
        }.first()
    }

    override suspend fun removeAll() {
        client.getOrFail().queryAwait("TRUNCATE TABLE sample CASCADE;")
    }

}

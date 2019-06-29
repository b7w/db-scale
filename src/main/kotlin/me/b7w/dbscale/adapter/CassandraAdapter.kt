package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.Tuple
import io.vertx.cassandra.CassandraClient
import io.vertx.core.Vertx
import io.vertx.kotlin.cassandra.executeAwait
import me.b7w.dbscale.Properties
import me.b7w.dbscale.executeWithFullFetchAwait


open class CassandraAdapter(val properties: Properties) : IAdapter {

    private var client: CassandraClient? = null

    open val replicationFactor = 1

    override suspend fun name(): String = "cassandra"

    open suspend fun clientOptions() = properties.cassandra()

    override suspend fun connect(vertx: Vertx) {
        if (client == null) {
            val options = clientOptions()
            if (options != null) {
                client = CassandraClient.createNonShared(vertx, options)

                client.getOrFail().executeAwait(
                    """
                    CREATE KEYSPACE IF NOT EXISTS root WITH replication = {'class':'SimpleStrategy', 'replication_factor' : $replicationFactor};
                    """
                )
                client.getOrFail().executeAwait(
                    """
                    CREATE TABLE IF NOT EXISTS root.sample(
                        id TEXT PRIMARY KEY,
                        value TEXT
                    );
                    """
                )
                val sample = sampleData(sampleKey())
                try {
                    client.getOrFail().executeAwait(
                        "INSERT INTO root.sample (id, value) VALUES ('${sample.key}', '${sample.value}');"
                    )
                } catch (e: NotImplementedError) {
                }
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val id = sampleKey()
        val result = client.getOrFail().executeWithFullFetchAwait(
            "SELECT * FROM root.sample WHERE id='$id'"
        )
        return result.map { Tuple.of(it.getString(0), it.getString(1)) }.first()

    }

    override suspend fun removeAll() {
        client.getOrFail().executeAwait("TRUNCATE TABLE root.users;")
    }

}

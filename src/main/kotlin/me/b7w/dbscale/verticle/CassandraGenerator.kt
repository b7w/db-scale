package me.b7w.dbscale.verticle

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.SimpleStatement
import io.vertx.cassandra.CassandraClient
import io.vertx.kotlin.cassandra.executeAwait
import me.b7w.dbscale.LOG
import me.b7w.dbscale.executeWithFullFetchAwait
import me.b7w.dbscale.range
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class CassandraGenerator(val client: CassandraClient, val counter: AtomicInteger) {


    suspend fun createKeySpace(): io.vertx.cassandra.ResultSet {
        LOG.info("Create keyspace")
        return client.executeAwait(
            """
            CREATE KEYSPACE IF NOT EXISTS root WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3};
            """
        )
    }

    suspend fun createTables(): io.vertx.cassandra.ResultSet {
        LOG.info("Create tables")
        return client.executeAwait(
            """
            CREATE TABLE IF NOT EXISTS root.users(
                id TEXT PRIMARY KEY,
                username TEXT,
                desertion TEXT
            );
            """
        )
    }

    suspend fun select(): List<Triple<String, String, String>> {
        val id = Random.nextInt(counter.get()).toString()
        return client.executeWithFullFetchAwait(
            "SELECT * FROM root.users WHERE id='$id'"
        ).map { Triple(it.getString(0), it.getString(1), it.getString(2)) }
    }

    suspend fun countUsers(): Long {
        return client.executeWithFullFetchAwait("SELECT COUNT(id) FROM root.users;").first().getLong(0)
    }

    suspend fun truncateUsers() {
        client.executeAwait("TRUNCATE TABLE root.users;")
    }

    suspend fun insertUser(): Boolean {
        val params = createUser()
        return client.executeAwait("INSERT INTO root.users (id, username, desertion) VALUES ('${params.first}', '${params.second}', '${params.third}');")
            .wasApplied()
    }

    suspend fun insertUsers(count: Long): Boolean {
        val statements = range(count, start = 1)
            .map { createUser() }
            .map { "INSERT INTO root.users (id, username, desertion) VALUES ('${it.first}', '${it.second}', '${it.third}');" }
            .map { SimpleStatement(it) }

        return client.executeAwait(BatchStatement().addAll(statements)).wasApplied()
    }

    private fun createUser() = Triple(
        counter.getAndIncrement().toString(),
        UUID.randomUUID().toString(),
        Random.nextLong(999999999999999999L).toString()
    )

}

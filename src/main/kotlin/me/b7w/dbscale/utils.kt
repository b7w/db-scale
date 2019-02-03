package me.b7w.dbscale

import com.datastax.driver.core.Row
import com.datastax.driver.core.Statement
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.ResultSet
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.awaitResult


val <T : Any> T.LOG: Logger
    get() = LoggerFactory.getLogger(this.javaClass)

fun range(count: Long, start: Long = 0) = start.rangeTo(count)


suspend fun PgPool.begin(): PgTransaction {
    return awaitResult {
        this.begin(it)
    }
}

suspend fun PgPool.queryAwait(sql: String): PgRowSet {
    return awaitResult {
        this.query(sql, it)
    }
}

suspend fun PgPool.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
    return awaitResult {
        this.preparedQuery(sql, arguments, it)
    }
}

suspend fun PgTransaction.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
    return awaitResult {
        this.preparedBatch(sql, batch, it)
    }
}

suspend fun PgTransaction.commitAwait() {
    awaitResult<Void> {
        this.commit(it)
    }
}

suspend fun CassandraClient.executeAwait(statement: Statement): ResultSet {
    return awaitResult {
        this.execute(statement, it)
    }
}

suspend fun CassandraClient.executeWithFullFetchAwait(statement: String): List<Row> {
    return awaitResult {
        this.executeWithFullFetch(statement, it)
    }
}

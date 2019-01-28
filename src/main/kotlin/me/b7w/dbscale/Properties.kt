package me.b7w.dbscale

import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.config.ConfigRetriever
import io.vertx.kotlin.config.getConfigAwait

class Properties(val retriever: ConfigRetriever) {

    suspend fun pg(): PgPoolOptions = retriever
        .getConfigAwait()
        .getJsonObject("pg")
        .mapTo(PgPoolOptions::class.java)

    suspend fun cockroach(): PgPoolOptions = retriever
        .getConfigAwait()
        .getJsonObject("cockroach")
        .mapTo(PgPoolOptions::class.java)

}

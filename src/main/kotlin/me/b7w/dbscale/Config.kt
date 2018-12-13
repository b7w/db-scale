package me.b7w.dbscale

import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.config.ConfigRetriever
import io.vertx.kotlin.config.getConfigAwait

class Config(val retriever: ConfigRetriever) {

    suspend fun pg(): PgPoolOptions = retriever
        .getConfigAwait()
        .getJsonObject("pg")
        .mapTo(PgPoolOptions::class.java)
}

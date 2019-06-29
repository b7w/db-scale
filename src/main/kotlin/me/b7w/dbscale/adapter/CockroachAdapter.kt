package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.PgPoolOptions
import me.b7w.dbscale.Properties


class CockroachAdapter(properties: Properties) : PostgresAdapter(properties) {

    override suspend fun name(): String = "cockroach"

    override suspend fun clientOptions(): PgPoolOptions? = properties.cockroach()

}

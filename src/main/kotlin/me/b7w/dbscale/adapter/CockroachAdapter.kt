package me.b7w.dbscale.adapter

import me.b7w.dbscale.Properties


class CockroachAdapter(properties: Properties) : PostgresAdapter(properties) {

    override suspend fun name(): String = "cockroach"

}

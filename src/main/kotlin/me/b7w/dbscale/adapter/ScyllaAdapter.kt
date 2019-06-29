package me.b7w.dbscale.adapter

import me.b7w.dbscale.Properties


open class ScyllaAdapter(properties: Properties) : CassandraAdapter(properties) {

    override suspend fun name(): String = "scylla"

    override suspend fun clientOptions() = properties.scylla()

}

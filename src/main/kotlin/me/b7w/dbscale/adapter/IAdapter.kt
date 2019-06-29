package me.b7w.dbscale.adapter

import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.util.*


interface IAdapter {

    data class Sample(val key: String, val value: JsonObject)

    fun sampleKey() = "sample-key"

    fun <T> T?.getOrFail(): T {
        if (this == null) {
            throw Exception("Client not inited")
        }
        return this
    }

    fun sampleData(key: String = UUID.randomUUID().toString()) = Sample(
        key,
        jsonObjectOf("sample" to UUID.randomUUID().toString())
    )

    suspend fun name(): String

    suspend fun connect(vertx: Vertx)

    suspend fun findOne(): Tuple

    suspend fun removeAll()

}

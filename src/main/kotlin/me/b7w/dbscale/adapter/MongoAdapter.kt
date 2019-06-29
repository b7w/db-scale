package me.b7w.dbscale.adapter

import com.mongodb.MongoWriteException
import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.ext.mongo.*
import me.b7w.dbscale.Properties


open class MongoAdapter(val properties: Properties) : IAdapter {

    private var client: MongoClient? = null

    override suspend fun name(): String = "mongo"

    override suspend fun connect(vertx: Vertx) {
        if (client == null) {
            val options = properties.mongo()
            if (options != null) {
                client = MongoClient.createShared(vertx, options)
                if ("sample" !in client.getOrFail().getCollectionsAwait()) {
                    client.getOrFail().createCollectionAwait("sample")
                }
                val sample = sampleData(sampleKey())
                try {
                    val obj = JsonObject()
                        .put("_id", sample.key)
                        .put("value", sample.value)
                    client.getOrFail().insertAwait("sample", obj)
                } catch (e: MongoWriteException) {
                }
            } else {
                throw Exception("Config not found")
            }
        }
    }

    override suspend fun findOne(): Tuple {
        val id = JsonObject().put("_id", sampleKey())
        val result = client.getOrFail().findOneAwait("sample", id, JsonObject())!!
        return Tuple.of(result.getString("_id"), result.getJsonObject("value").toString())
    }

    override suspend fun removeAll() {
        if ("sample" in client.getOrFail().getCollectionsAwait()) {
            client.getOrFail().dropCollectionAwait("sample")
        }
        client.getOrFail().createCollectionAwait("sample")
    }

}

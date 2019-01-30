package me.b7w.dbscale.verticle

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.IndexOptions
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.MongoClientBulkWriteResult
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.ext.mongo.*
import me.b7w.dbscale.range
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class MongoGenerator(val client: MongoClient, val counter: AtomicInteger) {

    suspend fun createCollection() {
        if ("users" !in client.getCollectionsAwait()) {
            client.createCollectionAwait("users")
            //client.createIndexWithOptionsAwait("users", JsonObject("username" to 1), IndexOptions().unique(true))
            client.createIndexWithOptionsAwait("users", JsonObject("id" to 1), IndexOptions().unique(true))
        }
    }

    suspend fun insertUser(): String {
        val params = createUser()
        return client.insertAwait("users", params)
    }

    suspend fun insertUsers(count: Long): MongoClientBulkWriteResult {
        val params = range(count, start = 1)
            .map { createUser() }
            .map { io.vertx.ext.mongo.BulkOperation.createInsert(it) }

        return client.bulkWriteAwait("users", params)
    }

    suspend fun select(): JsonObject {
        val id = Random.nextInt(counter.get()).toString()
        return client.findOneAwait("users", JsonObject("id" to id), JsonObject())
    }

    suspend fun select(count: Int): JsonObject {
        val id = Random.nextInt(count).toString()
        return client.findOneAwait("users", JsonObject("id" to id), JsonObject())
    }

    suspend fun drop() {
        client.dropCollectionAwait("users")
        createCollection()
        counter.set(1)
    }

    private fun createUser() = JsonObject(
        "id" to counter.getAndIncrement().toString(),
        "username" to UUID.randomUUID().toString(),
        "desertion" to Random.nextLong(999999999999999999L).toString()
    )

}

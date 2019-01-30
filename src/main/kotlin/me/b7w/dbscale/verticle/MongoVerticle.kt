package me.b7w.dbscale.verticle

import io.vertx.core.json.Json
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.mongo.countAwait
import kotlinx.coroutines.launch
import me.b7w.dbscale.Properties
import java.util.concurrent.atomic.AtomicInteger

class MongoVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val options = properties.mongo()

        if (options != null) {
            val client = MongoClient.createShared(vertx, options)

            val generator = MongoGenerator(client, AtomicInteger(0))

            generator.createCollection()

            router.route("/mongo/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = client.countAwait("users", JsonObject())

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/mongo/users/select/").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.select()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/mongo/users/select/:count").handler { context ->
                val c = context.request().getParam("count").toInt()
                launch(vertx.dispatcher()) {
                    val result = generator.select(c)

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/mongo/users/drop").handler { context ->
                launch(vertx.dispatcher()) {
                    generator.drop()
                    context.response().end(Json.encodePrettily(""))
                }
            }

            router.route("/mongo/users/insert").handler { context ->
                launch(vertx.dispatcher()) {
                    val res = generator.insertUser()
                    context.response().end(Json.encodePrettily(res))
                }
            }

            router.route("/mongo/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    val result = generator.insertUsers(count)
                    context.response().end(Json.encodePrettily(result))
                }
            }
        }
    }
}

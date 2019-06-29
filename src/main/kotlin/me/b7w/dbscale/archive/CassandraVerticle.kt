package me.b7w.dbscale.archive

import io.vertx.cassandra.CassandraClient
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.Properties
import java.util.concurrent.atomic.AtomicInteger

class CassandraVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val options = properties.cassandra()

        if (options != null) {
            val client = CassandraClient.createNonShared(vertx, options)
            val generator = CassandraGenerator(client, AtomicInteger(0))

            generator.createKeySpace()
            generator.createTables()

            router.route("/cassandra/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.countUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/cassandra/users/select/").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = CassandraGenerator(client, generator.counter).select()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/cassandra/users/select/:count").handler { context ->
                val c = context.request().getParam("count").toInt()
                launch(vertx.dispatcher()) {
                    val result = CassandraGenerator(client, AtomicInteger(c)).select()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/cassandra/users/truncate").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.truncateUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/cassandra/users/insert").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.insertUser()
                    if (result) {
                        context.response().end("Ok")
                    } else {
                        context.response().setStatusCode(500).end("Error")
                    }
                }
            }

            router.route("/cassandra/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    val result = generator.insertUsers(count)
                    if (result) {
                        context.response().end("Ok")
                    } else {
                        context.response().setStatusCode(500).end("Error")
                    }
                }
            }
        }

    }
}

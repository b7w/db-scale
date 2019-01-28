package me.b7w.dbscale.verticle

import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.Properties
import java.util.concurrent.atomic.AtomicInteger

class PgVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val options = properties.pg()

        val clients = options.host.split(",").map {
            val json = options.toJson().put("host", it)
            PgClient.pool(PgPoolOptions(json))
        }
        val master = PgGenerator(clients.first(), AtomicInteger(0))

        master.createTables()

        router.route("/pg/users/count").handler { context ->
            launch(vertx.dispatcher()) {
                val result = master.countUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/select/").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), master.counter).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/select/:count").handler { context ->
            val c = context.request().getParam("count").toInt()
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), AtomicInteger(c)).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/truncate").handler { context ->
            launch(vertx.dispatcher()) {
                val result = master.truncateUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert").handler { context ->
            launch(vertx.dispatcher()) {
                val result = master.insertUser()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert/:count").handler { context ->
            val count = context.request().getParam("count").toLong()
            launch(vertx.dispatcher()) {
                val (code, msg) = master.insertUsers(count)
                if (code) {
                    context.response().end(Json.encodePrettily(msg))
                } else {
                    context.response().setStatusCode(500).end(msg)
                }
            }
        }
    }
}

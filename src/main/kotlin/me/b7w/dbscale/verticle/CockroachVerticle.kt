package me.b7w.dbscale.verticle

import io.reactiverse.pgclient.PgPool
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class CockroachVerticle(val router: Router, val clients: List<PgPool>) : CoroutineVerticle() {

    val counter = AtomicInteger(1)

    override suspend fun start() {

        router.route("/cockroach/users/count").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).countUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/select/").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/select/:count").handler { context ->
            val c = context.request().getParam("count").toInt()
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), AtomicInteger(c)).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/delete").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).deleteUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/truncate").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).truncateUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/insert").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).insertUser()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/cockroach/users/insert/:count").handler { context ->
            val count = context.request().getParam("count").toLong()
            launch(vertx.dispatcher()) {
                val (code, msg) = PgGenerator(clients.random(), counter).insertUsers(count)
                if (code) {
                    context.response().end(Json.encodePrettily(msg))
                } else {
                    context.response().setStatusCode(500).end(msg)
                }
            }
        }
    }
}

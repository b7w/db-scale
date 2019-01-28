package me.b7w.dbscale.verticle

import io.reactiverse.pgclient.PgPool
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class PgVerticle(val router: Router, val clients: List<PgPool>) : CoroutineVerticle() {

    val counter = AtomicInteger(1)

    override suspend fun start() {

        router.route("/pg/users/count").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.first(), counter).countUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/select/").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.random(), counter).select()

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

        router.route("/pg/users/delete").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.first(), counter).deleteUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/truncate").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.first(), counter).truncateUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert").handler { context ->
            launch(vertx.dispatcher()) {
                val result = PgGenerator(clients.first(), counter).insertUser()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert/:count").handler { context ->
            val count = context.request().getParam("count").toLong()
            launch(vertx.dispatcher()) {
                val (code, msg) = PgGenerator(clients.first(), counter).insertUsers(count)
                if (code) {
                    context.response().end(Json.encodePrettily(msg))
                } else {
                    context.response().setStatusCode(500).end(msg)
                }
            }
        }
    }
}

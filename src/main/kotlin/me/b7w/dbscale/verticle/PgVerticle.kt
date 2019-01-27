package me.b7w.dbscale.verticle

import io.reactiverse.pgclient.PgPool
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.LOG
import java.util.concurrent.atomic.AtomicInteger

class PgVerticle(val router: Router, val clients: List<PgPool>) : CoroutineVerticle() {

    val counter = AtomicInteger(1)

    override suspend fun start() {

        router.route("/pg/users/count").handler { context ->
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/count")
                val result = PgGenerator(clients.random(), counter).countUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/select/").handler { context ->
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/count/:count")
                val result = PgGenerator(clients.random(), counter).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/select/:count").handler { context ->
            val c = context.request().getParam("count").toInt()
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/count/:count")
                val result = PgGenerator(clients.random(), AtomicInteger(c)).select()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/delete").handler { context ->
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/delete")
                val result = PgGenerator(clients.first(), counter).deleteUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/truncate").handler { context ->
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/delete")
                val result = PgGenerator(clients.first(), counter).truncateUsers()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert").handler { context ->
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/insert")
                val result = PgGenerator(clients.random(), counter).insertUser()

                context.response().end(Json.encodePrettily(result))
            }
        }

        router.route("/pg/users/insert/:count").handler { context ->
            val count = context.request().getParam("count").toLong()
            launch(vertx.dispatcher()) {
                LOG.trace("/pg/users/insert/:count")
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

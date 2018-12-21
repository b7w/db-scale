import io.reactiverse.pgclient.PgClient
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.b7w.dbscale.Config
import me.b7w.dbscale.LOG
import me.b7w.dbscale.PgDataLoaderNew


fun main(args: Array<String>) {
    println("Hello, World")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    val vertx = Vertx.vertx()
    val config = Config(ConfigRetriever.create(vertx))

    val options = runBlocking(vertx.dispatcher()) {
        config.pg()
    }
    val pgPool = PgClient.pool(options)

    class HttpServerVerticle : CoroutineVerticle() {
        override suspend fun start() {
            val router = Router.router(vertx)

            router.route("/pg/users/select").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/select")
                    val result = PgDataLoaderNew(pgPool).select()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/count")
                    val result = PgDataLoaderNew(pgPool).countUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/delete").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/delete")
                    val result = PgDataLoaderNew(pgPool).deleteUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/insert").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/insert")
                    val result = PgDataLoaderNew(pgPool).insertUser()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/insert/:count")
                    val (code, msg) = PgDataLoaderNew(pgPool).insertUsers(count)
                    if (code) {
                        context.response().end(Json.encodePrettily(msg))
                    } else {
                        context.response().setStatusCode(500).end(msg)
                    }
                }
            }

            vertx.createHttpServer().requestHandler(router).listen(8080)
        }
    }
    vertx.deployVerticle(HttpServerVerticle())

}


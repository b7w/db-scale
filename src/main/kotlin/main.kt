import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.LOG
import me.b7w.dbscale.PgDataLoader


fun main(args: Array<String>) {
    println("Hello, World")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    val vertx = Vertx.vertx()
    val pgClient = PostgreSQLClient.createShared(
        vertx, JsonObject(
            "host" to "rc1a-bfb85etuy0q2tg34.mdb.yandexcloud.net",
            "port" to 6432,
            "database" to "root",
            "username" to "root",
            "password" to "q1w2e3r4",
            "maxPoolSize" to 36,
            "sslMode" to "verify-full",
            "sslRootCert" to "/Users/B7W/.postgresql/root.crt"
        )
    )
    pgClient.LOG.info("Done PG client setup")

    class HttpServerVerticle : CoroutineVerticle() {
        override suspend fun start() {
            val router = Router.router(vertx)

            router.route("/pg/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.info("/pg/users/count")
                    val result = PgDataLoader(pgClient).countUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/delete").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.info("/pg/users/delete")
                    val result = PgDataLoader(pgClient).deleteUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    LOG.info("/pg/users/insert/:count")
                    val loader = PgDataLoader(pgClient)
                    val result = loader.insertUsers(count)

                    val response = context.response()
                    response.putHeader("changelog-type", "text/plain")
                    response.end(Json.encodePrettily(result))
                }
            }

            vertx.createHttpServer().requestHandler(router).listen(8080)
        }
    }
    vertx.deployVerticle(HttpServerVerticle())

}


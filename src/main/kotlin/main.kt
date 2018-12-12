import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.PgDataLoader


fun main(args: Array<String>) {
    println("Hello, World")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    val vertx = Vertx.vertx()
    val pgClient = PostgreSQLClient.createShared(
        vertx, JsonObject(
            "host" to "localhost",
            "database" to "root",
            "username" to "root",
            "password" to "root"
        )
    )

    class HttpServerVerticle : CoroutineVerticle() {
        override suspend fun start() {
            val router = Router.router(vertx)

            router.route("/pg/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
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


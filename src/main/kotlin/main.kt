import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.PgDataLoader


fun main(args: Array<String>) {
    println("Hello, World")

    val vertx = Vertx.vertx()

    class HttpServerVerticle : CoroutineVerticle() {
        override suspend fun start() {
            val router = Router.router(vertx)

            router.route("/pg/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    val loader = PgDataLoader(count)
                    val result = loader.load()

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


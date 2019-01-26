import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.b7w.dbscale.Config
import me.b7w.dbscale.PgDataLoaderNew
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val LOG = LoggerFactory.getLogger("main")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    val vertx = Vertx.vertx()
    val config = Config(ConfigRetriever.create(vertx))
    val background = vertx.createSharedWorkerExecutor("background", 2)

    val options = runBlocking(vertx.dispatcher()) {
        config.pg()
    }
    var usersCache = listOf<String>()

    background.executeBlocking<Unit>({
        LOG.info("Start cache users")
        val time = measureTimeMillis {
            usersCache = config.usersCache()
                .readLines()
        }
        LOG.info("Cache ${usersCache.size} users in ${time}ms")
    }, {
        LOG.info("Stop cache users")
    })


    val clients = options.host.split(",").map {
        val json = options.toJson().put("host", it)
        PgClient.pool(PgPoolOptions(json))
    }

    class HttpServerVerticle : CoroutineVerticle() {
        override suspend fun start() {
            val router = Router.router(vertx)

            router.route("/pg/users/select").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/select")
                    val result = PgDataLoaderNew(clients.random()).select(usersCache)

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/count")
                    val result = PgDataLoaderNew(clients.random()).countUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/delete").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/delete")
                    val result = PgDataLoaderNew(clients.first()).deleteUsers()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/insert").handler { context ->
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/insert")
                    val result = PgDataLoaderNew(clients.first()).insertUser()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/pg/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    LOG.trace("/pg/users/insert/:count")
                    val (code, msg) = PgDataLoaderNew(clients.first()).insertUsers(count)
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


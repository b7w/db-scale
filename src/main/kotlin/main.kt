import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import me.b7w.dbscale.Config
import me.b7w.dbscale.verticle.PgGenerator
import me.b7w.dbscale.verticle.PgVerticle
import java.util.concurrent.atomic.AtomicInteger
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
            if (config.usersCache().exists()) {
                usersCache = config.usersCache().readLines()
            }
        }
        LOG.info("Cache ${usersCache.size} users in ${time}ms")
    }, {
        LOG.info("Stop cache users")
    })


    val clients = options.host.split(",").map {
        val json = options.toJson().put("host", it)
        PgClient.pool(PgPoolOptions(json))
    }

    background.executeBlocking<Unit>({
        runBlocking(vertx.dispatcher()) {
            PgGenerator(clients.first(), AtomicInteger()).createTables()
        }
        clients.forEach {
            runBlocking(vertx.dispatcher()) {
                val count = PgGenerator(it, AtomicInteger()).countUsers()
                LOG.info("Users count on ${it} is ${Json.encodePrettily(count)}")
            }
        }
    }, {})

    val router = Router.router(vertx)

    vertx.deployVerticle(PgVerticle(router, clients))
    vertx.createHttpServer().requestHandler(router).listen(8080)

}


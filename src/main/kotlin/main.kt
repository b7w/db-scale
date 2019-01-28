import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import me.b7w.dbscale.Properties
import me.b7w.dbscale.verticle.CockroachVerticle
import me.b7w.dbscale.verticle.PgVerticle


fun main(args: Array<String>) {
    val LOG = LoggerFactory.getLogger("main")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    val vertx = Vertx.vertx()
    val config = Properties(ConfigRetriever.create(vertx))

    val router = Router.router(vertx)

    vertx.deployVerticle(PgVerticle(config, router))
    vertx.deployVerticle(CockroachVerticle(config, router))
    vertx.createHttpServer().requestHandler(router).listen(8080)

}


import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import me.b7w.dbscale.Properties
import me.b7w.dbscale.verticle.AdapterVerticle


fun main(args: Array<String>) {
    System.setProperty("javax.net.ssl.trustStore", "etc/keystore.jks")
    System.setProperty("javax.net.ssl.trustStorePassword", "q1w2e3r4")
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    val LOG = LoggerFactory.getLogger("main")

    val vertx = Vertx.vertx()
    val config = Properties(ConfigRetriever.create(vertx))

    val router = Router.router(vertx)

    vertx.deployVerticle(AdapterVerticle(config, router))
//    vertx.deployVerticle(CockroachVerticle(config, router))
//    vertx.deployVerticle(CockroachVerticle(config, router))
//    vertx.deployVerticle(ClickHouseVerticle(config, router))
//    vertx.deployVerticle(CassandraVerticle(config, router))
//    vertx.deployVerticle(RedisVerticle(config, router))
    vertx.createHttpServer().requestHandler(router).listen(8080)
}


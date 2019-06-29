package me.b7w.dbscale.verticle

import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.LOG
import me.b7w.dbscale.Properties
import me.b7w.dbscale.adapter.*


class AdapterVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val adapters: Map<String, IAdapter> = listOf(
            PostgresAdapter(properties),
            CockroachAdapter(properties),
            CassandraAdapter(properties),
            MongoAdapter(properties),
            ScyllaAdapter(properties),
            RedisAdapter(properties),
            ClickHouseAdapter(properties)
        ).map { it.name() to it }.toMap()

        router.route("/:adapter/find-one").handler { context ->
            launch(vertx.dispatcher()) {
                val name = context.request().getParam("adapter")
                try {
                    val adapter = adapters.getOrElse(name) { throw Exception("No adapter found") }
                    adapter.connect(vertx)
                    val result = adapter.findOne()
                    context.response().end(result.toString())
                } catch (e: Exception) {
                    LOG.error(e.message, e)
                    context.response().end(e.message)
                }
            }
        }

        router.route("/:adapter/remove-all").handler { context ->
            launch(vertx.dispatcher()) {
                val name = context.request().getParam("adapter")
                try {
                    val adapter = adapters.getOrElse(name) { throw Exception("No adapter found") }
                    adapter.connect(vertx)
                    adapter.removeAll()
                    context.response().end("Ok")
                } catch (e: Exception) {
                    LOG.error(e.message, e)
                    context.response().end(e.message)
                }
            }
        }

    }
}

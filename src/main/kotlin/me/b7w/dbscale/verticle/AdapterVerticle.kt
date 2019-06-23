package me.b7w.dbscale.verticle

import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import me.b7w.dbscale.Properties
import me.b7w.dbscale.adapter.CockroachAdapter
import me.b7w.dbscale.adapter.IAdapter
import me.b7w.dbscale.adapter.PostgresAdapter
import me.b7w.dbscale.adapter.RedisAdapter


class AdapterVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val adapters: Map<String, IAdapter> = listOf(
            PostgresAdapter(properties),
            CockroachAdapter(properties),
            RedisAdapter(properties)
        ).map { it.name() to it }.toMap()

        router.route("/:adapter/find-one").handler { context ->
            launch(vertx.dispatcher()) {
                val name = context.request().getParam("adapter")
                try {
                    val adapter = adapters.get(name)!!
                    adapter.connect(vertx)
                    val result = adapter.findOne()
                    context.response().end(result.toString())
                } catch (e: Exception) {
                    context.response().end(e.message)
                }
            }
        }

        router.route("/:adapter/remove-all").handler { context ->
            launch(vertx.dispatcher()) {
                val name = context.request().getParam("adapter")
                try {
                    val adapter = adapters.get(name)!!
                    adapter.connect(vertx)
                    val result = adapter.removeAll()
                    context.response().end(result.toString())
                } catch (e: Exception) {
                    context.response().end(e.message)
                }
            }
        }

    }
}

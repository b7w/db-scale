package me.b7w.dbscale.verticle

import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.launch
import me.b7w.dbscale.LOG
import me.b7w.dbscale.Properties


class ClickHouseVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    override suspend fun start() {

        val options = properties.clickhouse()

        if (options != null) {

            val opt = WebClientOptions()
                .setSsl(true)
            val client = WebClient.create(vertx, opt)

            router.route("/clickhouse/users/ping").handler { context ->
                launch(vertx.dispatcher()) {
                    try {
                        val response = client
                            .getAbs(options.url)
                            .addQueryParam("database", options.database)
                            .addQueryParam("query", "SELECT now()")
                            .putHeader("X-ClickHouse-User", options.username)
                            .putHeader("X-ClickHouse-Key", options.password)
                            .sendAwait()
                        println(response.headers())
                        context.response().end(response.bodyAsString())
                    } catch (e: Exception) {
                        LOG.error("Er", e)
                        context.response().end(Json.encodePrettily(e.message))
                    }
                }
            }

        }

    }
}

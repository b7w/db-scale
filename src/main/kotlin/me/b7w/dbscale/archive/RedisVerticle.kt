package me.b7w.dbscale.archive

import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.redis.client.connectAwait
import io.vertx.kotlin.redis.client.getAwait
import io.vertx.kotlin.redis.client.setAwait
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.b7w.dbscale.Properties
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet

class RedisVerticle(val properties: Properties, val router: Router) : CoroutineVerticle() {

    val threadLocal = ThreadLocal<RedisAPI>()

    suspend fun client(options: RedisOptions) = coroutineScope {
        threadLocal.getOrSet {
            val pool = Redis.createClient(vertx, options).connectAwait()
            RedisAPI.api(pool)
        }
    }

    override suspend fun start() {

        val options = properties.redis()

        if (options != null) {
            withContext(threadLocal.asContextElement()) {
                val client = client(options)
                client.setAwait(listOf("sample-key", "{\"message\": \"Hello World\"}"))
            }

            val generator = RedisGenerator(client(options), AtomicInteger(0))

            router.route("/redis/users/count").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.count()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/redis/users/select/").handler { context ->
                launch(vertx.dispatcher()) {
                    val result = generator.select()

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/redis/users/select/sample-key").handler { context ->
                launch(vertx.dispatcher()) {
                    val client = client(options)
                    val result = client.getAwait("sample-key").toString()
                    context.response().putHeader("Connection", "keep-alive")
                    context.response().putHeader("Content-Type", "text/plain; charset=utf-8")
                    context.response().putHeader("Keep-Alive", "5")
                    context.response().end(result)
                }
            }

            router.route("/redis/users/select/:count").handler { context ->
                val c = context.request().getParam("count").toInt()
                launch(vertx.dispatcher()) {
                    val result = generator.select(c)

                    context.response().end(Json.encodePrettily(result))
                }
            }

            router.route("/redis/users/drop").handler { context ->
                launch(vertx.dispatcher()) {
                    generator.drop()
                    context.response().end(Json.encodePrettily(""))
                }
            }

            router.route("/redis/users/insert").handler { context ->
                launch(vertx.dispatcher()) {
                    val res = generator.insertUser()
                    context.response().end(Json.encodePrettily(res))
                }
            }

            router.route("/redis/users/insert/:count").handler { context ->
                val count = context.request().getParam("count").toLong()
                launch(vertx.dispatcher()) {
                    val result = generator.insertUsers(count)
                    context.response().end(Json.encodePrettily(result))
                }
            }
        }
    }
}

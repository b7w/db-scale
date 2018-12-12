package me.b7w.dbscale

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory


val <T : Any> T.LOG: Logger
    get() = LoggerFactory.getLogger(this.javaClass)

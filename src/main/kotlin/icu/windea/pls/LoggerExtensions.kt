@file:Suppress("unused")

package icu.windea.pls

import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val loggerCache = createCache<Class<*>, Logger> { LoggerFactory.getLogger(it) }

fun Any.logger(): Logger = loggerCache.get(this.javaClass)

inline fun Logger.trace(lazyMessage: () -> String) {
	this.trace(lazyMessage())
}

inline fun Logger.trace(e: Throwable, lazyMessage: () -> String) {
	this.trace(lazyMessage(), e)
}

inline fun Logger.debug(lazyMessage: () -> String) {
	this.debug(lazyMessage())
}

inline fun Logger.debug(e: Throwable, lazyMessage: () -> String) {
	this.debug(lazyMessage(), e)
}

inline fun Logger.info(lazyMessage: () -> String) {
	this.info(lazyMessage())
}

inline fun Logger.info(e: Throwable, lazyMessage: () -> String) {
	this.debug(lazyMessage(), e)
}

inline fun Logger.warn(lazyMessage: () -> String) {
	this.warn(lazyMessage())
}

inline fun Logger.warn(e: Throwable, lazyMessage: () -> String) {
	this.warn(lazyMessage(), e)
}

inline fun Logger.error(lazyMessage: () -> String) {
	this.error(lazyMessage())
}

inline fun Logger.error(e: Throwable, lazyMessage: () -> String) {
	this.error(lazyMessage(), e)
}
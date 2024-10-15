package icu.windea.pls.core

import java.math.*
import java.util.concurrent.*

val isDebug = System.getProperty("pls.is.debug").toBoolean()

val avgMillisMap = ConcurrentHashMap<String, Double>()

inline fun <T> withMeasureMillis(id: String, min: Int = -1, action: () -> T): T {
    return withMeasureMillis({ id }, min, action)
}

inline fun <T> withMeasureMillis(idProvider: () -> String, min: Int = -1, action: () -> T): T {
    if (!isDebug) return action()
    val start = System.currentTimeMillis()
    try {
        return action()
    } finally {
        val id = idProvider()
        val end = System.currentTimeMillis()
        val millis = end - start
        val avgMillis = avgMillisMap.compute(id) { _, v ->
            if (v == null) millis.toDouble() else (v + millis) / 2.0
        } ?: 0.0
        if (millis > min) {
            val avg = BigDecimal(avgMillis).setScale(10, RoundingMode.HALF_UP)
            println("${id} - cur: $millis, avg: $avg")
        }
    }
}


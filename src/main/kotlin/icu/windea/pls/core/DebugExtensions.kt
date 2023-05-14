package icu.windea.pls.core

import java.math.*
import java.util.concurrent.*

val isDebug = System.getProperty("pls.is.debug").toBoolean()

val avgMillisMap = ConcurrentHashMap<String, BigDecimal>()

inline fun <T> withMeasureMillis(id: String, min: Int = -1, action: () -> T): T {
    return withMeasureMillis({ id }, min, action)
}

inline fun <T> withMeasureMillis(idProvider: () -> String, min: Int = -1, action: () -> T): T {
    if(!isDebug) return action()
    val start = System.currentTimeMillis()
    try {
        return action()
    } finally {
        val id = idProvider()
        InternalExtensionsHolder.doPrintMeasureMillis(start, id, min)
    }
}

@Suppress("UnusedReceiverParameter")
fun InternalExtensionsHolder.doPrintMeasureMillis(start: Long, id: String, min: Int) {
    val end = System.currentTimeMillis()
    val millis = end - start
    val avgMillis = avgMillisMap.compute(id) { _, v ->
        if(v == null) millis.toBigDecimal() else v + millis.toBigDecimal()
    }
    if(millis > min) println("${id} - cur: $millis, avg: ${avgMillis?.setScale(10)}")
}

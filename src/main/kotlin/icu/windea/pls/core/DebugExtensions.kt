@file:Suppress("unused")

package icu.windea.pls.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap

/** 是否启用调试模式（读取系统属性 `pls.is.debug`）。 */
val isDebug = System.getProperty("pls.is.debug").toBoolean()

/** 调试用：记录各标识的平均耗时（毫秒）。 */
val avgMillisMap = ConcurrentHashMap<String, Double>()

/** 统计并打印执行耗时（毫秒），便于调试；仅在调试模式下生效。 */
inline fun <T> withMeasureMillis(id: String, min: Int = -1, action: () -> T): T {
    return withMeasureMillis({ id }, min, action)
}

/** 统计并打印执行耗时（毫秒），标识由[idProvider]提供；仅在调试模式下生效。 */
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


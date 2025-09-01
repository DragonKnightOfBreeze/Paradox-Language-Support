@file:Suppress("unused")

package icu.windea.pls.core

/**
 * 安全地串联比较器：当且仅当两者都不为空时返回 `this.then(comparator)`，否则返回非空者或 null。
 */
infix fun <T> Comparator<T>?.thenPossible(comparator: Comparator<T>?): Comparator<T>? {
    if (this == null || comparator == null) return this ?: comparator
    return this.then(comparator)
}

/**
 * 复合排序：
 * - 先用 [selector] 提取目标（null 排到最后）
 * - 再按 [comparableSelector] 正序比较（为 null 时返回 0）
 * - 最后将 [pinPredicate] 命中的元素置顶（多个命中时相互视为相等）
 */
inline fun <T, R, C : Comparable<C>> complexCompareBy(
    crossinline selector: (T) -> R?,
    crossinline comparableSelector: (R) -> C? = { null },
    crossinline pinPredicate: (R) -> Boolean = { false }
): java.util.Comparator<T> {
    return Comparator { a, b ->
        val a1 = selector(a)
        val b1 = selector(b)
        when {
            a1 == b1 -> 0 // requires chained comparators
            a1 == null -> 1
            b1 == null -> -1
            pinPredicate(b1) -> if (pinPredicate(a1)) 0 else 1
            pinPredicate(a1) -> -1
            else -> {
                val a2 = comparableSelector(a1) ?: return@Comparator 0
                val b2 = comparableSelector(b1) ?: return@Comparator 0
                a2.compareTo(b2)
            }
        }
    }
}

/**
 * 复合排序（降序）：
 * - 先用 [selector] 提取目标（null 排到最后）
 * - 再按 [comparableSelector] 降序比较（为 null 时返回 0）
 * - 最后将 [pinPredicate] 命中的元素置顶（多个命中时相互视为相等）
 */
inline fun <T, R, C : Comparable<C>> complexCompareByDescending(
    crossinline selector: (T) -> R?,
    crossinline comparableSelector: (R) -> C? = { null },
    crossinline pinPredicate: (R) -> Boolean = { false }
): java.util.Comparator<T> {
    return Comparator { a, b ->
        val a1 = selector(a)
        val b1 = selector(b)
        when {
            a1 == b1 -> 0 // requires chained comparators
            a1 == null -> 1
            b1 == null -> -1
            pinPredicate(b1) -> if (pinPredicate(a1)) 0 else 1
            pinPredicate(a1) -> -1
            else -> {
                val a2 = comparableSelector(a1) ?: return@Comparator 0
                val b2 = comparableSelector(b1) ?: return@Comparator 0
                b2.compareTo(a2)
            }
        }
    }
}

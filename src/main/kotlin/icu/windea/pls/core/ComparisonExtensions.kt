@file:Suppress("unused")

package icu.windea.pls.core

/**
 * 可能的比较器链。
 *
 * - 如果两个比较器都为 `null`，返回 `null`；
 * - 如果其中一个比较器为 `null`，返回另一个比较器；
 * - 否则，返回两个比较器的链式比较器。
 */
infix fun <T> Comparator<T>?.thenPossible(comparator: Comparator<T>?): Comparator<T>? {
    if (this == null || comparator == null) return this ?: comparator
    return this.then(comparator)
}

/**
 * 复杂排序（正序）。
 *
 * - 先用 [selector] 提取比较对象，`null` 放到最后；
 * - 再用 [comparableSelector] 比较（若为 `null` 返回 0，不改变当前顺序）；
 * - 最后按 [pinPredicate] 置顶（多个命中时返回 0，不改变相对顺序）；
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
 * 复杂排序（降序）。
 *
 * 规则与 [complexCompareBy] 一致，但比较结果取反（降序）。
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

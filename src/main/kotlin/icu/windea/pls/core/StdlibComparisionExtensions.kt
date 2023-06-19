package icu.windea.pls.core

infix fun <T> Comparator<T>?.thenPossible(comparator: Comparator<T>?): Comparator<T>? {
    if(this == null || comparator == null) return this ?: comparator
    return this.then(comparator)
}

/**
 * 通过[selector]得到需要的结果之后（如果这里得到的结果是null，放到最后面），
 * 首先按照[comparableSelector]的结果进行降序排序（如果这里得到的结果是null，比较结果返回0），
 * 然后按照[pinPredicate]的结果置顶匹配的元素（如果存在多个匹配的元素，比较结果返回0）。
 */
inline fun <T, R, C : Comparable<C>> complexCompareByDescending(
    crossinline selector: (T) -> R?,
    crossinline comparableSelector: (R) -> C? = { null },
    crossinline pinPredicate: (R) -> Boolean = { false }
): java.util.Comparator<T> {
    return Comparator<T> { a, b ->
        val a1 = selector(a)
        val b1 = selector(b)
        when {
            a1 == b1 -> 1
            a1 == null -> 1
            b1 == null -> -1
            pinPredicate(b1) -> if(pinPredicate(a1)) 0 else 1
            pinPredicate(a1) -> -1
            else -> {
                val a2 = comparableSelector(a1) ?: return@Comparator 0
                val b2 = comparableSelector(b1) ?: return@Comparator 0
                b2.compareTo(a2)
            }
        }
    }
}

/**
 * 通过[selector]得到需要的结果之后（如果这里得到的结果是null，放到最后面），
 * 首先按照[comparableSelector]的结果进行正序排序（如果这里得到的结果是null，比较结果返回0），
 * 然后按照[pinPredicate]的结果置顶匹配的元素（如果存在多个匹配的元素，比较结果返回0）。
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
            a1 == b1 -> 0
            a1 == null -> 1
            b1 == null -> -1
            pinPredicate(b1) -> if(pinPredicate(a1)) 0 else 1
            pinPredicate(a1) -> -1
            else -> {
                val a2 = comparableSelector(a1) ?: return@Comparator 0
                val b2 = comparableSelector(b1) ?: return@Comparator 0
                a2.compareTo(b2)
            }
        }
    }
}
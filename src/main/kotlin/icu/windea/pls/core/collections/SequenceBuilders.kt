package icu.windea.pls.core.collections

/**
 * 生成一个序列，初始元素是 [initial]，下一个元素通过在上一个元素上应用 [next] 生成。
 */
fun <T : Any> generateFoldSequence(initial: T?, next: (T) -> T?): Sequence<T> {
    if (initial == null) return emptySequence()
    return sequence {
        var current = initial
        while (current != null) {
            yield(current)
            current = next(current)
        }
    }
}

/**
 * 生成一个序列，初始元素是 [initial]，下一组元素通过在上一个元素上应用 [next] 生成。
 *
 * 使用广度优先遍历。
 */
fun <T : Any> generateFoldAllSequence(initial: T?, next: (T) -> Collection<T>): Sequence<T> {
    if (initial == null) return emptySequence()
    return sequence {
        val queue = ArrayDeque<T>()
        queue.add(initial)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            yield(current)
            queue.addAll(next(current))
        }
    }
}

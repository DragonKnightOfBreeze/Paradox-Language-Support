@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

/**
 * 生成一个序列，初始元素是 [seed]，下一组元素通过在上一个元素上应用 [nextFunction] 生成。
 *
 * 使用广度优先遍历。
 */
fun <T : Any> generateBreathFirstSequence(seed: T?, nextFunction: (T) -> Collection<T>): Sequence<T> {
    if (seed == null) return emptySequence()
    return sequence {
        val queue = ArrayDeque<T>()
        queue.add(seed)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            yield(current)
            queue.addAll(nextFunction(current))
        }
    }
}

/**
 * 生成一个序列，初始元素是 [seed]，下一组元素通过在上一个元素上应用 [nextFunction] 生成。
 *
 * 使用深度优先遍历（前序遍历）。
 */
fun <T : Any> generateDepthFirstSequence(seed: T?, nextFunction: (T) -> Collection<T>): Sequence<T> {
    if (seed == null) return emptySequence()
    return sequence {
        yield(seed)
        nextFunction(seed).forEach {
            yieldAll(generateDepthFirstSequence(it, nextFunction))
        }
    }
}

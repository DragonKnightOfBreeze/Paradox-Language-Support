@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import com.intellij.util.containers.TreeTraversal

/**
 * 生成一个序列，初始元素是 [seed]，下一组元素通过在上一个元素上应用 [nextFunction] 生成。
 *
 * 通过 [traversal] 指定遍历策略。
 */
fun <T : Any> generateSequence(traversal: TreeTraversal, seed: T?, nextFunction: (T) -> Iterable<T>): Sequence<T> {
    if (seed == null) return emptySequence()
    return traversal.traversal(seed, nextFunction).asSequence()
}

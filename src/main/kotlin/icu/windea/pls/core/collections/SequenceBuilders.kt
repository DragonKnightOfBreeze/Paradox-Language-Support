@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import com.intellij.util.containers.TreeTraversal

/**
 * 生成一个序列，初始元素是 [seed]，下一组元素通过在上一个元素上应用 [nextFunction] 生成。
 *
 * 通过 [traversal] 指定遍历策略。
 */
fun <T : Any> generateSequenceFromSeed(traversal: TreeTraversal, seed: T?, nextFunction: (T) -> Iterable<T>): Sequence<T> {
    if (seed == null) return emptySequence()
    return traversal.traversal(seed, nextFunction).asSequence()
}

/**
 * 生成一个序列，初始元素来自 [seeds]，下一组元素通过在上一个元素上应用 [nextFunction] 生成。
 *
 * 通过 [traversal] 指定遍历策略。
 */
fun <T : Any> generateSequenceFromSeeds(traversal: TreeTraversal, seeds: Iterable<T>?, nextFunction: (T) -> Iterable<T>): Sequence<T> {
    if (seeds == null) return emptySequence()
    if (seeds is Collection && seeds.isEmpty()) return emptySequence()
    return traversal.traversal(seeds, nextFunction).asSequence()
}

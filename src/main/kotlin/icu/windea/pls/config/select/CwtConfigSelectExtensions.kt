@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.properties
import icu.windea.pls.config.values
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.model.paths.CwtConfigPath

// region Common

context(scope: CwtConfigSelectScope)
inline fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

context(scope: CwtConfigSelectScope)
inline fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

// endregion

// region Walks

context(scope: CwtConfigSelectScope)
fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>> {
    return generateSequence(this) { it.parentConfig }
}

context(scope: CwtConfigSelectScope)
fun CwtMemberContainerConfig<*>.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS): Sequence<CwtMemberConfig<*>> {
    return generateSequenceFromSeeds(traversal, configs) { it.configs.orEmpty() }
}

// endregion

// region Casts

context(scope: CwtConfigSelectScope)
fun CwtMemberConfig<*>?.asProperty(): CwtPropertyConfig? {
    return this?.castOrNull()
}

context(scope: CwtConfigSelectScope)
fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig> {
    return filterIsInstance<CwtPropertyConfig>()
}

context(scope: CwtConfigSelectScope)
fun CwtMemberConfig<*>?.asValue(): CwtValueConfig? {
    return this?.castOrNull()
}

context(scope: CwtConfigSelectScope)
fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig> {
    return filterIsInstance<CwtValueConfig>()
}

context(scope: CwtConfigSelectScope)
fun CwtMemberConfig<*>?.asBlock(): CwtValueConfig? {
    return this?.takeIf { it.configs != null }?.castOrNull()
}

context(scope: CwtConfigSelectScope)
fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig> {
    return filter { it.configs != null }.filterIsInstance<CwtValueConfig>()
}

// endregion

// region Queries

context(scope: CwtConfigSelectScope)
fun CwtMemberConfig<*>.selectLiteralValue(): String? {
    return if (configs == null) value else null
}

context(scope: CwtConfigSelectScope)
fun CwtPropertyConfig.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    if (key.isEmpty()) return null
    return takeIf { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    if (key.isEmpty()) return emptySequence()
    return filter { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
fun CwtPropertyConfig.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    return takeIf { keys.any { key -> PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    return filter { keys.any { key -> PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
fun <T : CwtMemberConfig<*>> T.ofValue(value: String, ignoreCase: Boolean = true): T? {
    return takeIf { selectLiteralValue().equals(value, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectLiteralValue().equals(value, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
fun <T : CwtMemberConfig<*>> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T? {
    return takeIf { it.selectLiteralValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

context(scope: CwtConfigSelectScope)
fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectLiteralValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
fun CwtMemberContainerConfig<*>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return ofPathInternal(path, ignoreCase, usePattern)
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { it.ofPathInternal(path, ignoreCase, usePattern) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
fun CwtMemberContainerConfig<*>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
private fun CwtMemberContainerConfig<*>.ofPathInternal(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
    ProgressManager.checkCanceled()
    if (path.isEmpty()) return emptySequence()
    var current: Sequence<CwtMemberConfig<*>>? = null
    val expect = CwtConfigPath.resolve(path)
    for (subPath in expect) {
        ProgressManager.checkCanceled()
        if (current == null) {
            current = when (subPath) {
                "-" -> values()
                else -> properties().ofKey(subPath, ignoreCase, usePattern)
            }
        } else {
            current = when (subPath) {
                "-" -> current.flatMap { it.values() }
                else -> current.flatMap { it.properties().ofKey(subPath, ignoreCase, usePattern) }
            }
        }
    }
    return current.orEmpty()
}

// endregion

@file:Suppress("unused")

package icu.windea.pls.config.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.properties
import icu.windea.pls.config.selectValue
import icu.windea.pls.config.values
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.model.paths.CwtConfigPath

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>> {
    return generateSequence(this) { it.parentConfig }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS): Sequence<CwtMemberConfig<*>> {
    return generateSequenceFromSeeds(traversal, configs) { it.configs.orEmpty() }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>?.asProperty(): CwtPropertyConfig? {
    return this?.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig> {
    return filterIsInstance<CwtPropertyConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>?.asValue(): CwtValueConfig? {
    return this?.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig> {
    return filterIsInstance<CwtValueConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>?.asBlock(): CwtValueConfig? {
    return this?.takeIf { it.configs != null }?.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig> {
    return filter { it.configs != null }.filterIsInstance<CwtValueConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    if (key.isEmpty()) return this
    return takeIf { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    if (key.isEmpty()) return this
    return filter { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    return takeIf { keys.any { key -> key.isEmpty() || PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    return filter { keys.any { key -> key.isEmpty() || PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>> T.ofValue(value: String, ignoreCase: Boolean = true): T? {
    return takeIf { selectValue().equals(value, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectValue().equals(value, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T? {
    return takeIf { it.selectValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return ofPathInternal(path, ignoreCase, usePattern)
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { it.ofPathInternal(path, ignoreCase, usePattern) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
private fun CwtMemberContainerConfig<*>.ofPathInternal(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
    ProgressManager.checkCanceled()
    if (path.isEmpty()) {
        if (this is CwtMemberConfig<*>) return sequenceOf(this)
        throw UnsupportedOperationException()
    }
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


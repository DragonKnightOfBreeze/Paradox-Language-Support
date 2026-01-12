@file:Suppress("unused")

package icu.windea.pls.config.select

import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.collections.orNull
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
fun CwtMemberContainerConfig<*>.members(): Sequence<CwtMemberConfig<*>> {
    return configs?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.values(): Sequence<CwtMemberConfig<*>> {
    return values?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.properties(): Sequence<CwtPropertyConfig> {
    return properties?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.asProperty(): CwtPropertyConfig? {
    return this.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig> {
    return filterIsInstance<CwtPropertyConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.asValue(): CwtValueConfig? {
    return this.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig> {
    return filterIsInstance<CwtValueConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.asBlock(): CwtValueConfig? {
    return takeIf { it.configs != null }?.castOrNull()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig> {
    return filter { it.configs != null }.filterIsInstance<CwtValueConfig>()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofName(name: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    return takeIf { PathMatcher.matches(it.key, name, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofName(name: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    return filter { PathMatcher.matches(it.key, name, ignoreCase, usePattern) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofNames(names: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig? {
    return takeIf { names.any { name -> PathMatcher.matches(it.key, name, ignoreCase, usePattern) } }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofNames(names: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig> {
    return filter { names.any { name -> PathMatcher.matches(it.key, name, ignoreCase, usePattern) } }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    val subPaths = CwtConfigPath.resolve(path).subPaths
    if (subPaths.isEmpty()) return emptySequence()
    var current: Sequence<CwtMemberConfig<*>>? = null
    for (subPath in subPaths) {
        if (current == null) {
            current = when (subPath) {
                "-" -> values()
                else -> properties().ofName(subPath, ignoreCase, usePattern)
            }
        } else {
            current = when (subPath) {
                "-" -> current.flatMap { it.values() }
                else -> current.flatMap { it.properties().ofName(subPath, ignoreCase, usePattern) }
            }
        }
    }
    return current.orEmpty()
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { it.ofPath(path, ignoreCase) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberContainerConfig<*>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return paths.asSequence().flatMap { path -> ofPath(path, ignoreCase) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>> {
    return flatMap { it.ofPaths(paths, ignoreCase) }
}

@file:Suppress("unused")

package icu.windea.pls.config.select

import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequence
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.model.paths.CwtConfigPath

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>> {
    return generateSequence(this) { it.parentConfig }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS): Sequence<CwtMemberConfig<*>> {
    return generateSequence(traversal, this) { it.configs.orEmpty() }
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
fun CwtMemberConfig<*>.members(): Sequence<CwtMemberConfig<*>> {
    return configs?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.values(): Sequence<CwtMemberConfig<*>> {
    return values?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtMemberConfig<*>.properties(): Sequence<CwtPropertyConfig> {
    return properties?.orNull()?.asSequence().orEmpty()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofName(name: String, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return takeIf { it.key.equals(name, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofName(name: String, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return mapNotNull { it.ofName(name, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofNames(names: Collection<String>, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return takeIf { if (ignoreCase) names.any { name -> it.key.equals(name, ignoreCase = true) } else names.contains(it.key) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofNames(names: Collection<String>, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return mapNotNull { it.ofNames(names, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofNamePattern(namePattern: String, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return takeIf { it.key.matchesPattern(namePattern, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofNamePattern(namePattern: String, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return mapNotNull { it.ofNamePattern(namePattern, ignoreCase) }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofNamePatterns(namePatterns: Collection<String>, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return takeIf { namePatterns.any { namePattern -> it.key.matchesPattern(namePattern, ignoreCase) } }
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofNamePatterns(namePatterns: Collection<String>, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return mapNotNull { it.ofNamePatterns(namePatterns, ignoreCase) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofPath(path: String, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    val subPaths = CwtConfigPath.resolve(path).subPaths
    if (subPaths.isEmpty()) return emptySequence()

    var current: Sequence<CwtMemberConfig<*>> = sequenceOf(this)
    for (subPath in subPaths) {
        current = when (subPath) {
            "-" -> {
                current.flatMap { parent ->
                    parent.configs
                        ?.asSequence()
                        ?.filterIsInstance<CwtValueConfig>()
                        ?.filter { it.configs != null }
                        ?: emptySequence()
                }
            }
            else -> {
                current.flatMap { parent ->
                    parent.configs
                        ?.asSequence()
                        ?.filterIsInstance<CwtPropertyConfig>()
                        ?.filter { it.key.equals(subPath, ignoreCase) }
                        ?: emptySequence()
                }
            }
        }
    }
    return current.filterIsInstance<CwtPropertyConfig>()
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofPath(path: String, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return flatMap { it.ofPath(path, ignoreCase) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun CwtPropertyConfig.ofPaths(paths: Collection<String>, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return paths.asSequence().flatMap { path -> ofPath(path, ignoreCase) }
}

/** @see CwtConfigPath */
context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
fun Sequence<CwtPropertyConfig>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = false): Sequence<CwtPropertyConfig> {
    return flatMap { it.ofPaths(paths, ignoreCase) }
}

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

class CwtConfigSelectScopeImpl: CwtConfigSelectScope {
    // region Common

    override fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

    override fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

    // endregion

    // region Walks

    override fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>> {
        return generateSequence(this) { it.parentConfig }
    }

    override fun CwtMemberContainerConfig<*>.walkDown(traversal: TreeTraversal): Sequence<CwtMemberConfig<*>> {
        return generateSequenceFromSeeds(traversal, configs) { it.configs.orEmpty() }
    }

    // endregion

    // region Casts

    override fun CwtMemberConfig<*>?.asProperty(): CwtPropertyConfig? {
        return this?.castOrNull()
    }

    override fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig> {
        return filterIsInstance<CwtPropertyConfig>()
    }

    override fun CwtMemberConfig<*>?.asValue(): CwtValueConfig? {
        return this?.castOrNull()
    }

    override fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig> {
        return filterIsInstance<CwtValueConfig>()
    }

    override fun CwtMemberConfig<*>?.asBlock(): CwtValueConfig? {
        return this?.takeIf { it.configs != null }?.castOrNull()
    }

    override fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig> {
        return filter { it.configs != null }.filterIsInstance<CwtValueConfig>()
    }

    // endregion

    // region Queries

    override fun CwtMemberConfig<*>.selectLiteralValue(): String? {
        return if (configs == null) value else null
    }

    override fun CwtPropertyConfig.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): CwtPropertyConfig? {
        if (key.isEmpty()) return null
        return takeIf { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
    }

    override fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtPropertyConfig> {
        if (key.isEmpty()) return emptySequence()
        return filter { PathMatcher.matches(it.key, key, ignoreCase, usePattern) }
    }

    override fun CwtPropertyConfig.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): CwtPropertyConfig? {
        return takeIf { keys.any { key -> PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
    }

    override fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtPropertyConfig> {
        return filter { keys.any { key -> PathMatcher.matches(it.key, key, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> T.ofValue(value: String, ignoreCase: Boolean): T? {
        return takeIf { selectLiteralValue().equals(value, ignoreCase) }
    }

    override fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean): Sequence<T> {
        return filter { it.selectLiteralValue().equals(value, ignoreCase) }
    }

    override fun <T : CwtMemberConfig<*>> T.ofValues(values: Collection<String>, ignoreCase: Boolean): T? {
        return takeIf { it.selectLiteralValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean): Sequence<T> {
        return filter { it.selectLiteralValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun CwtMemberContainerConfig<*>.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return ofPathInternal(path, ignoreCase, usePattern)
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return flatMap { it.ofPathInternal(path, ignoreCase, usePattern) }
    }

    override fun CwtMemberContainerConfig<*>.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern) }
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern) } }
    }

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
}



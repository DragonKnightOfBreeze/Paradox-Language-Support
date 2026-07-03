package icu.windea.pls.config.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.members
import icu.windea.pls.config.properties
import icu.windea.pls.config.values
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.match.KeywordMatcher
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.model.paths.CwtConfigPath

class CwtConfigSelectScopeImpl : CwtConfigSelectScope {
    // region Common

    override fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

    override fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

    override fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>> {
        return generateSequence(this) { it.parentConfig }
    }

    override fun CwtMemberContainerConfig<*>.walkDown(traversal: TreeTraversal): Sequence<CwtMemberConfig<*>> {
        return generateSequenceFromSeeds(traversal, configs) { it.configs.orEmpty() }
    }

    override fun CwtMemberConfig<*>?.literalValue(): String? {
        if (this == null) return null
        return if (configs == null) value else null
    }

    // endregion

    // region Filters

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

    override fun CwtPropertyConfig?.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): CwtPropertyConfig? {
        if (this == null) return null
        return takeIf { with(it.key) { KeywordMatcher.matches(this, key, ignoreCase, usePattern) } }
    }

    override fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtPropertyConfig> {
        return filter { with(it.key) { KeywordMatcher.matches(this, key, ignoreCase, usePattern) } }
    }

    override fun CwtPropertyConfig?.ofKeys(vararg keys: String, ignoreCase: Boolean, usePattern: Boolean): CwtPropertyConfig? {
        if (this == null) return null
        return takeIf { with(it.key) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun Sequence<CwtPropertyConfig>.ofKeys(vararg keys: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtPropertyConfig> {
        return filter { with(it.key) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun CwtPropertyConfig?.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): CwtPropertyConfig? {
        if (this == null) return null
        return takeIf { with(it.key) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtPropertyConfig> {
        return filter { with(it.key) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> T?.ofValue(value: String, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, value, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, value, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> T?.ofValues(vararg values: String, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(vararg values: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> T?.ofValues(values: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    // endregion

    // region Queries

    override fun CwtMemberContainerConfig<*>?.query(): Sequence<CwtMemberConfig<*>> {
        if (this == null) return emptySequence()
        return members()
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.query(): Sequence<CwtMemberConfig<*>> {
        return flatMap { it.members() }
    }

    override fun CwtMemberContainerConfig<*>?.queryBy(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        if (this == null) return emptySequence()
        return queryByInternal(path, ignoreCase, usePattern)
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.queryBy(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return flatMap { it.queryByInternal(path, ignoreCase, usePattern) }
    }

    override fun CwtMemberContainerConfig<*>?.queryBy(vararg paths: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        if (this == null) return emptySequence()
        return paths.asSequence().flatMap { path -> queryByInternal(path, ignoreCase, usePattern) }
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.queryBy(vararg paths: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return flatMap { paths.asSequence().flatMap { path -> it.queryByInternal(path, ignoreCase, usePattern) } }
    }

    override fun CwtMemberContainerConfig<*>?.queryBy(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        if (this == null) return emptySequence()
        return paths.asSequence().flatMap { path -> queryByInternal(path, ignoreCase, usePattern) }
    }

    override fun Sequence<CwtMemberContainerConfig<*>>.queryBy(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        return flatMap { paths.asSequence().flatMap { path -> it.queryByInternal(path, ignoreCase, usePattern) } }
    }

    private fun CwtMemberContainerConfig<*>.queryByInternal(path: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<CwtMemberConfig<*>> {
        val resolvedPath = CwtConfigPath.resolve(path)
        val subPaths = resolvedPath.subPaths
        if (subPaths.isEmpty()) return emptySequence()
        var current: Sequence<CwtMemberConfig<*>>? = null
        for (i in 0..subPaths.lastIndex) {
            ProgressManager.checkCanceled()
            val subPath = subPaths[i]
            if (current == null) {
                current = when (subPath) {
                    "-" -> values()
                    else -> properties().filter { p -> p.matchesSubPath(subPath, ignoreCase, usePattern) }
                }
            } else {
                current = when (subPath) {
                    "-" -> current.flatMap { it.values() }
                    else -> current.flatMap { it.properties().filter { p -> p.matchesSubPath(subPath, ignoreCase, usePattern) } }
                }
            }
        }
        return current.orEmpty()
    }

    private fun CwtPropertyConfig.matchesSubPath(subPath: String, ignoreCase: Boolean, usePattern: Boolean): Boolean {
        return PathMatcher.matches(key, subPath, ignoreCase, usePattern)
    }

    // endregion
}



@file:Suppress("unused")

package icu.windea.pls.config

import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberType
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removePrefixOrNull

// region Config Extensions

inline val CwtMemberConfig<*>.documentation: String? get() = CwtConfigManager.getDocumentation(this)

inline val CwtFilePathMatchableConfig<*>.filePathPatterns: Set<String> get() = CwtConfigManager.getFilePathPatterns(this)

inline val CwtFilePathMatchableConfig<*>.filePathPatternsForPriority: Set<String> get() = CwtConfigManager.getFilePathPatternsForPriority(this)

inline fun CwtUnionConfig.processUnionCandidates(processor: (CwtValueConfig) -> Boolean): Boolean {
    return CwtConfigManager.processUnionCandidates(this, processor)
}

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression?, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    return CwtConfigManager.sortedByPriority(this, expressionProvider, configGroupProvider)
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@Optimized
inline fun List<CwtMemberConfig<*>>.filterProperties(): List<CwtPropertyConfig> {
    return filterFast { it.memberType == CwtMemberType.PROPERTY } as List<CwtPropertyConfig>
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@Optimized
inline fun List<CwtMemberConfig<*>>.filterValues(): List<CwtValueConfig> {
    return filterFast { it.memberType == CwtMemberType.VALUE } as List<CwtValueConfig>
}

// endregion

// region Addon Extensions

// in order to be compatible with eu5 config files
private val pathPrefixes = arrayOf("game/", "game/in_game/", "game/main_menu/", "game/loading_screen/")

fun String.optimizedPath(): String {
    val r = pathPrefixes.firstNotNullOfOrNull { removePrefixOrNull(it) } ?: this
    return r.normalizePath().optimized()
}

fun String.optimizedPathExtension(): String {
    val r = removePrefix(".")
    return r.optimized()
}

// endregion

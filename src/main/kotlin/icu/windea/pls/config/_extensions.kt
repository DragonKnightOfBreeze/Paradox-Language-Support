@file:Suppress("unused")

package icu.windea.pls.config

import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtFilePathMatchableConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configExpression.CwtConfigExpressionService
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.toListOrThis
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.cwt.psi.CwtMember

val CwtMemberConfig<*>.documentation: String? get() = CwtConfigManager.getDocumentation(this)

val CwtFilePathMatchableConfig.filePathPatterns: Set<String> get() = CwtConfigManager.getFilePathPatterns(this)

val CwtFilePathMatchableConfig.filePathPatternsForPriority: Set<String> get() = CwtConfigManager.getFilePathPatternsForPriority(this)

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression?, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    if (size <= 1) return toListOrThis()
    return sortedByDescending s@{
        val expression = expressionProvider(it) ?: return@s Double.MAX_VALUE
        val configGroup = configGroupProvider(it)
        CwtConfigExpressionService.getPriority(expression, configGroup)
    }
}

fun <T : CwtMember> T.bindConfig(config: CwtConfig<*>): T {
    this.putUserData(CwtConfigManager.Keys.bindingConfig, config)
    return this
}

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

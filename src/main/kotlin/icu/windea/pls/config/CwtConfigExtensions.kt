package icu.windea.pls.config

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.dataExpression.*

val Project.configGroupLibrary: CwtConfigGroupLibrary
    get() = this.getOrPutUserData(PlsKeys.configGroupLibrary) { CwtConfigGroupLibrary(this) }

/**
 * 解析为被内联的CWT规则，或者返回自身。
 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolved(): T {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config ?: this
        else -> this
    } as T
}

/**
 * 解析为被内联的规则，或者返回null。
 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolvedOrNull(): T? {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config
        else -> this
    } as? T
}

inline fun CwtMemberConfig<*>.processParent(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    var parent = this.parentConfig
    while (parent != null) {
        val result = processor(parent)
        if (!result) return false
        parent = parent.parentConfig
    }
    return true
}

fun CwtMemberConfig<*>.processDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtMemberConfig<*>.doProcessDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    processor(this).also { if (!it) return false }
    this.configs?.process { it.doProcessDescendants(processor) }?.also { if (!it) return false }
    return true
}

val CwtMemberConfig<*>.documentation: String? get() = CwtConfigManager.getDocumentation(this)

val CwtFilePathMatchableConfig.filePathPatterns: Set<String> get() = CwtConfigManager.getFilePathPatterns(this)

val CwtFilePathMatchableConfig.filePathPatternsForPriority: Set<String> get() = CwtConfigManager.getFilePathPatternsForPriority(this)

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression?, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    if (size <= 1) return toListOrThis()
    return sortedByDescending s@{
        val expression = expressionProvider(it) ?: return@s Double.MAX_VALUE
        val configGroup = configGroupProvider(it)
        CwtDataExpressionPriorityProvider.getPriority(expression, configGroup)
    }
}

fun <T : CwtMemberElement> T.bindConfig(config: CwtConfig<*>): T {
    this.putUserData(PlsKeys.bindingConfig, config)
    return this
}

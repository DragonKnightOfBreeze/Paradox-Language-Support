package icu.windea.pls.lang.util.renderers

import icu.windea.pls.lang.codeInsight.ParadoxTypeManager

/**
 * 渲染器的注解级别。
 *
 * @property includeType 是否包含类型信息。参见 [ParadoxTypeManager.getType]。
 * @property includeDefinitionType 是否包含定义类型信息。参见 [ParadoxTypeManager.getDefinitionType]。
 * @property includeOverrideStrategy 是否包含覆盖方式信息。参见 [ParadoxTypeManager.getOverrideStrategy]。
 * @property includeConfigExpression 是否包含配置表达式信息。参见 [ParadoxTypeManager.getConfigExpression]。
 * @property includeScopeContext 是否包含作用域上下文信息。参见 [ParadoxTypeManager.getScopeContext]。
 * @property includeDetailedScopeContext 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
 *
 * @see ParadoxScriptTextAnnotatedRenderer
 * @see ParadoxCsvTextAnnotatedRenderer
 */
@Suppress("unused")
data class ParadoxRendererAnnotatedLevel(
    val includeType: Boolean = false,
    val includeDefinitionType: Boolean = false,
    val includeOverrideStrategy: Boolean = false,
    val includeConfigExpression: Boolean = false,
    val includeScopeContext: Boolean = false,
    val includeDetailedScopeContext: Boolean = false,
) {
    companion object {
        @JvmField
        val BASIC: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel(
            includeType = true,
        )

        @JvmField
        val DEFAULT: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
        )

        @JvmField
        val ADVANCED: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
        )

        @JvmField
        val ALL: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel(
            includeType = true,
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
            includeDetailedScopeContext = true,
        )
    }
}

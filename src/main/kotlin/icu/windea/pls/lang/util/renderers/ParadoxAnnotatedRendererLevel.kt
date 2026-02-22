package icu.windea.pls.lang.util.renderers

import icu.windea.pls.lang.codeInsight.ParadoxAnnotatedManager

/**
 * 渲染器的注解级别。
 *
 * @property includeType 是否包含类型信息。
 * @property includeDefinitionType 是否包含定义类型信息。
 * @property includeOverrideStrategy 是否包含覆盖方式信息。
 * @property includeConfigExpression 是否包含规则表达式信息。
 * @property includeScopeContext 是否包含作用域上下文信息。
 * @property includeDetailedScopeContext 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
 *
 * @see ParadoxAnnotatedManager
 * @see ParadoxScriptTextAnnotatedRenderer
 * @see ParadoxCsvTextAnnotatedRenderer
 */
@Suppress("unused")
data class ParadoxAnnotatedRendererLevel(
    val includeType: Boolean = false,
    val includeDefinitionType: Boolean = false,
    val includeOverrideStrategy: Boolean = false,
    val includeConfigExpression: Boolean = false,
    val includeScopeContext: Boolean = false,
    val includeDetailedScopeContext: Boolean = false,
) {
    companion object {
        @JvmField
        val BASIC: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel(
            includeType = true,
        )

        @JvmField
        val DEFAULT: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
        )

        @JvmField
        val ADVANCED: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
        )

        @JvmField
        val ALL: ParadoxAnnotatedRendererLevel = ParadoxAnnotatedRendererLevel(
            includeType = true,
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
            includeDetailedScopeContext = true,
        )
    }
}

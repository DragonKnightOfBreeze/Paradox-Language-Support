package icu.windea.pls.lang.codeInsight.annotated

/**
 * 注解级别。用于配置要包含的注解。
 *
 * @property includeType 是否包含类型信息。
 * @property includeDefinitionType 是否包含定义类型信息。
 * @property includeOverrideStrategy 是否包含覆盖方式信息。
 * @property includeConfigExpression 是否包含规则表达式信息。
 * @property includeScopeContext 是否包含作用域上下文信息。
 * @property includeUnchancedScopeContext 是否包含未发生更改的作用域上下文信息。
 * @property includeDetailedScopeContext 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
 *
 * @see ParadoxScriptAnnotatedManager
 * @see ParadoxCsvAnnotatedManager
 */
@Suppress("unused")
data class ParadoxAnnotatedLevel(
    val includeType: Boolean = false,
    val includeDefinitionType: Boolean = false,
    val includeOverrideStrategy: Boolean = false,
    val includeConfigExpression: Boolean = false,
    val includeScopeContext: Boolean = false,
    val includeUnchancedScopeContext: Boolean = false,
    val includeDetailedScopeContext: Boolean = false,
) {
    companion object {
        @JvmField val BASIC: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel(
            includeType = true,
        )

        @JvmField val DEFAULT: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
        )

        @JvmField val ADVANCED: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel(
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
        )

        @JvmField val ALL: ParadoxAnnotatedLevel = ParadoxAnnotatedLevel(
            includeType = true,
            includeDefinitionType = true,
            includeOverrideStrategy = true,
            includeConfigExpression = true,
            includeScopeContext = true,
            includeUnchancedScopeContext = true,
            includeDetailedScopeContext = true,
        )
    }
}

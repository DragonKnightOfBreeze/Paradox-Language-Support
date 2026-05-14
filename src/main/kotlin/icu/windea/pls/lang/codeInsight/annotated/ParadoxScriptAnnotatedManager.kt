package icu.windea.pls.lang.codeInsight.annotated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.toScopeIdMap
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxScriptAnnotatedManager {
    // region Prefixes

    const val typePrefix = "@type"
    const val definitionTypePrefix = "@definition_type"
    const val configExpressionPrefix = "@config_expression"
    const val overrideStrategyPrefix = "@override_strategy"
    const val scopeContextPrefix = "@scope_context"

    // endregion

    // region Annotation Getters

    /**
     * 得到类型信息的注解。
     *
     * 格式：
     * - `## @type key_type = value_type`
     * - `## @type value_type`
     */
    fun getTypeAnnotation(element: ParadoxScriptMember): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyType = element.propertyKey.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) }?.id ?: FallbackStrings.unknown
                val valueType = element.propertyValue?.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) }?.id ?: FallbackStrings.unknown
                "## $typePrefix ${keyType} = ${valueType}"
            }
            is ParadoxScriptValue -> {
                val valueType = element.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) }?.id ?: FallbackStrings.unknown
                "## $typePrefix ${valueType}"
            }
            else -> null
        }
    }

    /**
     * 得到定义类型信息的注解。
     *
     * 格式：
     * - `## @definition_type = type`
     * - `## @definition_type = type, subtype_1, subtype_2`
     */
    fun getDefinitionTypeAnnotation(element: ParadoxScriptMember): String? {
        if (element !is ParadoxScriptProperty) return null
        val definitionType = element.definitionCandidateInfo?.typeText ?: return null
        return "## $definitionTypePrefix ${definitionType}"
    }

    /**
     * 得到规则表达式信息的注解。
     *
     * 格式：
     * - `## @config_expression key_expression = value_expression`
     * - `## @config_expression value_expression`
     */
    fun getConfigExpressionAnnotation(element: ParadoxScriptMember): String? {
        val options = ParadoxMatchOptions(forDeclarationRoot = true)
        val config = ParadoxConfigManager.getConfigs(element, options).firstOrNull() ?: return null
        return when (element) {
            is ParadoxScriptProperty -> {
                if (config !is CwtPropertyConfig) return null
                val key = config.key
                val value = config.value
                "## $configExpressionPrefix ${key.quoteIfNecessary()} = ${value.quoteIfNecessary()}"
            }
            is ParadoxScriptValue -> {
                if (config !is CwtValueConfig) return null
                val value = config.value
                "## $configExpressionPrefix ${value.quoteIfNecessary()}"
            }
            else -> null
        }
    }

    /**
     * 得到覆盖方式信息的注解。
     *
     * 格式：
     * - `## @override_strategy = STRATEGY`
     */
    fun getOverrideStrategyAnnotation(element: ParadoxScriptMember): String? {
        val overrideStrategy = ParadoxOverrideService.getOverrideStrategy(element) ?: return null
        return "## $overrideStrategyPrefix ${overrideStrategy.id}"
    }

    /**
     * 得到作用域上下文信息的注解。
     *
     * 格式：
     * - `## @scope_context this = scope_1 root = scope_2`
     *
     * @param unchanged 是否包含未发生更改的作用域上下文信息。
     * @param detailed 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
     */
    fun getScopeContextAnnotation(element: ParadoxScriptMember, unchanged: Boolean = false, detailed: Boolean = false): String? {
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getScopeContext(element) ?: return null
        if (!unchanged && !ParadoxScopeManager.isScopeContextChanged(element, scopeContext)) return null
        val map = scopeContext.toScopeIdMap(showPrev = detailed)
        if (map.isEmpty()) return null
        return map.entries.joinToString(" ", "## $scopeContextPrefix ") { (k, v) -> "${k.quoteIfNecessary()} = ${v.quoteIfNecessary()}" }
    }

    // endregion
}

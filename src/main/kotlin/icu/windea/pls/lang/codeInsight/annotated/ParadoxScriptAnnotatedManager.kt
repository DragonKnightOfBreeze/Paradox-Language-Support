package icu.windea.pls.lang.codeInsight.annotated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.lang.codeInsight.ParadoxTypeManager
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.scope.toScopeIdMap
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxScriptAnnotatedManager {
    // region Prefixes

    const val typePrefix = "@type"
    const val definitionTypePrefix = "@definition_type"
    const val overrideStrategyPrefix = "@override_strategy"
    const val configExpressionPrefix = "@config_expression"
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
    fun getType(element: ParadoxScriptMember): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyType = ParadoxTypeManager.getType(element.propertyKey) ?: ParadoxType.Unknown
                val valueType = element.propertyValue?.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
                "## $typePrefix ${keyType.id} = ${valueType.id}"
            }
            is ParadoxScriptValue -> {
                val type = ParadoxTypeManager.getType(element) ?: ParadoxType.Unknown
                "## $typePrefix ${type.id}"
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
    fun getDefinitionType(element: ParadoxScriptMember): String? {
        if (element !is ParadoxScriptProperty) return null
        val definitionType = ParadoxTypeManager.getDefinitionType(element.propertyKey) ?: return null
        return "## $definitionTypePrefix $definitionType"
    }

    /**
     * 得到覆盖方式信息的注解。
     *
     * 格式：
     * - `## @override_strategy = STRATEGY`
     */
    fun getOverrideStrategy(element: ParadoxScriptMember): String? {
        val key = when (element) {
            is ParadoxScriptProperty -> element.propertyKey
            else -> null
        } ?: return null
        val overrideStrategy = ParadoxTypeManager.getOverrideStrategy(key) ?: return null
        return "## $overrideStrategyPrefix ${overrideStrategy.id}"
    }

    /**
     * 得到规则表达式信息的注解。
     *
     * 格式：
     * - `## @config_expression key_expression = value_expression`
     * - `## @config_expression value_expression`
     */
    fun getConfigExpression(element: ParadoxScriptMember): String? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        return when (element) {
            is ParadoxScriptProperty -> {
                if (config !is CwtPropertyConfig) return null
                val keyExpression = config.key.quoteIfNecessary()
                val valueExpression = config.value.quoteIfNecessary()
                "## $configExpressionPrefix $keyExpression = $valueExpression"
            }
            is ParadoxScriptValue -> {
                if (config !is CwtValueConfig) return null
                val valueExpression = config.value.quoteIfNecessary()
                "## $configExpressionPrefix $valueExpression"
            }
            else -> null
        }
    }

    /**
     * 得到作用域上下文信息的注解。
     *
     * 格式：
     * - `## @scope_context this = scope_1 root = scope_2`
     *
     * @param detailed 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
     */
    fun getScopeContext(element: ParadoxScriptMember, detailed: Boolean): String? {
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getScopeContext(element) ?: return null
        val map = scopeContext.toScopeIdMap(showPrev = detailed)
        if (map.isEmpty()) return null
        return map.entries.joinToString(" ", "## $scopeContextPrefix ") { "${it.key.quoteIfNecessary()} = ${it.value.quoteIfNecessary()}" }
    }

    // endregion
}

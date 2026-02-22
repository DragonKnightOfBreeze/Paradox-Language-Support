package icu.windea.pls.lang.codeInsight

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.scope.toScopeIdMap
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxAnnotatedManager {
    /**
     * 得到类型信息的注解。
     */
    fun getType(element: ParadoxScriptMember): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyType = ParadoxTypeManager.getType(element.propertyKey) ?: ParadoxType.Unknown
                val valueType = element.propertyValue?.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
                "## type = { ${keyType.id} = ${valueType.id} }"
            }
            is ParadoxScriptValue -> {
                val type = ParadoxTypeManager.getType(element) ?: ParadoxType.Unknown
                "## type = ${type.id}"
            }
            else -> null
        }
    }

    /**
     * 得到定义类型信息的注解。
     */
    fun getDefinitionType(element: ParadoxScriptMember): String? {
        if (element !is ParadoxScriptProperty) return null
        val definitionType = ParadoxTypeManager.getDefinitionType(element.propertyKey) ?: return null
        return "## definition_type = $definitionType"
    }

    /**
     * 得到覆盖方式信息的注解。
     */
    fun getOverrideStrategy(element: ParadoxScriptMember): String? {
        val key = when (element) {
            is ParadoxScriptProperty -> element.propertyKey
            else -> null
        } ?: return null
        val overrideStrategy = ParadoxTypeManager.getOverrideStrategy(key) ?: return null
        return "## override_strategy = ${overrideStrategy.id}"
    }

    /**
     * 得到规则表达式信息的注解。
     */
    fun getConfigExpression(element: ParadoxScriptMember): String? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        return when (element) {
            is ParadoxScriptProperty -> {
                if (config !is CwtPropertyConfig) return null
                val keyExpression = config.key.quoteIfNecessary()
                val valueExpression = config.value.quoteIfNecessary()
                "## config_expression = { $keyExpression = $valueExpression }"
            }
            is ParadoxScriptValue -> {
                if (config !is CwtValueConfig) return null
                val valueExpression = config.value.quoteIfNecessary()
                "## config_expression = $valueExpression"
            }
            else -> null
        }
    }

    /**
     * 得到作用域上下文信息的注解。
     *
     * @param detailed 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
     */
    fun getScopeContext(element: ParadoxScriptMember, detailed: Boolean): String? {
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getScopeContext(element) ?: return null
        val map = scopeContext.toScopeIdMap(showPrev = detailed)
        if (map.isEmpty()) return null
        return map.entries.joinToString(" ", "## scope_context = { ", " }") { "${it.key.quoteIfNecessary()} = ${it.value.quoteIfNecessary()}" }
    }

    /**
     * 得到类型信息的注解。
     */
    fun getTypeForRow(element: ParadoxCsvRowElement): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val columns = element.columnList.orNull() ?: return null
        val types = columns.map { column ->
            val type = column.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
            type.id
        }
        return types.joinToString(" ", "## type = { ", " }")
    }

    /**
     * 得到规则表达式信息的注解。
     */
    fun getConfigExpressionForRow(element: ParadoxCsvRowElement): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val rowConfig = ParadoxCsvManager.getRowConfig(element) ?: return null
        val columns = element.columnList.orNull() ?: return null
        val configExpressions = columns.map { column ->
            val columnConfig = ParadoxCsvManager.getColumnConfig(column, rowConfig) ?: return@map FallbackStrings.unknown
            if (!ParadoxCsvManager.isMatchedColumnConfig(column, columnConfig)) return@map FallbackStrings.unknown // require matched
            columnConfig.value
        }
        return configExpressions.joinToString(" ", "## config_expression = { ", " }")
    }
}

package icu.windea.pls.lang.data.annotated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.data.annotated.ParadoxConfigExpressionAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxDefinitionTypeAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxOverrideStrategyAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxScopeContextAnnotatedData
import icu.windea.pls.model.data.annotated.ParadoxTypeAnnotatedData
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

@Optimized
object ParadoxAnnotatedDataFactory {
    /**
     * 创建来自脚本成员的表达式类型的注解数据。
     */
    fun createType(element: ParadoxScriptMember): ParadoxTypeAnnotatedData.FromMember? {
        return when (element) {
            is ParadoxScriptProperty -> createType(element)
            is ParadoxScriptValue -> createType(element)
            else -> null
        }
    }

    /**
     * 创建来自脚本属性的表达式类型的注解数据。
     */
    fun createType(element: ParadoxScriptProperty): ParadoxTypeAnnotatedData.FromProperty {
        val keyType = element.propertyKey.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) } ?: ParadoxExpressionType.Unknown
        val valueType = element.propertyValue?.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) } ?: ParadoxExpressionType.Unknown
        return ParadoxTypeAnnotatedData.FromProperty(keyType, valueType)
    }

    /**
     * 创建来自脚本值的表达式类型的注解数据。
     */
    fun createType(element: ParadoxScriptValue): ParadoxTypeAnnotatedData.FromValue {
        val valueType = element.resolved()?.let { ParadoxTypeResolver.resolveExpressionType(it) } ?: ParadoxExpressionType.Unknown
        return ParadoxTypeAnnotatedData.FromValue(valueType)
    }

    /**
     * 创建来自列容器的表达式类型的注解数据。
     */
    fun createType(element: ParadoxCsvColumnContainer): ParadoxTypeAnnotatedData.FromColumns? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val columns = element.columnList.orNull() ?: return null
        val types = columns.mapFast { ParadoxTypeResolver.resolveExpressionType(it) }
        return ParadoxTypeAnnotatedData.FromColumns(types)
    }

    /**
     * 创建来自脚本成员的规则表达式的注解数据。
     */
    fun createConfigExpression(element: ParadoxScriptMember): ParadoxConfigExpressionAnnotatedData.FromMember? {
        return when (element) {
            is ParadoxScriptProperty -> createConfigExpression(element)
            is ParadoxScriptValue -> createConfigExpression(element)
            else -> null
        }
    }

    /**
     * 创建来自脚本属性的规则表达式的注解数据。
     */
    fun createConfigExpression(element: ParadoxScriptProperty): ParadoxConfigExpressionAnnotatedData.FromProperty? {
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true)).firstOrNull() ?: return null
        if (config !is CwtPropertyConfig) return null
        val configGroup = config.configGroup
        return ParadoxConfigExpressionAnnotatedData.FromProperty(config.keyExpression, config.valueExpression, configGroup)
    }

    /**
     * 创建来自脚本值的规则表达式的注解数据。
     */
    fun createConfigExpression(element: ParadoxScriptValue): ParadoxConfigExpressionAnnotatedData.FromValue? {
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true)).firstOrNull() ?: return null
        if (config !is CwtValueConfig) return null
        val configGroup = config.configGroup
        return ParadoxConfigExpressionAnnotatedData.FromValue(config.valueExpression, configGroup)
    }

    /**
     * 创建来自列容器的规则表达式的注解数据。
     */
    fun createConfigExpression(element: ParadoxCsvColumnContainer): ParadoxConfigExpressionAnnotatedData.FromColumns? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val rowConfig = ParadoxConfigManager.getRowConfig(element) ?: return null
        val columns = element.columnList.orNull() ?: return null
        val configExpressions = columns.mapFast f@{ column ->
            val columnConfig = ParadoxConfigManager.getColumnConfig(column, rowConfig) ?: return@f CwtDataExpression.resolveEmpty()
            if (!ParadoxConfigManager.isMatchedColumnConfig(column, columnConfig)) return@f CwtDataExpression.resolveEmpty() // require matched
            columnConfig.valueExpression
        }
        val configGroup = rowConfig.configGroup
        return ParadoxConfigExpressionAnnotatedData.FromColumns(configExpressions, configGroup)
    }

    /**
     * 创建来自脚本成员（在语义级别数据定义候选，包括定义、定义注入等）的定义类型的注解数据。
     */
    fun createDefinitionType(element: ParadoxScriptMember): ParadoxDefinitionTypeAnnotatedData? {
        if (element !is ParadoxScriptProperty) return null
        val info = element.definitionCandidateInfo ?: return null
        return ParadoxDefinitionTypeAnnotatedData(info.type, info.subtypes, info.configGroup)
    }

    /**
     * 创建来着脚本成员的覆盖策略的注解数据。
     */
    fun createOverrideStrategy(element: ParadoxScriptMember): ParadoxOverrideStrategyAnnotatedData? {
        val overrideStrategy = ParadoxOverrideService.getOverrideStrategy(element) ?: return null
        return ParadoxOverrideStrategyAnnotatedData(overrideStrategy)
    }

    /**
     * 创建来自脚本成员的覆盖策略的注解数据。
     *
     * @param unchanged 是否包含未发生更改的作用域上下文信息。
     * @param detailed 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
     */
    fun createScopeContext(element: ParadoxScriptMember, unchanged: Boolean = false, detailed: Boolean = false): ParadoxScopeContextAnnotatedData? {
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getScopeContext(element) ?: return null
        if (!unchanged && !ParadoxScopeManager.isScopeContextChanged(element, scopeContext)) return null
        return ParadoxScopeContextAnnotatedData(scopeContext, unchanged, detailed)
    }
}

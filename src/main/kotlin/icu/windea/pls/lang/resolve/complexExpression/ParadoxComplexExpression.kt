package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 复杂表达式。
 *
 * 对应脚本语言与本地化语言中的一段特定的表达式文本，它们可能包含数个节点，且允许嵌套包含。
 */
interface ParadoxComplexExpression : ParadoxComplexExpressionNode {
    fun getErrors(element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> = emptyList()

    fun getAllErrors(element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> = emptyList()

    fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference> = emptyList()

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression?
        fun resolveByConfig(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression?
        fun resolveByDataType(text: String, range: TextRange?, configGroup: CwtConfigGroup, dataType: CwtDataType, config: CwtConfig<*>? = null): ParadoxComplexExpression?
    }

    companion object : Resolver by ParadoxComplexExpressionResolverImpl()
}

// region Implementations

private class ParadoxComplexExpressionResolverImpl : ParadoxComplexExpression.Resolver {
    override fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
                val value = element.value
                resolveByConfig(value, null, configGroup, config)
            }
            is ParadoxLocalisationExpressionElement -> {
                if (!element.isComplexExpression()) return null
                when {
                    element.isCommandExpression() -> {
                        val value = element.value
                        ParadoxCommandExpression.resolve(value, null, configGroup)
                    }
                    element.isDatabaseObjectExpression(strict = true) -> {
                        val value = element.value
                        ParadoxDatabaseObjectExpression.resolve(value, null, configGroup)
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    override fun resolveByConfig(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
        val dataType = config.configExpression?.type ?: return null
        return resolveByDataType(text, range, configGroup, dataType, config)
    }

    override fun resolveByDataType(text: String, range: TextRange?, configGroup: CwtConfigGroup, dataType: CwtDataType, config: CwtConfig<*>?): ParadoxComplexExpression? {
        return when {
            dataType == CwtDataTypes.TemplateExpression -> ParadoxTemplateExpression.resolve(text, range, configGroup, config ?: return null)
            dataType in CwtDataTypeSets.DynamicValue -> ParadoxDynamicValueExpression.resolve(text, range, configGroup, config ?: return null)
            dataType in CwtDataTypeSets.ScopeField -> ParadoxScopeFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeSets.ValueField -> ParadoxValueFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeSets.VariableField -> ParadoxVariableFieldExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.Command -> ParadoxCommandExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.DatabaseObject -> ParadoxDatabaseObjectExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.DefineReference -> ParadoxDefineReferenceExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.StellarisNameFormat -> StellarisNameFormatExpression.resolve(text, range, configGroup, config ?: return null)
            else -> null
        }
    }
}

// endregion

package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.ParadoxCommandExpression
import icu.windea.pls.lang.expression.ParadoxComplexExpression
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.expression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.ParadoxTemplateExpression
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

internal class ParadoxComplexExpressionResolverImpl : ParadoxComplexExpression.Resolver {
    override fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                resolveByConfig(value, textRange, configGroup, config)
            }
            is ParadoxLocalisationExpressionElement -> {
                if (!element.isComplexExpression()) return null
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                when {
                    element.isCommandExpression() -> ParadoxCommandExpression.resolve(value, textRange, configGroup)
                    element.isDatabaseObjectExpression() -> ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup)
                    else -> null
                }
            }
            else -> null
        }
    }

    override fun resolveByDataType(text: String, range: TextRange, configGroup: CwtConfigGroup, dataType: CwtDataType, config: CwtConfig<*>?): ParadoxComplexExpression? {
        return when {
            dataType == CwtDataTypes.TemplateExpression -> ParadoxTemplateExpression.resolve(text, range, configGroup, config ?: return null)
            dataType in CwtDataTypeGroups.DynamicValue -> ParadoxDynamicValueExpression.resolve(text, range, configGroup, config ?: return null)
            dataType in CwtDataTypeGroups.ScopeField -> ParadoxScopeFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeGroups.ValueField -> ParadoxValueFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeGroups.VariableField -> ParadoxVariableFieldExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.DatabaseObject -> ParadoxDatabaseObjectExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.DefineReference -> ParadoxDefineReferenceExpression.resolve(text, range, configGroup)
            else -> null
        }
    }

    override fun resolveByConfig(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
        val dataType = config.configExpression?.type ?: return null
        return resolveByDataType(text, range, configGroup, dataType, config)
    }
}

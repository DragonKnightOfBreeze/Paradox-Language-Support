package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

internal class ParadoxComplexExpressionResolverImpl : ParadoxComplexExpression.Resolver {
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

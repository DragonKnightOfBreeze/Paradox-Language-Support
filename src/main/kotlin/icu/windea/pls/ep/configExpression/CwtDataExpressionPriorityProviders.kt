package icu.windea.pls.ep.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.enums

class BaseCwtDataExpressionPriorityProvider : CwtDataExpressionPriorityProvider {
    override fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        return when (configExpression.type) {
            CwtDataTypes.Block -> 100.0 //highest
            CwtDataTypes.Bool -> 100.0 //highest
            CwtDataTypes.Int -> 90.0 //very high
            CwtDataTypes.Float -> 90.0 //very high
            CwtDataTypes.Scalar -> 2.0 //very low
            CwtDataTypes.ColorField -> 90.0 //very high
            else -> 0.0
        }
    }
}

class CoreCwtDataExpressionPriorityProvider : CwtDataExpressionPriorityProvider {
    override fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        return when (configExpression.type) {
            CwtDataTypes.Constant -> 100.0 //highest
            CwtDataTypes.Any -> 1.0 //very low
            CwtDataTypes.Parameter -> 10.0
            CwtDataTypes.ParameterValue -> 90.0 //same to Scalar
            CwtDataTypes.LocalisationParameter -> 10.0
            CwtDataTypes.ShaderEffect -> 85.0
            CwtDataTypes.StellarisNameFormat -> 60.0
            CwtDataTypes.TechnologyWithLevel -> 69.0 //lower than Definition
            CwtDataTypes.PercentageField -> 90.0
            CwtDataTypes.DateField -> 90.0
            CwtDataTypes.Localisation -> 60.0
            CwtDataTypes.SyncedLocalisation -> 60.0
            CwtDataTypes.InlineLocalisation -> 60.0
            CwtDataTypes.AbsoluteFilePath -> 70.0
            CwtDataTypes.Icon -> 70.0
            CwtDataTypes.FilePath -> 70.0
            CwtDataTypes.FileName -> 70.0
            CwtDataTypes.Definition -> 70.0
            CwtDataTypes.EnumValue -> {
                val enumName = configExpression.value ?: return 0.0 //unexpected
                if (configGroup.enums.containsKey(enumName)) return 80.0
                if (configGroup.complexEnums.containsKey(enumName)) return 45.0
                return 0.0 //unexpected
            }
            CwtDataTypes.Value -> 40.0
            CwtDataTypes.ValueSet -> 40.0
            CwtDataTypes.DynamicValue -> 40.0
            CwtDataTypes.ScopeField -> 50.0
            CwtDataTypes.Scope -> 50.0
            CwtDataTypes.ScopeGroup -> 50.0
            CwtDataTypes.ValueField -> 45.0
            CwtDataTypes.IntValueField -> 45.0
            CwtDataTypes.VariableField -> 45.0
            CwtDataTypes.IntVariableField -> 45.0
            CwtDataTypes.Modifier -> 75.0 //higher than Definition
            CwtDataTypes.SingleAliasRight -> 0.0 //unexpected
            CwtDataTypes.AliasName -> 0.0 //unexpected
            CwtDataTypes.AliasKeysField -> 0.0 //unexpected
            CwtDataTypes.AliasMatchLeft -> 0.0 //unexpected
            CwtDataTypes.TemplateExpression -> 65.0
            else -> 0.0
        }
    }
}

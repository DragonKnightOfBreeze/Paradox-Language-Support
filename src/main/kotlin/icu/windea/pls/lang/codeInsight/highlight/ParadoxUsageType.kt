package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.usages.impl.rules.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import java.util.concurrent.*

object ParadoxUsageType {
    val SCRIPTED_VARIABLE_REFERENCE_1 = UsageType { PlsBundle.message("usageType.scriptedVariableReference.1") }
    val SCRIPTED_VARIABLE_REFERENCE_2 = UsageType { PlsBundle.message("usageType.scriptedVariableReference.2") }
    val SCRIPTED_VARIABLE_REFERENCE_3 = UsageType { PlsBundle.message("usageType.scriptedVariableReference.3") }

    val PARAMETER_REFERENCE_1 = UsageType { PlsBundle.message("usageType.parameterReference.1") }
    val PARAMETER_REFERENCE_2 = UsageType { PlsBundle.message("usageType.parameterReference.2") }
    val PARAMETER_REFERENCE_3 = UsageType { PlsBundle.message("usageType.parameterReference.3") }
    val PARAMETER_REFERENCE_4 = UsageType { PlsBundle.message("usageType.parameterReference.4") }
    val PARAMETER_REFERENCE_5 = UsageType { PlsBundle.message("usageType.parameterReference.5") }
    val PARAMETER_REFERENCE_6 = UsageType { PlsBundle.message("usageType.parameterReference.6") }

    val LOCALISATION_REFERENCE = UsageType { PlsBundle.message("usageType.localisationReference") }
    val LOCALISATION_ICON = UsageType { PlsBundle.message("usageType.localisationIcon") }
    val LOCALISATION_COLOR = UsageType { PlsBundle.message("usageType.localisationColor") }
    val LOCALISATION_COMMAND_TEXT = UsageType { PlsBundle.message("usageType.localisationCommandText") }
    val LOCALISATION_CONCEPT_NAME = UsageType { PlsBundle.message("usageType.localisationConceptName") }

    val COMPLEX_ENUM_VALUE = UsageType { PlsBundle.message("usageType.complexEnumValue") }

    private val FROM_CONFIG_EXPRESSION_TYPES: MutableMap<String, UsageType> = ConcurrentHashMap()

    fun FROM_CONFIG_EXPRESSION(configExpression: CwtDataExpression) = FROM_CONFIG_EXPRESSION_TYPES.getOrPut(configExpression.expressionString) {
        UsageType { PlsBundle.message("usageType.byConfigExpression", configExpression) }
    }
}

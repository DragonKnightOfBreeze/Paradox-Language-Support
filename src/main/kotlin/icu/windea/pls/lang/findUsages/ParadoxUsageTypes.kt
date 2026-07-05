package icu.windea.pls.lang.findUsages

import com.intellij.usages.impl.rules.UsageType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.configExpression.CwtDataExpression
import java.util.concurrent.ConcurrentHashMap

object ParadoxUsageTypes {
    val SCRIPTED_VARIABLE_REFERENCE = UsageType { ChronicleBundle.message("usageType.scriptedVariableReference") }
    val SCRIPTED_VARIABLE_REFERENCE_1 = UsageType { ChronicleBundle.message("usageType.scriptedVariableReference.1") }
    val SCRIPTED_VARIABLE_REFERENCE_2 = UsageType { ChronicleBundle.message("usageType.scriptedVariableReference.2") }

    val PARAMETER_REFERENCE = UsageType { ChronicleBundle.message("usageType.parameterReference") }
    val PARAMETER_REFERENCE_1 = UsageType { ChronicleBundle.message("usageType.parameterReference.1") }
    val PARAMETER_REFERENCE_2 = UsageType { ChronicleBundle.message("usageType.parameterReference.2") }
    val PARAMETER_REFERENCE_3 = UsageType { ChronicleBundle.message("usageType.parameterReference.3") }
    val PARAMETER_REFERENCE_4 = UsageType { ChronicleBundle.message("usageType.parameterReference.4") }

    val LOCALISATION_PARAMETER_REFERENCE = UsageType { ChronicleBundle.message("usageType.localisationParameterReference") }

    val LOCALISATION_PARAMETER = UsageType { ChronicleBundle.message("usageType.localisationParameter") }
    val LOCALISATION_ICON = UsageType { ChronicleBundle.message("usageType.localisationIcon") }
    val LOCALISATION_COLOR = UsageType { ChronicleBundle.message("usageType.localisationColor") }
    val LOCALISATION_COMMAND_TEXT = UsageType { ChronicleBundle.message("usageType.localisationCommandText") }
    val LOCALISATION_CONCEPT_NAME = UsageType { ChronicleBundle.message("usageType.localisationConceptName") }
    val LOCALISATION_TEXT_ICON = UsageType { ChronicleBundle.message("usageType.localisationTextIcon") }
    val LOCALISATION_TEXT_FORMAT = UsageType { ChronicleBundle.message("usageType.localisationTextFormat") }

    val COMPLEX_ENUM_VALUE_DECLARATION = UsageType { ChronicleBundle.message("usageType.complexEnumValueDeclaration") }
    val EVENT_NAMESPACE_REFERENCE = UsageType { ChronicleBundle.message("usageType.eventNamespaceReference") }
    val DEFINITION_INJECTION_TARGET = UsageType { ChronicleBundle.message("usageType.definitionInjectionTarget") }

    private val FROM_CONFIG_EXPRESSION_TYPES: MutableMap<String, UsageType> = ConcurrentHashMap()

    fun FROM_CONFIG_EXPRESSION(configExpression: CwtDataExpression) = FROM_CONFIG_EXPRESSION_TYPES.getOrPut(configExpression.expressionString) {
        UsageType { ChronicleBundle.message("usageType.byConfigExpression", configExpression) }
    }

    val HEADER_COLUMN = UsageType { ChronicleBundle.message("usageType.headerColumn") }
}

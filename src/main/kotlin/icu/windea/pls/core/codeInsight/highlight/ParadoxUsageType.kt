package icu.windea.pls.core.codeInsight.highlight

import com.intellij.usages.impl.rules.*
import icu.windea.pls.*

object ParadoxUsageType {
	val SCRIPTED_VARIABLE_REFERENCE_1 = UsageType { PlsBundle.message("usageType.scriptedVariableReference.1") }
	val SCRIPTED_VARIABLE_REFERENCE_2 = UsageType { PlsBundle.message("usageType.scriptedVariableReference.2") }
	val PARAMETER_REFERENCE_1 = UsageType { PlsBundle.message("usageType.parameterReference.1") }
	val PARAMETER_REFERENCE_2 = UsageType { PlsBundle.message("usageType.parameterReference.2") }
	val PARAMETER_REFERENCE_3 = UsageType { PlsBundle.message("usageType.parameterReference.3") }
	val PARAMETER_REFERENCE_4 = UsageType { PlsBundle.message("usageType.parameterReference.4") }
	val PARAMETER_REFERENCE_5 = UsageType { PlsBundle.message("usageType.parameterReference.5") }
}

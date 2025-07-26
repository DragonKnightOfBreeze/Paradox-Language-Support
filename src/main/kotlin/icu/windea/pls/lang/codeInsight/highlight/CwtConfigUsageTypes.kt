package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.usages.impl.rules.UsageType
import icu.windea.pls.PlsBundle

object CwtConfigUsageTypes {
    val SYMBOL_DECLARATION = UsageType { PlsBundle.message("usageType.config.symbolDeclaration") }
    val SYMBOL_REFERENCE = UsageType { PlsBundle.message("usageType.config.symbolReference") }
}

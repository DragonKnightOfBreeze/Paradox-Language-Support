package icu.windea.pls.lang.findUsages

import com.intellij.usages.impl.rules.UsageType
import icu.windea.pls.ChronicleBundle

object CwtConfigUsageTypes {
    val SYMBOL_DECLARATION = UsageType { ChronicleBundle.message("usageType.config.symbolDeclaration") }
    val SYMBOL_REFERENCE = UsageType { ChronicleBundle.message("usageType.config.symbolReference") }
}

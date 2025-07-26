package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.usages.impl.rules.*
import icu.windea.pls.*

object CwtConfigUsageTypes {
    val SYMBOL_DECLARATION = UsageType { PlsBundle.message("usageType.config.symbolDeclaration") }
    val SYMBOL_REFERENCE = UsageType { PlsBundle.message("usageType.config.symbolReference") }
}

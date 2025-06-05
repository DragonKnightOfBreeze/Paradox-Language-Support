package icu.windea.pls.integrations.translation

import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.providers.*
import icu.windea.pls.integrations.translation.providers.PlsTranslationToolProvider.INSTANCE.EP_NAME

object PlsTranslationManager {
    fun findTool(): PlsTranslationToolProvider? {
        return EP_NAME.extensionList.find { it.supports() }
    }

    fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String? {
        val toolProvider = findTool() ?: throw UnsupportedOperationException("Unsupported: No translation tool found.")
        return toolProvider.translate(text, sourceLocale, targetLocale)
    }
}

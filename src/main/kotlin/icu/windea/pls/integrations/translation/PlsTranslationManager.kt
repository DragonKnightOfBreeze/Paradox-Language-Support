package icu.windea.pls.integrations.translation

import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.providers.*

object PlsTranslationManager {
    fun findTool(): PlsTranslationToolProvider? {
        return PlsTranslationToolProvider.EP_NAME.extensionList.find { it.supports() }
    }

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback) {
        val toolProvider = findTool() ?: throw UnsupportedOperationException("Unsupported: No translation tool found.")
        return toolProvider.translate(text, sourceLocale, targetLocale, callback)
    }
}

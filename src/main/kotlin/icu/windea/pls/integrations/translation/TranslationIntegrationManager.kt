package icu.windea.pls.integrations.translation

import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.translation.tools.TranslationToolProvider

object TranslationIntegrationManager {
    /** @see TranslationToolProvider */
    fun findTool(): TranslationToolProvider? {
        return TranslationToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    /** @see TranslationToolProvider.translate */
    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }

    /** @see TranslationToolProvider.translate */
    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }
}

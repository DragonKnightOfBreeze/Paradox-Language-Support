package icu.windea.pls.integrations.translation

import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.translation.tools.PlsTranslationToolProvider

object PlsTranslationManager {
    fun findTool(): PlsTranslationToolProvider? {
        return PlsTranslationToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }
}

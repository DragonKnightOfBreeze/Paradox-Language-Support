package icu.windea.pls.integrations.translation

import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.providers.*

object PlsTranslationManager {
    fun findTool(): PlsTranslationToolProvider? {
        return PlsTranslationToolProvider.EP_NAME.extensionList.findLast { it.supports() }
    }

    fun findRequiredTool(): PlsTranslationToolProvider {
        return findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
    }

    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback) {
        val tool = findRequiredTool()
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback) {
        val tool = findRequiredTool()
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }
}

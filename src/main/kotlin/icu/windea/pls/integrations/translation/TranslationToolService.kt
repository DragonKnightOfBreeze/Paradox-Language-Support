package icu.windea.pls.integrations.translation

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

@Service
class TranslationToolService {
    /** @see TranslationToolProvider */
    fun findTool(): TranslationToolProvider? {
        return TranslationToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    /** @see TranslationToolProvider.translate */
    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslationCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }

    /** @see TranslationToolProvider.translate */
    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslationCallback) {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return tool.translate(text, sourceLocale, targetLocale, callback)
    }

    companion object {
        @JvmStatic
        fun getInstance(): TranslationToolService = service()
    }
}

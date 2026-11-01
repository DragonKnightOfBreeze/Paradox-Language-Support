package icu.windea.pls.integrations.translation

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.ExceptionUtil
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.checkCancellation
import java.util.concurrent.ConcurrentHashMap

@Service
class TranslationToolService : Disposable {
    /** @see TranslationToolProvider */
    fun findTool(): TranslationToolProvider? {
        return TranslationToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    /** @see TranslationToolProvider.translate */
    @Throws(UnsupportedOperationException::class)
    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String): String? {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return with(tool) {
            doTranslate(text, sourceLocale, targetLocale)
        }
    }

    /** @see TranslationToolProvider.translate */
    @Throws(UnsupportedOperationException::class)
    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String? {
        val tool = findTool() ?: throw UnsupportedOperationException("Unsupported: No available translation tool found.")
        return with(tool) {
            doTranslate(text, sourceLocale, targetLocale)
        }
    }

    context(tool: TranslationToolProvider)
    private suspend fun doTranslate(text: String, sourceLocale: String?, targetLocale: String): String? {
        try {
            val result = tool.translate(text, sourceLocale, targetLocale)
            logOperation(tool) { "translate (sourceLocale=$sourceLocale, targetLocale=$targetLocale)" }
            return result
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "translate (sourceLocale=$sourceLocale, targetLocale=$targetLocale)" }
            throw e
        }
    }

    context(tool: TranslationToolProvider)
    private suspend fun doTranslate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String? {
        try {
            val result = tool.translate(text, sourceLocale, targetLocale)
            logOperation(tool) { "translate (sourceLocale=${sourceLocale?.name}, targetLocale=${targetLocale.name})" }
            return result
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "translate (sourceLocale=${sourceLocale?.name}, targetLocale=${targetLocale.name})" }
            throw e
        }
    }

    private fun logOperation(tool: TranslationToolProvider, error: Exception? = null, operation: () -> String) {
        if (error != null) {
            if (!warnedStacks.add(ExceptionUtil.currentStackTrace())) return
            logger.warn("[${tool.name}] Execute translation tool failed. Operation: ${operation()}.")
        } else {
            if (!logger.isDebugEnabled) return
            logger.debug("[${tool.name}] Execute translation tool success. Operation: ${operation()}.")
        }
    }

    override fun dispose() {
        warnedStacks.clear()
    }

    companion object {
        private val logger = logger<TranslationToolService>()
        private val warnedStacks = ConcurrentHashMap.newKeySet<String>()

        @JvmStatic
        fun getInstance(): TranslationToolService = service()
    }
}

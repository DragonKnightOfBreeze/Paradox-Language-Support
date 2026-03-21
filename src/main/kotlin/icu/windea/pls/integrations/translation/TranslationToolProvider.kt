package icu.windea.pls.integrations.translation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

/**
 * 提供翻译工具。用于翻译本地化文本。
 */
interface TranslationToolProvider {
    fun isAvailable(): Boolean

    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslationCallback)

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslationCallback)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<TranslationToolProvider>("icu.windea.pls.integrations.translationToolProvider")
    }
}

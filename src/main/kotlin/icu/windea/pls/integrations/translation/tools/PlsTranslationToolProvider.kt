package icu.windea.pls.integrations.translation.tools

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.integrations.translation.TranslateCallback

/**
 * 提供翻译工具。用于翻译本地化文本。
 */
interface PlsTranslationToolProvider {
    fun isAvailable(): Boolean

    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback)

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsTranslationToolProvider>("icu.windea.pls.integrations.translationToolProvider")
    }
}

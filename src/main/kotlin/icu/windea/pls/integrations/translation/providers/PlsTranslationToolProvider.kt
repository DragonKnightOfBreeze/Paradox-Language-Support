package icu.windea.pls.integrations.translation.providers

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.*

/**
 * 提供翻译工具，用于翻译本地化文本。
 */
interface PlsTranslationToolProvider {
    fun supports(): Boolean

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsTranslationToolProvider>("icu.windea.pls.integrations.translationToolProvider")
    }
}

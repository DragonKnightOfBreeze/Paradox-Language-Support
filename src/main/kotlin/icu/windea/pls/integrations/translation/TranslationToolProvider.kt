package icu.windea.pls.integrations.translation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.ExternalToolProvider

/**
 * 提供翻译工具。用于翻译本地化文本。
 */
interface TranslationToolProvider: ExternalToolProvider {
    @Throws(UnsupportedOperationException::class)
    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String): String?

    @Throws(UnsupportedOperationException::class)
    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<TranslationToolProvider>("icu.windea.pls.integrations.translationToolProvider")
    }
}

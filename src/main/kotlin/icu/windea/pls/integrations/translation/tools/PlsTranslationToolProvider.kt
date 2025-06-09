package icu.windea.pls.integrations.translation.tools

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.*

/**
 * 提供翻译工具。用于翻译本地化文本。
 *
 * 注意：具体的操作方法不会再次验证工具是否受支持（[supports]）。
 */
interface PlsTranslationToolProvider {
    fun supports(): Boolean

    suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback)

    suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsTranslationToolProvider>("icu.windea.pls.integrations.translationToolProvider")
    }
}

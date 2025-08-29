package icu.windea.pls.integrations.translation.tools

import cn.yiiguxing.plugin.translate.trans.Lang
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import cn.yiiguxing.plugin.translate.trans.TranslateListener
import cn.yiiguxing.plugin.translate.trans.TranslateService
import cn.yiiguxing.plugin.translate.trans.Translation
import com.intellij.openapi.application.UI
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.integrations.translation.TranslateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 参见：[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)
 */
class PlsTranslationPluginToolProvider : PlsTranslationToolProvider {
    override fun isAvailable(): Boolean {
        return true // see pls-extension-translation.xml
    }

    override suspend fun translate(text: String, sourceLocale: String?, targetLocale: String, callback: TranslateCallback) {
        val sourceLang = if(sourceLocale == null) Lang.AUTO else Lang[sourceLocale]
        val targetLang = Lang[targetLocale]
        val translateService = TranslateService.getInstance()

        //NOTE 使用 TranslateService 之前，必须先转到 EDT
        withContext(Dispatchers.UI) {
            translateService.translate(text, sourceLang, targetLang, object : TranslateListener {
                override fun onSuccess(translation: Translation) {
                    callback(translation.translation, null)
                }

                override fun onError(throwable: Throwable) {
                    callback(null, throwable)
                }
            })
        }
    }

    override suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig, callback: TranslateCallback) {
        val translateService = TranslateService.getInstance()
        val supportedSourceLanguages = translateService.translator.supportedSourceLanguages
        val supportedTargetLanguages = translateService.translator.supportedTargetLanguages
        val sourceLang = toLang(sourceLocale, supportedSourceLanguages)
        val targetLang = toLang(targetLocale, supportedTargetLanguages)

        //NOTE 使用 TranslateService 之前，必须先转到 EDT
        withContext(Dispatchers.UI) {
            translateService.translate(text, sourceLang, targetLang, object : TranslateListener {
                override fun onSuccess(translation: Translation) {
                    callback(translation.translation, null)
                }

                override fun onError(throwable: Throwable) {
                    callback(null, throwable)
                }
            })
        }
    }

    private fun toLang(localeConfig: CwtLocaleConfig?, supportedLangList: Collection<Lang> = emptyList()): Lang {
        if (localeConfig == null) return Lang.AUTO
        if (localeConfig.id == "l_default") {
            return Lang.default
        }
        for (code in localeConfig.codes) {
            val lang = Lang[code]
            if (lang.isExplicit() && lang in supportedLangList) return lang
        }
        return Lang.AUTO
    }
}

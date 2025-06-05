package icu.windea.pls.integrations.translation.providers

import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import icu.windea.pls.config.config.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * 参见：[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)
 */
class PlsTranslationPluginToolProvider : PlsTranslationToolProvider {
    override fun supports(): Boolean {
        return true // see pls-extension-translation.xml
    }

    override fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String? {
        val translateService = TranslateService.getInstance()
        val supportedSourceLanguages = translateService.translator.supportedSourceLanguages
        val supportedTargetLanguages = translateService.translator.supportedTargetLanguages
        val sourceLang = toLang(sourceLocale, supportedSourceLanguages)
        val targetLang = PlsTranslationPluginManager.toLang(targetLocale, supportedTargetLanguages)
        val countDownLatch = CountDownLatch(1)
        val resultRef = AtomicReference<String>()
        val errorRef = AtomicReference<Throwable>()
        translateService.translate(text, sourceLang, targetLang, object : TranslateListener {
            override fun onSuccess(translation: Translation) {
                translation.translation?.let { resultRef.set(it) }
                countDownLatch.countDown()
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        if(errorRef.get() != null) throw errorRef.get()
        return resultRef.get()
    }

    private fun toLang(localeConfig: CwtLocaleConfig?, supportedLangList: Collection<Lang> = emptyList()): Lang {
        if (localeConfig == null) return Lang.AUTO
        if (localeConfig.id == "l_default") {
            return Lang.Companion.default
        }
        for (code in localeConfig.codes) {
            val lang = Lang.Companion[code]
            if (lang.isExplicit() && lang in supportedLangList) return lang
        }
        return Lang.AUTO
    }
}

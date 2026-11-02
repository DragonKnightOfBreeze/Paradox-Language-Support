package icu.windea.pls.integrations.translation.providers

import cn.yiiguxing.plugin.translate.trans.Lang
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import cn.yiiguxing.plugin.translate.trans.TranslateListener
import cn.yiiguxing.plugin.translate.trans.TranslateService
import cn.yiiguxing.plugin.translate.trans.Translation
import com.intellij.openapi.application.UI
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.translation.TranslationToolProvider
import icu.windea.pls.lang.util.ParadoxLocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 参见：[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)
 */
@Suppress("UnstableApiUsage")
class TranslationPluginToolProvider : TranslationToolProvider {
    override val name = "translation-plugin"

    override fun isAvailable(): Boolean {
        return true // see: chronicle-extension-translation.xml
    }

    override suspend fun translate(text: String, sourceLocale: String?, targetLocale: String): String? {
        val sourceLang = if (sourceLocale == null) Lang.AUTO else Lang[sourceLocale]
        val targetLang = Lang[targetLocale]
        return translateWithCallback(text, sourceLang, targetLang)
    }

    override suspend fun translate(text: String, sourceLocale: CwtLocaleConfig?, targetLocale: CwtLocaleConfig): String? {
        val translateService = TranslateService.getInstance()
        val supportedSourceLanguages = translateService.translator.supportedSourceLanguages
        val supportedTargetLanguages = translateService.translator.supportedTargetLanguages
        val sourceLang = toLang(sourceLocale, supportedSourceLanguages)
        val targetLang = toLang(targetLocale, supportedTargetLanguages)
        return translateWithCallback(text, sourceLang, targetLang)
    }

    private fun toLang(localeConfig: CwtLocaleConfig?, supportedLangList: Collection<Lang> = emptyList()): Lang {
        if (localeConfig == null) return Lang.AUTO
        if (localeConfig.name == ParadoxLocaleManager.ID_DEFAULT) {
            return Lang.default
        }
        for (code in localeConfig.codes) {
            val lang = Lang[code]
            if (lang.isExplicit() && lang in supportedLangList) return lang
        }
        return Lang.AUTO
    }

    private suspend fun translateWithCallback(text: String, sourceLang: Lang, targetLang: Lang): String? {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                // NOTE 使用 `TranslateService` 之前，必须先转到 EDT
                withContext(Dispatchers.UI) {
                    val listener = object : TranslateListener {
                        override fun onSuccess(translation: Translation) = continuation.resume(translation.translation)
                        override fun onError(throwable: Throwable) = continuation.resumeWithException(throwable)
                    }
                    TranslateService.getInstance().translate(text, sourceLang, targetLang, listener)
                }
            }
        }
    }
}

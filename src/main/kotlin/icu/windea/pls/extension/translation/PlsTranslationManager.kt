package icu.windea.pls.extension.translation

import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

object PlsTranslationManager {
    fun toLang(localeConfig: CwtLocalisationLocaleConfig?, supportedLangList: Collection<Lang> = emptyList()): Lang {
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

    fun toTranslatableStringSnippets(element: ParadoxLocalisationProperty, supportedLangList: List<Lang>): List<TranslatableStringSnippet> {
        val text = element.text
        val lang = toLang(selectLocale(element), supportedLangList)
        run {
            val propertyValue = element.propertyValue ?: return@run
            val propertyValueTokenElement = element.propertyValue?.tokenElement ?: return@run
            if (!shouldTranslate(propertyValue)) return@run
            val start = element.startOffset
            val tokenStart = propertyValueTokenElement.startOffset
            val tokenEnd = propertyValueTokenElement.endOffset
            val snippets = mutableListOf<TranslatableStringSnippet>()
            snippets.add(TranslatableStringSnippet(text.substring(0, tokenStart - start), false, lang))
            snippets.add(TranslatableStringSnippet(text.substring(tokenStart - start, tokenEnd - start), true, lang))
            snippets.add(TranslatableStringSnippet(text.substring(tokenEnd - start), false, lang))
            return snippets
        }
        return listOf(TranslatableStringSnippet(text, false, lang))
    }

    fun shouldTranslate(element: ParadoxLocalisationPropertyValue): Boolean {
        return element.richTextList.any { shouldTranslate(it) }
    }

    fun shouldTranslate(element: ParadoxLocalisationRichText): Boolean {
        return when (element) {
            is ParadoxLocalisationString -> element.text.isNotBlank()
            is ParadoxLocalisationColorfulText -> element.richTextList.any { shouldTranslate(it) }
            is ParadoxLocalisationConceptCommand -> element.conceptText?.richTextList?.any { shouldTranslate(it) } ?: false
            is ParadoxLocalisationTextFormat -> element.textFormatText?.richTextList?.any { shouldTranslate(it) } ?: false
            else -> false
        }
    }
}

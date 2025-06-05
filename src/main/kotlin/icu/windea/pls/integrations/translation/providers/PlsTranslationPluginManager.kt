package icu.windea.pls.integrations.translation.providers

import cn.yiiguxing.plugin.translate.trans.Lang
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.integrations.translation.TranslatableStringSnippet
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText
import icu.windea.pls.localisation.psi.ParadoxLocalisationString
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat

object PlsTranslationPluginManager {
    fun toLang(localeConfig: CwtLocaleConfig?, supportedLangList: Collection<Lang> = emptyList()): Lang {
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

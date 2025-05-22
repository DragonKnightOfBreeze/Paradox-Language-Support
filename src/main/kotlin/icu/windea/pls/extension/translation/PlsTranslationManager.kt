package icu.windea.pls.extension.translation

import cn.yiiguxing.plugin.translate.trans.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

object PlsTranslationManager {
    fun toLang(localeConfig: CwtLocalisationLocaleConfig?): Lang? {
        if (localeConfig == null) return null
        if (localeConfig.id == "l_default") {
            //基于OS得到对应的语言区域，或者使用英文
            val userLanguage = System.getProperty("user.language").orEmpty()
            val locale = runCatchingCancelable { Lang[userLanguage] }.getOrNull()
            return locale
        }
        for (code in localeConfig.codes) {
            val locale = runCatchingCancelable { Lang[code] }.getOrNull()
            if (locale != null) return locale
        }
        return null
    }

    fun toTranslatableStringSnippets(element: ParadoxLocalisationProperty): List<TranslatableStringSnippet> {
        return runCatchingCancelable {
            val propertyValue = element.propertyValue ?: return emptyList()
            val start = element.startOffset
            val quoteStart = propertyValue.startOffset // _"
            val quoteEnd = propertyValue.endOffset // "_
            if (quoteEnd - quoteStart < 2) return emptyList()
            val snippets = mutableListOf<TranslatableStringSnippet>()
            val text = element.text
            snippets.add(TranslatableStringSnippet(text.substring(0, quoteStart + 1 - start), false))
            snippets.add(TranslatableStringSnippet(text.substring(quoteStart + 1 - start, quoteEnd - 1 - start), true))
            snippets.add(TranslatableStringSnippet(text.substring(quoteEnd - 1 - start), false))
            snippets
        }.getOrNull().orEmpty()
    }
}

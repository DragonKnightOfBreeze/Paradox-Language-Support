@file:Suppress("unused")

package icu.windea.pls.extension.translation

import cn.yiiguxing.plugin.translate.trans.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.localisation.psi.*

fun CwtLocalisationLocaleConfig.toLang(): Lang? {
	if(this.id == "l_default") {
		//基于OS得到对应的语言区域，或者使用英文
		val userLanguage = System.getProperty("user.language") ?: return null
		return runCatching { Lang[userLanguage] }.getOrNull()
	}
	for(code in this.codes) {
		try {
			return Lang[code]
		} catch(e: Exception) {
			//ignored
		}
	}
	return null
}

fun ParadoxLocalisationProperty.toTranslatableStringSnippets(): TranslatableStringSnippets? {
	try {
		val propertyValue = this.propertyValue ?: return null
		val start = startOffset
		val quoteStart = propertyValue.startOffset // _"
		val quoteEnd = propertyValue.endOffset // "_
		if(quoteEnd - quoteStart < 2) return null
		val snippets = mutableListOf<TranslatableStringSnippet>()
		val text = text
		snippets.add(TranslatableStringSnippet(text.substring(0, quoteStart + 1 - start), false))
		snippets.add(TranslatableStringSnippet(text.substring(quoteStart + 1 - start, quoteEnd - 1 - start), true))
		snippets.add(TranslatableStringSnippet(text.substring(quoteEnd - 1 - start), false))
		return TranslatableStringSnippets(snippets)
	} catch(e: Exception) {
		return null
	}
}
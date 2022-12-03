@file:Suppress("unused")

package icu.windea.pls.extension.translation

import cn.yiiguxing.plugin.translate.*
import cn.yiiguxing.plugin.translate.action.*
import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.localisation.psi.*
import java.util.*

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
		val start = textRange.startOffset
		val quoteStart = propertyValue.textRange.startOffset // _"
		val quoteEnd = propertyValue.textRange.endOffset // "_
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


//cn.yiiguxing.plugin.translate.trans.TranslationNotifications

fun TranslationNotifications.showTranslationErrorNotification(
	project: Project?,
	title: String,
	content: String,
	throwableList: List<Throwable>,
	vararg actions: AnAction
) {
	val actionList = LinkedList<AnAction>()
	for(throwable in throwableList) {
		val errorInfo = (throwable as? TranslateException)?.errorInfo
		// actions的折叠是从左往右折叠的
		errorInfo?.continueActions?.let { actionList += it }
	}
	
	actionList.addAll(actions)
	actionList.add(TranslatorActionGroup({ message("action.SwitchTranslatorAction.text") }))
	
	for(throwable in throwableList) {
		if(throwable !is TranslateException) {
			// 将异常写入IDE异常池，以便用户反馈
			logger<TranslationNotifications>().e("Translation error: ${throwable.message}", throwable)
		}
	}
	Notifications.showErrorNotification(project, title, content, *actionList.toTypedArray())
}

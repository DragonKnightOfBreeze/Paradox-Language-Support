@file:Suppress("unused")

package icu.windea.pls.translation

import cn.yiiguxing.plugin.translate.*
import cn.yiiguxing.plugin.translate.action.*
import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import icu.windea.pls.config.internal.config.*
import java.util.*

fun ParadoxLocaleConfig.toLang(): Lang? {
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
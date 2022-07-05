package icu.windea.pls.localisation.intentions

import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.util.*
import com.intellij.codeInsight.intention.*
import com.intellij.ide.plugins.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.ex.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

//https://github.com/YiiGuxing/TranslationPlugin/blob/master/src/main/kotlin/cn/yiiguxing/plugin/translate/action/TranslateAndReplaceAction.kt

/**
 * 复制本地化到剪贴板并在复制之前转化语言区域的意向。（鼠标位置对应的本地化，或者鼠标选取范围涉及到的所有本地化）
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 *
 * 可以配置是否需要尝试翻译本地化文本。
 */
class CopyLocalisationForLocaleIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.copyLocalisationForLocale")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxLocalisationLanguage) return false
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		return if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			originalElement?.parentOfType<ParadoxLocalisationProperty>() != null
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return false
			val originalEndElement = file.findElementAt(selectionEnd) ?: return false
			hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
		}
	}
	
	//在翻译之前，要讲特殊标记用<>包围起来，这样翻译后就可以保留特殊标记（期望如此）
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxLocalisationLanguage) return
		val selectionStart = editor.selectionModel.selectionStart
		val selectionEnd = editor.selectionModel.selectionEnd
		val elements = if(selectionStart == selectionEnd) {
			val originalElement = file.findElementAt(selectionStart)
			val element = originalElement?.parentOfType<ParadoxLocalisationProperty>() ?: return
			listOf(element)
		} else {
			val originalStartElement = file.findElementAt(selectionStart) ?: return
			val originalEndElement = file.findElementAt(selectionEnd) ?: return
			findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
		}
		if(elements.isEmpty()) return
		
		val localeDialog = SelectParadoxLocaleDialog(preferredParadoxLocale())
		if(!localeDialog.showAndGet()) return
		
		//Determines whether the translation plug-in is enabled
		val isEnabled = PluginManagerCore.isPluginInstalled(translationPluginId) && !PluginManagerCore.isDisabled(translationPluginId))
		if(!isEnabled) {
			Notifications.showWarningNotification(
				PlsBundle.message("translate.notification.pluginNotEnabled.title"), 
				PlsBundle.message("translate.notification.pluginNotEnabled.description"),
				project, "pls"
			)
		}
		
		val targetLocale = localeDialog.locale
		val targetLang = targetLocale.languageTag.let { runCatching { Lang[it] }.getOrNull() }
		val textList = elements.map { element ->
			if(targetLang == null) return@map element.text
			val sourceLang = element.localeConfig?.languageTag?.let { runCatching { Lang[it] }.getOrNull() } ?: return@map element.text
			if(sourceLang == targetLang) return@map element.text
			if(!isEnabled) return@map element.text
			
			val key = element.name
			val indicatorTitle = PlsBundle.message("translate.indicator.translate.title", key, targetLocale)
			val progressIndicator = BackgroundableProcessIndicator(project, indicatorTitle, null, "", true)
			progressIndicator.text = PlsBundle.message("translate.indicator.translate.text1", key)
			progressIndicator.text2 = PlsBundle.message("translate.indicator.translate.text2", text.processBeforeTranslate() ?: text)
			progressIndicator.addStateDelegate(ProcessIndicatorDelegate(progressIndicator))
			
			var resultText = element.text
			TranslateService.translate(element.text, sourceLang, targetLang, object : TranslateListener {
				override fun onSuccess(translation: Translation) {
					if(checkProcessCanceledAndEditorDisposed(progressIndicator, project, editor)) return
					
					progressIndicator.processFinish()
					resultText = translation.translation
				}
				
				override fun onError(throwable: Throwable) {
					if(checkProcessCanceledAndEditorDisposed(progressIndicator, project, editor)) return
					
					progressIndicator.processFinish()
					TranslationNotifications.showTranslationErrorNotification(project, PlsBundle.message("translate.notification.translate.failed.title", key, targetLocale), null, throwable)
				}
			})
			resultText
			
		}
		val finalText = textList.joinToString("\n")
		CopyPasteManager.getInstance().setContents(StringSelection(finalText))
	}
	
	fun checkProcessCanceledAndEditorDisposed(progressIndicator: BackgroundableProcessIndicator, project: Project?, editor: Editor?): Boolean {
		if(progressIndicator.isCanceled) {
			// no need to finish the progress indicator,
			// because it's already finished in the delegate.
			return true
		}
		if((project != null && project.isDisposed) || editor.let { it == null || it.isDisposed }) {
			progressIndicator.processFinish()
			return true
		}
		return false
	}
	
	private class ProcessIndicatorDelegate(
		private val progressIndicator: BackgroundableProcessIndicator,
	) : EmptyProgressIndicatorBase(), ProgressIndicatorEx {
		override fun cancel() {
			// 在用户取消的时候使`progressIndicator`立即结束并且不再显示，否则需要等待任务结束才能跟着结束
			progressIndicator.processFinish()
		}
		
		override fun isCanceled(): Boolean = true
		override fun finish(task: TaskInfo) = Unit
		override fun isFinished(task: TaskInfo): Boolean = true
		override fun wasStarted(): Boolean = false
		override fun processFinish() = Unit
		override fun initStateFrom(indicator: ProgressIndicator) = Unit
		
		override fun addStateDelegate(delegate: ProgressIndicatorEx) {
			throw UnsupportedOperationException()
		}
	}
}
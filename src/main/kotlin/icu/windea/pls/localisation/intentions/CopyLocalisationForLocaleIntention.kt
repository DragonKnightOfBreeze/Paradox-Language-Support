package icu.windea.pls.localisation.intentions

import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.util.*
import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.notification.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.wm.ex.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.translation.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*

//cn.yiiguxing.plugin.translate.action.TranslateAndReplaceAction

/**
 * 复制本地化到剪贴板并在这之前尝试将本地化文本翻译到指定的语言区域。（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
@WithExtension("cn.yiiguxing.plugin.translate")
class CopyLocalisationForLocaleIntention : IntentionAction, PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH
    
    override fun getText() = PlsBundle.message("intention.localisation.copyLocalisationForLocale")
    
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
            val originalEndElement = file.findElementAt(selectionEnd)
            hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
    }
    
    //TODO 采用文档翻译 （将需要翻译的文本中的特殊标记用<span translate="no"></span>包围起来，然后再进行翻译）
    //cn.yiiguxing.plugin.translate.trans.google.GoogleTranslator.translateDocumentation
    
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
            val originalEndElement = file.findElementAt(selectionEnd)
            findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
        if(elements.isEmpty()) return
        
        val onChosen = { selected: CwtLocalisationLocaleConfig ->
            val targetLocale = selected
            val targetLang = targetLocale.toLang()
            val failedKeys = mutableSetOf<String>()
            val throwableList = mutableListOf<Throwable>()
            val textList = elements.map { element ->
                if(targetLang == null) return@map element.text
                val sourceLang = selectLocale(element)?.toLang() ?: return@map element.text
                if(sourceLang == targetLang) return@map element.text
                
                val key = element.name
                val indicatorTitle = PlsTranslationBundle.message("indicator.translate.title", key, targetLocale)
                val progressIndicator = BackgroundableProcessIndicator(project, indicatorTitle, null, "", true)
                progressIndicator.text = PlsTranslationBundle.message("indicator.translate.text1", key)
                progressIndicator.text2 = PlsTranslationBundle.message("indicator.translate.text2", text) //不过滤任何字符
                progressIndicator.addStateDelegate(ProcessIndicatorDelegate(progressIndicator))
                
                val snippets = element.toTranslatableStringSnippets() ?: return@map element.text
                snippets.forEach { snippet ->
                    if(!snippet.shouldTranslate) return@forEach
                    TranslateService.translate(snippet.text, sourceLang, targetLang, object : TranslateListener {
                        override fun onSuccess(translation: Translation) {
                            if(checkProcessCanceledAndEditorDisposed(progressIndicator, project, editor)) return
                            
                            progressIndicator.processFinish()
                            translation.translation?.also { snippet.text = it }
                        }
                        
                        override fun onError(throwable: Throwable) {
                            if(checkProcessCanceledAndEditorDisposed(progressIndicator, project, editor)) return
                            
                            progressIndicator.processFinish()
                            failedKeys.add(key)
                            throwableList.add(throwable)
                        }
                    })
                }
                snippets.toString()
            }
            if(failedKeys.isNotEmpty()) {
                val failedKeysText = failedKeys.take(PlsConstants.keysTruncateLimit).joinToString { "'<code>$it</code>'" } + if(failedKeys.size > PlsConstants.keysTruncateLimit) ", ..." else ""
                TranslationNotifications.showTranslationErrorNotification(
                    project,
                    PlsTranslationBundle.message("notification.translate.failed.title"),
                    PlsTranslationBundle.message("notification.translate.failed.content", failedKeysText, targetLocale),
                    throwableList.first() // first only
                )
            }
            
            val finalText = textList.joinToString("\n")
            CopyPasteManager.getInstance().setContents(StringSelection(finalText))
            
            val keys = elements.mapTo(mutableSetOf()) { it.name }
            val keysText = keys.take(PlsConstants.keysTruncateLimit).joinToString { "'$it'" } + if(keys.size > PlsConstants.keysTruncateLimit) ", ..." else ""
            NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                PlsBundle.message("notification.copyLocalisationForLocale.success.title"),
                PlsBundle.message("notification.copyLocalisationForLocale.success.content", keysText, targetLocale),
                NotificationType.INFORMATION
            ).notify(project)
        }
        
        val selectedLocale = ParadoxLocaleHandler.getPreferredLocale()
        val allLocales = ParadoxLocaleHandler.getLocaleConfigs()
        val localePopup = ParadoxLocalePopup(selectedLocale, allLocales, onChosen = onChosen)
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }
    
    private fun checkProcessCanceledAndEditorDisposed(progressIndicator: BackgroundableProcessIndicator, project: Project?, editor: Editor?): Boolean {
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
    
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
    
    override fun startInWriteAction() = false
}

package icu.windea.pls.lang.intentions.localisation

import cn.yiiguxing.plugin.translate.trans.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.*
import com.intellij.openapi.progress.util.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.translation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*
import java.lang.ref.*

/**
 * 复制翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
@WithExtension("cn.yiiguxing.plugin.translate")
class CopyTranslatedLocalisationIntention : CopyLocalisationIntention() {
    override fun getFamilyName() = PlsBundle.message("intention.copyTranslatedLocalisation")

    override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        if (editor == null || file == null) return
        val onChosen = { selected: CwtLocalisationLocaleConfig ->
            try {
                val textToCopy = doTranslate(project, editor, elements, selected)
                createNotification(PlsBundle.message("intention.copyTranslatedLocalisation.notification.success", selected), NotificationType.INFORMATION).notify(project)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            } catch (e: Throwable) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                createNotification(PlsBundle.message("intention.copyTranslatedLocalisation.notification.failed", selected), NotificationType.WARNING).notify(project)
            }
        }
        val allLocales = ParadoxLocaleManager.getLocaleConfigs()
        val localePopup = ParadoxLocaleListPopup(allLocales, onChosen = onChosen)
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }

    private fun doTranslate(project: Project, editor: Editor, elements: List<ParadoxLocalisationProperty>, targetLocaleConfig: CwtLocalisationLocaleConfig): String? {
        val targetLang = PlsTranslationManager.toLang(targetLocaleConfig)
        val failedKeys = mutableSetOf<String>()
        val throwableList = mutableListOf<Throwable>()
        val total = elements.size
        val textList = elements.map { element ->
            if (targetLang == null) return@map element.text
            val sourceLocaleConfig = selectLocale(element)
            val sourceLang = PlsTranslationManager.toLang(sourceLocaleConfig)
            if (sourceLang == null) return@map element.text
            if (sourceLang == targetLang) return@map element.text

            val snippets = PlsTranslationManager.toTranslatableStringSnippets(element)
            if (snippets.isEmpty()) return@map element.text

            val editorRef = WeakReference(editor)
            val indicator = Indicator(project, editorRef, total, targetLocaleConfig)
            val translateService = TranslateService.getInstance()
            snippets.forEach { snippet ->
                if (indicator.checkProcessCanceledAndEditorDisposed()) return null
                if (!snippet.shouldTranslate) return@forEach
                translateService.translate(snippet.text, sourceLang, targetLang, object : TranslateListener {
                    override fun onSuccess(translation: Translation) {
                        if (indicator.checkProcessCanceledAndEditorDisposed()) return
                        translation.translation?.also { snippet.text = it }
                        indicator.updateProgress()
                    }

                    override fun onError(throwable: Throwable) {
                        if (indicator.checkProcessCanceledAndEditorDisposed()) return
                        throw throwable
                    }
                })
            }
            indicator.processFinish()
            snippets.joinToString("") { it.text }
        }
        return textList.joinToString("\n")
    }

    private class Indicator(
        val project: Project?,
        val editorRef: WeakReference<Editor>,
        val total: Int,
        localeConfig: CwtLocalisationLocaleConfig
    ) : BackgroundableProcessIndicator(project, PlsBundle.message("intention.copyTranslatedLocalisation.indicator.title", localeConfig), null, null, true) {
        var current = 0

        init {
            initDelegate()
            start()
            setProgressText()
        }

        private fun initDelegate() {
            addStateDelegate(object : AbstractProgressIndicatorExBase() {
                override fun cancel() {
                    // 在用户取消的时候使`progressIndicator`立即结束并且不再显示
                    this@Indicator.processFinish()
                }
            })
        }

        fun updateProgress() {
            if (current >= total) return
            current++
            setProgressText()
        }

        private fun setProgressText() {
            text = PlsBundle.message("intention.copyTranslatedLocalisation.indicator.text", current, total)
        }

        fun checkProcessCanceledAndEditorDisposed(): Boolean {
            if (isCanceled) {
                // no need to finish the progress indicator,
                // because it's already finished in the delegate.
                return true
            }
            if ((project != null && project.isDisposed) || editorRef.get().let { it == null || it.isDisposed }) {
                processFinish()
                return true
            }
            return false
        }
    }
}

package icu.windea.pls.lang.intentions.localisation

import cn.yiiguxing.plugin.translate.trans.*
import cn.yiiguxing.plugin.translate.trans.Lang.Companion.isExplicit
import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
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
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * 复制翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
@WithExtension(PlsConstants.Ids.translationPlugin)
class CopyTranslatedLocalisationIntention : CopyLocalisationIntention() {
    override fun getFamilyName() = PlsBundle.message("intention.copyTranslatedLocalisation")

    override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        if (editor == null || file == null) return
        val allLocales = ParadoxLocaleManager.getLocaleConfigs()
        val localePopup = ParadoxLocaleListPopup(allLocales)
        localePopup.doFinalStep { doTranslate(project, editor, elements, localePopup.selectedLocale) }
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }

    private fun doTranslate(project: Project, editor: Editor, elements: List<ParadoxLocalisationProperty>, targetLocale: CwtLocalisationLocaleConfig?) {
        if (targetLocale == null) return
        val translateService = TranslateService.getInstance()
        val supportedTargetLangList = translateService.translator.supportedTargetLanguages
        val targetLang = PlsTranslationManager.toLang(targetLocale, supportedTargetLangList)
        val supportedSourceLangList = translateService.translator.supportedSourceLanguages
        val snippetsList = elements.map { element -> PlsTranslationManager.toTranslatableStringSnippets(element, supportedSourceLangList)}
        if (snippetsList.isEmpty()) return

        val snippetsToTranslate = mutableListOf<TranslatableStringSnippet>()
        for (snippets in snippetsList) {
            for (snippet in snippets) {
                if (!snippet.shouldTranslate) continue
                if (!targetLang.isExplicit()) continue
                if (snippet.lang == targetLang) continue
                snippetsToTranslate.add(snippet)
            }
        }

        if (snippetsToTranslate.isEmpty()) {
            val textToCopy = snippetsList.joinToString("\n") { snippets -> snippets.joinToString("") { snippet -> snippet.text } }
            createNotification(PlsBundle.message("intention.copyTranslatedLocalisation.notification.1", targetLocale), NotificationType.INFORMATION).notify(project)
            CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            return
        }

        val editorRef = WeakReference(editor)
        val indicator = Indicator(project, editorRef, snippetsToTranslate.size, targetLocale)
        val errorRef = AtomicReference<Throwable>()
        val countDownLatch = CountDownLatch(snippetsToTranslate.size)
        for (snippet in snippetsToTranslate) {
            if (indicator.checkProcessCanceledAndEditorDisposed()) return
            translateService.translate(snippet.text, snippet.lang, targetLang, object : TranslateListener {
                override fun onSuccess(translation: Translation) {
                    if (indicator.checkProcessCanceledAndEditorDisposed()) return
                    translation.translation?.also { snippet.text = it }
                    indicator.updateProgress()
                    countDownLatch.countDown()
                }

                override fun onError(throwable: Throwable) {
                    if (indicator.checkProcessCanceledAndEditorDisposed()) return
                    errorRef.compareAndSet(null, throwable)
                    countDownLatch.countDown()
                }
            })
        }

        ApplicationManager.getApplication().executeOnPooledThread action@{
            countDownLatch.await()
            indicator.processFinish()
            val error = errorRef.get()
            if (error == null) {
                val textToCopy = snippetsList.joinToString("\n") { snippets -> snippets.joinToString("") { snippet -> snippet.text } }
                createNotification(PlsBundle.message("intention.copyTranslatedLocalisation.notification.0", targetLocale), NotificationType.INFORMATION).notify(project)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            } else {
                thisLogger().warn(error)
                createNotification(PlsBundle.message("intention.copyTranslatedLocalisation.notification.2", targetLocale), NotificationType.WARNING).notify(project)
                return@action
            }
        }
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

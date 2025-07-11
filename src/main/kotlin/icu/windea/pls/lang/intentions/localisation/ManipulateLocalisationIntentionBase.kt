package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*

/**
 * 用于处理本地化的一类意图。
 *
 * 光标处于以下位置时，此类意图可用：
 *
 * * 光标在语言区域PSI（[ParadoxLocalisationLocale]）中 - 此时处理此语言区域下的所有本地化
 * * 光标选择范围涉及到本地化属性PSI（[ParadoxLocalisationProperty]） - 此时处理涉及到的所有本地化
 */
abstract class ManipulateLocalisationIntentionBase<C> : IntentionAction, DumbAware {
    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        if (file !is ParadoxLocalisationFile) return false
        val hasElements = hasElements(editor, file)
        return hasElements
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        if (file !is ParadoxLocalisationFile) return
        val elements = findElements(editor, file)
        if (elements.isEmpty()) return
        doInvoke(project, editor, file, elements)
    }

    //默认不显示预览，因为可能涉及异步调用
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    private fun hasElements(editor: Editor, file: PsiFile): Boolean {
        val localeElement = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
        if (localeElement != null) return true

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        if (file.language !is ParadoxLocalisationLanguage) return false
        if (selectionStart == selectionEnd) {
            val originalElement = file.findElementAt(selectionStart)
            return originalElement?.parentOfType<ParadoxLocalisationProperty>() != null
        } else {
            val originalStartElement = file.findElementAt(selectionStart) ?: return false
            val originalEndElement = file.findElementAt(selectionEnd)
            return hasLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
    }

    private fun findElements(editor: Editor, file: PsiFile): List<ParadoxLocalisationProperty> {
        val localeElement = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
        if (localeElement != null) return localeElement.parent?.castOrNull<ParadoxLocalisationPropertyList>()?.propertyList.orEmpty()

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        if (selectionStart == selectionEnd) {
            val originalElement = file.findElementAt(selectionStart)
            return originalElement?.parentOfType<ParadoxLocalisationProperty>().toSingletonListOrEmpty()
        } else {
            val originalStartElement = file.findElementAt(selectionStart) ?: return emptyList()
            val originalEndElement = file.findElementAt(selectionEnd)
            return findLocalisationPropertiesBetween(originalStartElement, originalEndElement)
        }
    }

    protected abstract fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>)

    protected fun doHandleAsync(project: Project, file: PsiFile?, context: C) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            doHandle(project, file, context)
        }
    }

    protected abstract suspend fun doHandle(project: Project, file: PsiFile?, context: C)

    abstract class Default : ManipulateLocalisationIntentionBase<Default.Context>() {
        final override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
            doHandleAsync(project, file, Context(elements))
        }

        data class Context(
            val elements: List<ParadoxLocalisationProperty>
        )
    }

    abstract class WithLocalePopup : ManipulateLocalisationIntentionBase<WithLocalePopup.Context>() {
        protected open fun createLocalePopup(project: Project, editor: Editor?, file: PsiFile?): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        final override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
            if (editor == null) return
            val localePopup = createLocalePopup(project, editor, file)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                doHandleAsync(project, file, Context(elements, selected))
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
        }

        data class Context(
            val elements: List<ParadoxLocalisationProperty>,
            val selectedLocale: CwtLocaleConfig
        )
    }

    abstract class WithPopup<T> : ManipulateLocalisationIntentionBase<WithPopup.Context<T>>() {
        protected abstract fun createPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (T) -> Unit): JBPopup?

        final override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
            if (editor == null) return
            val popup = createPopup(project, editor, file) {
                doHandleAsync(project, file, Context(elements, it))
            }
            if (popup != null) {
                popup.showInBestPositionFor(editor)
            } else {
                doHandleAsync(project, file, Context(elements, null))
            }
        }

        data class Context<T>(
            val elements: List<ParadoxLocalisationProperty>,
            val data: T?
        )
    }

    abstract class WithLocalePopupAndPopup<T> : ManipulateLocalisationIntentionBase<WithLocalePopupAndPopup.Context<T>>() {
        protected open fun createLocalePopup(project: Project, editor: Editor?, file: PsiFile?): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        protected abstract fun createPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (T) -> Unit): JBPopup?

        final override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
            if (editor == null) return
            val localePopup = createLocalePopup(project, editor, file)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                val popup = createPopup(project, editor, file) {
                    doHandleAsync(project, file, Context(elements, selected, it))
                }
                if (popup != null) {
                    popup.showInBestPositionFor(editor)
                } else {
                    doHandleAsync(project, file, Context(elements, selected, null))
                }
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
        }

        data class Context<T>(
            val elements: List<ParadoxLocalisationProperty>,
            val selectedLocale: CwtLocaleConfig,
            val data: T?
        )
    }

    object Messages {
        fun success() = PlsBundle.message("intention.manipulateLocalisation.status.0")
        fun failed() = PlsBundle.message("intention.manipulateLocalisation.status.1")
        fun partialSuccess() = PlsBundle.message("intention.manipulateLocalisation.status.2")
    }
}

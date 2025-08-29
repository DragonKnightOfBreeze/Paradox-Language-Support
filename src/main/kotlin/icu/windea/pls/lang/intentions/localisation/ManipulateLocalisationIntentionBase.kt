package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.lang.ui.locale.ParadoxLocaleListPopup
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.dataFlow.ParadoxLocalisationSequence
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import kotlinx.coroutines.launch

/**
 * 用于处理本地化的一类意图。
 *
 * 光标处于以下位置时，此类意图可用：
 *
 * * 光标在语言区域PSI（[ParadoxLocalisationLocale]）中 - 此时处理此语言区域下的所有本地化
 * * 光标选择范围涉及到本地化属性PSI（[ParadoxLocalisationProperty]） - 此时处理涉及到的所有本地化
 */
abstract class ManipulateLocalisationIntentionBase<C> : IntentionAction {
    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (file !is ParadoxLocalisationFile) return false
        val elements = findElements(editor, file)
        return elements.any()
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is ParadoxLocalisationFile) return
        val elements = findElements(editor, file)
        if (elements.none()) return
        doInvoke(project, editor, file, elements)
    }

    //默认不显示预览，因为可能涉及异步调用
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false

    protected open fun findElements(editor: Editor, file: ParadoxLocalisationFile): ParadoxLocalisationSequence {
        return ParadoxLocalisationManipulator.buildSelectedSequence(editor, file)
    }

    protected abstract fun doInvoke(project: Project, editor: Editor, file: PsiFile, elements: ParadoxLocalisationSequence)

    protected fun doHandleAsync(project: Project, file: PsiFile, context: C) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            doHandle(project, file, context)
        }
    }

    protected abstract suspend fun doHandle(project: Project, file: PsiFile, context: C)

    abstract class Default : ManipulateLocalisationIntentionBase<Default.Context>() {
        final override fun doInvoke(project: Project, editor: Editor, file: PsiFile, elements: ParadoxLocalisationSequence) {
            doHandleAsync(project, file, Context(elements))
        }

        data class Context(
            val elements: ParadoxLocalisationSequence
        )
    }

    abstract class WithLocalePopup : ManipulateLocalisationIntentionBase<WithLocalePopup.Context>() {
        protected open fun createLocalePopup(project: Project, editor: Editor, file: PsiFile): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        final override fun doInvoke(project: Project, editor: Editor, file: PsiFile, elements: ParadoxLocalisationSequence) {
            val localePopup = createLocalePopup(project, editor, file)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                doHandleAsync(project, file, Context(elements, selected))
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
        }

        data class Context(
            val elements: ParadoxLocalisationSequence,
            val selectedLocale: CwtLocaleConfig
        )
    }

    abstract class WithPopup<T> : ManipulateLocalisationIntentionBase<WithPopup.Context<T>>() {
        protected abstract fun createPopup(project: Project, editor: Editor, file: PsiFile, callback: (T) -> Unit): JBPopup?

        final override fun doInvoke(project: Project, editor: Editor, file: PsiFile, elements: ParadoxLocalisationSequence) {
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
            val elements: ParadoxLocalisationSequence,
            val data: T?
        )
    }

    abstract class WithLocalePopupAndPopup<T> : ManipulateLocalisationIntentionBase<WithLocalePopupAndPopup.Context<T>>() {
        protected open fun createLocalePopup(project: Project, editor: Editor, file: PsiFile): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        protected abstract fun createPopup(project: Project, editor: Editor, file: PsiFile, callback: (T) -> Unit): JBPopup?

        final override fun doInvoke(project: Project, editor: Editor, file: PsiFile, elements: ParadoxLocalisationSequence) {
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
            val elements: ParadoxLocalisationSequence,
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

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
abstract class ManipulateLocalisationIntentionBase : IntentionAction, DumbAware {
    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val hasElements = hasElements(editor, file)
        return hasElements
    }

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

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val elements = findElements(editor, file)
        if (elements.isEmpty()) return
        doInvoke(project, editor, file, elements)
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

    protected open fun createLocalePopup(): ParadoxLocaleListPopup? {
        val allLocales = ParadoxLocaleManager.getLocaleConfigs()
        return ParadoxLocaleListPopup(allLocales)
    }

    protected open fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        val localePopup = createLocalePopup()
        if (localePopup == null) {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                doHandle(project, file, elements, null)
            }
        } else {
            if (editor == null) return
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                val coroutineScope = PlsFacade.getCoroutineScope(project)
                coroutineScope.launch {
                    doHandle(project, file, elements, selected)
                }
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
        }
    }

    protected abstract suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?)

    //默认不显示预览，因为可能涉及异步调用
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}

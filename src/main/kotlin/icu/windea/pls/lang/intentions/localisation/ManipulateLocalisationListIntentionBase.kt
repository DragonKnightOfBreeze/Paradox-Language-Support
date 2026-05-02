package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 用于处理本地化列表的一类意图。
 */
abstract class ManipulateLocalisationListIntentionBase : IntentionAction {
    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (file !is ParadoxLocalisationFile) return false
        val element = findElement(editor, file)
        return element != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is ParadoxLocalisationFile) return
        val elements = findElement(editor, file) ?: return
        doInvoke(project, editor, file, elements)
    }

    override fun startInWriteAction() = false

    protected open fun findElement(editor: Editor, file: ParadoxLocalisationFile): ParadoxLocalisationPropertyList? {
        val contextElement = file.findElementAt(editor.caretModel.offset) ?: return null
        val contextElementType = contextElement.elementType
        if (contextElementType != LOCALE_TOKEN && contextElementType != COLON) return null
        return contextElement.parent?.parent as? ParadoxLocalisationPropertyList
    }

    protected abstract fun doInvoke(project: Project, editor: Editor, file: PsiFile, element: ParadoxLocalisationPropertyList)
}

package icu.windea.pls.lang.refactoring.inline

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.inline.InlineOptionsDialog
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile

class ParadoxInlineScriptInlineDialog(
    project: Project,
    private val element: ParadoxScriptFile,
    private val reference: PsiReference?,
    private val editor: Editor?
) : InlineOptionsDialog(project, true, element) {
    private val optimizedScope = ParadoxSearchScope.fromElement(element)
        ?.withFileTypes(ParadoxScriptFileType)
        ?.intersectWith(GlobalSearchScope.projectScope(project))
        ?: GlobalSearchScope.projectScope(project)

    init {
        title = PlsBundle.message("title.inline.inlineScript")
        myInvokedOnReference = reference != null
        init()
        helpAction.isEnabled = false
    }

    override fun getNameLabelText(): String {
        val name = ParadoxInlineScriptManager.getInlineScriptExpression(element).or.anonymous()
        return PlsBundle.message("inline.inlineScript.label", name)
    }

    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.inlineScript.inline.this")
    }

    override fun getInlineAllText(): String {
        return if (element.isWritable) PlsBundle.message("inline.inlineScript.inline.all.remove")
        else PlsBundle.message("inline.inlineScript.inline.all")
    }

    override fun getKeepTheDeclarationText(): String {
        return if (element.isWritable) PlsBundle.message("inline.inlineScript.inline.all.keep")
        else super.getKeepTheDeclarationText()
    }

    override fun allowInlineAll(): Boolean {
        return true
    }

    override fun isInlineThis(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineInlineScriptThis
    }

    override fun isKeepTheDeclarationByDefault(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineInlineScriptKeep
    }

    override fun doAction() {
        val processor = ParadoxInlineScriptInlineProcessor(project, optimizedScope, element, reference, editor, isInlineThisOnly, isKeepTheDeclaration())
        invokeRefactoring(processor)
        val settings = ParadoxRefactoringSettings.getInstance()
        if (myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineInlineScriptThis = isInlineThisOnly
        }
        if (myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineInlineScriptKeep = isKeepTheDeclaration()
        }
    }
}

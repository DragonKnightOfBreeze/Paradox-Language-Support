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
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptedVariableInlineDialog(
    project: Project,
    private val element: ParadoxScriptScriptedVariable,
    private val reference: PsiReference?,
    private val editor: Editor?
) : InlineOptionsDialog(project, true, element) {
    private val optimizedScope = when {
        ParadoxPsiManager.isGlobalScriptedVariable(element) -> ParadoxSearchScope.fromElement(element)
            ?.withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType)
            ?.intersectWith(GlobalSearchScope.projectScope(project))
            ?: GlobalSearchScope.projectScope(project)
        else -> GlobalSearchScope.fileScope(element.containingFile)
    }

    init {
        title = PlsBundle.message("title.inline.scriptedVariable")
        myInvokedOnReference = reference != null
        init()
        helpAction.isEnabled = false
    }

    override fun getNameLabelText(): String {
        val name = element.name.or.anonymous()
        return PlsBundle.message("inline.scriptedVariable.label", name)
    }

    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.scriptedVariable.inline.this")
    }

    override fun getInlineAllText(): String {
        return if (element.isWritable) PlsBundle.message("inline.scriptedVariable.inline.all.remove")
        else PlsBundle.message("inline.scriptedVariable.inline.all")
    }

    override fun getKeepTheDeclarationText(): String {
        return if (element.isWritable) PlsBundle.message("inline.scriptedVariable.inline.all.keep")
        else super.getKeepTheDeclarationText()
    }

    override fun allowInlineAll(): Boolean {
        return true
    }

    override fun isInlineThis(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedVariableThis
    }

    override fun isKeepTheDeclarationByDefault(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedVariableKeep
    }

    override fun doAction() {
        val processor = ParadoxScriptedVariableInlineProcessor(project, optimizedScope, element, reference, editor, isInlineThisOnly, isKeepTheDeclaration())
        invokeRefactoring(processor)
        val settings = ParadoxRefactoringSettings.getInstance()
        if (myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineScriptedVariableThis = isInlineThisOnly
        }
        if (myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineScriptedVariableKeep = isKeepTheDeclaration()
        }
    }
}

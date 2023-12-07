package icu.windea.pls.core.refactoring.inline

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.refactoring.inline.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*

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
        val name = element.name.orAnonymous()
        return PlsBundle.message("inline.scriptedVariable.label", name)
    }
    
    override fun getBorderTitle(): String {
        return PlsBundle.message("inline.scriptedVariable.border.title")
    }
    
    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.scriptedVariable.inline.this")
    }
    
    override fun getInlineAllText(): String {
        return if(element.isWritable) PlsBundle.message("inline.scriptedVariable.inline.all.remove")
        else PlsBundle.message("inline.scriptedVariable.inline.all")
    }
    
    override fun getKeepTheDeclarationText(): String {
        return if(element.isWritable) PlsBundle.message("inline.scriptedVariable.inline.all.keep")
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
        if(myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineScriptedVariableThis = isInlineThisOnly
        }
        if(myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineScriptedVariableKeep = isKeepTheDeclaration()
        }
    }
}

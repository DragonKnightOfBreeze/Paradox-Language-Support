package icu.windea.pls.lang.refactoring.inline

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.refactoring.inline.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.refactoring.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedTriggerInlineDialog(
    project: Project,
    private val element: ParadoxScriptProperty,
    private val reference: PsiReference?,
    private val editor: Editor?
) : InlineOptionsDialog(project, true, element) {
    private val optimizedScope = ParadoxSearchScope.fromElement(element)
        ?.withFileTypes(ParadoxScriptFileType)
        ?.intersectWith(GlobalSearchScope.projectScope(project))
        ?: GlobalSearchScope.projectScope(project)

    init {
        title = PlsBundle.message("title.inline.scriptedTrigger")
        myInvokedOnReference = reference != null
        init()
        helpAction.isEnabled = false
    }

    override fun getNameLabelText(): String {
        val name = element.definitionInfo?.name.orAnonymous()
        return PlsBundle.message("inline.scriptedTrigger.label", name)
    }

    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.scriptedTrigger.inline.this")
    }

    override fun getInlineAllText(): String {
        return if (element.isWritable) PlsBundle.message("inline.scriptedTrigger.inline.all.remove")
        else PlsBundle.message("inline.scriptedTrigger.inline.all")
    }

    override fun getKeepTheDeclarationText(): String {
        return if (element.isWritable) PlsBundle.message("inline.scriptedTrigger.inline.all.keep")
        else super.getKeepTheDeclarationText()
    }

    override fun allowInlineAll(): Boolean {
        return true
    }

    override fun isInlineThis(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedTriggerThis
    }

    override fun isKeepTheDeclarationByDefault(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedTriggerKeep
    }

    override fun doAction() {
        val processor = ParadoxScriptedTriggerInlineProcessor(project, optimizedScope, element, reference, editor, isInlineThisOnly, isKeepTheDeclaration())
        invokeRefactoring(processor)
        val settings = ParadoxRefactoringSettings.getInstance()
        if (myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineScriptedTriggerThis = isInlineThisOnly
        }
        if (myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineScriptedTriggerKeep = isKeepTheDeclaration()
        }
    }
}

package icu.windea.pls.core.refactoring.inline

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.refactoring.inline.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class InlineScriptedEffectDialog(
    project: Project,
    private val element: ParadoxScriptProperty,
    private val reference: PsiReference?,
    private val editor: Editor?
) : InlineOptionsDialog(project, true, element) {
    private val optimizedScope = ParadoxSearchScope.fromElement(element)
        ?.withFileTypes(ParadoxScriptFileType)
        ?.intersectWith(GlobalSearchScope.projectScope(project))
        ?: GlobalSearchScope.projectScope(project)
    
    private val occurrencesNumber = getNumberOfOccurrences(element)
    
    init {
        title = PlsBundle.message("title.inline.scriptedEffect")
        myInvokedOnReference = reference != null
        init()
        helpAction.isEnabled = false
    }
    
    override fun getNameLabelText(): String {
        val name = element.definitionInfo?.name.orEmpty()
        return when {
            occurrencesNumber > -1 -> PlsBundle.message("inline.scriptedEffect.occurrences", name, occurrencesNumber)
            else -> PlsBundle.message("inline.scriptedEffect.label", name)
        }
    }
    
    override fun getBorderTitle(): String {
        return PlsBundle.message("inline.scriptedEffect.border.title")
    }
    
    override fun getInlineThisText(): String {
        return PlsBundle.message("inline.scriptedEffect.inline.this")
    }
    
    override fun getInlineAllText(): String {
        return if(element.isWritable) PlsBundle.message("inline.scriptedEffect.inline.all.remove")
        else PlsBundle.message("inline.scriptedEffect.inline.all")
    }
    
    override fun getKeepTheDeclarationText(): String {
        return if(element.isWritable) PlsBundle.message("inline.scriptedEffect.inline.all.keep")
        else super.getKeepTheDeclarationText()
    }
    
    override fun allowInlineAll(): Boolean {
        return true
    }
    
    override fun isInlineThis(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedEffectThis
    }
    
    override fun isKeepTheDeclarationByDefault(): Boolean {
        return ParadoxRefactoringSettings.getInstance().inlineScriptedEffectKeep
    }
    
    override fun ignoreOccurrence(reference: PsiReference?): Boolean {
        return true
    }
    
    override fun getNumberOfOccurrences(nameIdentifierOwner: PsiNameIdentifierOwner): Int {
        val element = nameIdentifierOwner
        return getNumberOfOccurrences(element, this::ignoreOccurrence) {
            ReferencesSearch.search(element, optimizedScope)
        }
    }
    
    override fun doAction() {
        val processor = InlineScriptedEffectProcessor(project, optimizedScope, element, reference, editor, isInlineThisOnly, isKeepTheDeclaration())
        invokeRefactoring(processor)
        val settings = ParadoxRefactoringSettings.getInstance()
        if(myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.inlineScriptedEffectThis = isInlineThisOnly
        }
        if(myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.inlineScriptedEffectKeep = isKeepTheDeclaration()
        }
    }
}
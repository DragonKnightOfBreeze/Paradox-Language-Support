package icu.windea.pls.core.refactoring.inline

import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

//com.intellij.refactoring.inline.InlineViewDescriptor

class InlineViewDescriptor(
    private val element: PsiElement
) : UsageViewDescriptor {
    override fun getElements(): Array<PsiElement> {
        return arrayOf(element)
    }
    
    override fun getProcessedElementsHeader(): String {
        return when {
            element is ParadoxScriptScriptedVariable -> PlsBundle.message("inline.scriptedVariable.elements.header")
            element is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo 
                when {
                    definitionInfo == null -> null
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inline.scriptedTrigger.elements.header")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inline.scriptedEffect.elements.header")
                    else -> null
                }
            }
            else -> null
        } ?: PlsBundle.message("inline.element.unknown.header")
    }
    
    override fun getCodeReferencesText(usagesCount: Int, filesCount: Int): String {
        return PlsBundle.message("invocations.to.be.inlined", UsageViewBundle.getReferencesString(usagesCount, filesCount))
    }
    
    override fun getCommentReferencesText(usagesCount: Int, filesCount: Int): String {
        return PlsBundle.message("comments.elements.header", UsageViewBundle.getOccurencesString(usagesCount, filesCount))
    }
}
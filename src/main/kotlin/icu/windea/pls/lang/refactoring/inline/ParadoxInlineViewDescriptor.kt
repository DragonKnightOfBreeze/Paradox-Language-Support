package icu.windea.pls.lang.refactoring.inline

import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewBundle
import com.intellij.usageView.UsageViewDescriptor
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// com.intellij.refactoring.inline.InlineViewDescriptor

class ParadoxInlineViewDescriptor(
    private val element: PsiElement
) : UsageViewDescriptor {
    override fun getElements(): Array<PsiElement> {
        return arrayOf(element)
    }

    override fun getProcessedElementsHeader(): String {
        return when {
            element is ParadoxScriptScriptedVariable -> ChronicleBundle.message("inline.scriptedVariable.elements.header")
            element is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo
                when {
                    definitionInfo == null -> null
                    definitionInfo.type == "scripted_trigger" -> ChronicleBundle.message("inline.scriptedTrigger.elements.header")
                    definitionInfo.type == "scripted_effect" -> ChronicleBundle.message("inline.scriptedEffect.elements.header")
                    else -> null
                }
            }
            // key or value of "inline_script = some/inline_script "
            element is ParadoxScriptStringExpressionElement -> ChronicleBundle.message("inline.inlineScript.elements.header")
            element is ParadoxLocalisationProperty -> ChronicleBundle.message("inline.localisation.elements.header")
            else -> null
        } ?: ChronicleBundle.message("inline.element.unknown.header")
    }

    override fun getCodeReferencesText(usagesCount: Int, filesCount: Int): String {
        return ChronicleBundle.message("invocations.to.be.inlined", UsageViewBundle.getReferencesString(usagesCount, filesCount))
    }

    override fun getCommentReferencesText(usagesCount: Int, filesCount: Int): String {
        return ChronicleBundle.message("comments.elements.header", UsageViewBundle.getOccurencesString(usagesCount, filesCount))
    }
}

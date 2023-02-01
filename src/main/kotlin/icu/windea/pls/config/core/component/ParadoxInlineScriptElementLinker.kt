package icu.windea.pls.config.core.component

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.ParadoxInlineScriptHandler.getInlineScriptExpression
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxInlineScriptElementLinker : ParadoxElementLinker {
    override fun linkElement(element: PsiElement): PsiElement? {
        if(element !is ParadoxScriptFile) return null
        if(!getSettings().inference.inlineScriptLocation) return null
        if(getInlineScriptExpression(element) == null) return null
        val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(element) ?: return null
        if(usageInfo.hasConflict) return null
        return usageInfo.pointer.element
    }
}
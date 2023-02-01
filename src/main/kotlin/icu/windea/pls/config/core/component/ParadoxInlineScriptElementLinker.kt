package icu.windea.pls.config.core.component

import com.intellij.psi.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxInlineScriptElementLinker : ParadoxElementLinker {
    override fun linkElement(element: PsiElement): PsiElement? {
        if(element !is ParadoxScriptFile) return null
        if(!ParadoxInlineScriptHandler.isInlineScriptFile(element)) return null
        return ParadoxInlineScriptHandler.getInlineScriptProperty(element)
    }
}
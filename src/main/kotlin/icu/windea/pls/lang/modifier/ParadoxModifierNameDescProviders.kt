package icu.windea.pls.lang.modifier

import com.intellij.psi.*
import icu.windea.pls.model.elementInfo.*

class ParadoxBaseModifierNameDescProvider : ParadoxModifierNameDescProvider {
    override fun addModifierNameKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        //mod_$, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierInfo.name}"
    }
    
    override fun addModifierDescKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierInfo.name}_desc"
    }
}
package icu.windea.pls.lang.modifier.impl

import com.intellij.psi.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.model.stubs.*

class ParadoxBaseModifierNameDescProvider : ParadoxModifierNameDescProvider {
    override fun addModifierNameKey(modifierData: ParadoxModifierStub, element: PsiElement, registry: MutableSet<String>) {
        //mod_$, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierData.name}"
    }
    
    override fun addModifierDescKey(modifierData: ParadoxModifierStub, element: PsiElement, registry: MutableSet<String>) {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierData.name}_desc"
    }
}
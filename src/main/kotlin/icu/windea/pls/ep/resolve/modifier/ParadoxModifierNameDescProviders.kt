package icu.windea.pls.ep.resolve.modifier

import com.intellij.psi.PsiElement
import icu.windea.pls.model.ParadoxModifierInfo

class ParadoxBaseModifierNameDescProvider : ParadoxModifierNameDescProvider {
    override fun addModifierNameKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        // mod_$, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierInfo.name}"
    }

    override fun addModifierDescKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        // mod_$_desc, ALL_UPPER_CASE is ok.
        registry += "mod_${modifierInfo.name}_desc"
    }
}

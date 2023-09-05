package icu.windea.pls.lang.modifier.impl

import com.intellij.psi.*
import icu.windea.pls.lang.modifier.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$
        registry += "gfx/interface/icons/modifiers/mod_${name}"
    }
}


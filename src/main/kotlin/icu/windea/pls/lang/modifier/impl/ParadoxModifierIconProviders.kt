package icu.windea.pls.lang.modifier.impl

import com.intellij.psi.*
import icu.windea.pls.lang.modifier.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$
        registry += "gfx/interface/icons/modifiers/mod_${name}"
    }
}

class ParadoxDelegateDefinitionBasedModifierIconProvider: ParadoxModifierIconProvider {
    //如果修正M由定义D生成，而定义D的作为图标的图片由委托给了定义D1
    //那么修正M的作为图标的图片也可以委托给定义D1的对应修正
    
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //TODO 1.1.8+
    }
}
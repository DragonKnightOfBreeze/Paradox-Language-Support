package icu.windea.pls.ep.modifier

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.elementInfo.*

/**
 * 用于为修正提供图标的图片。
 */
@WithGameTypeEP
interface ParadoxModifierIconProvider {
    /** 注意：这里加入的文件路径是不包含扩展名的。实际上，允许任何合法的扩展名（.dds, .png, .tga）。 */
    fun addModifierIconPath(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierIconProvider>("icu.windea.pls.modifierIconProvider")
        
        fun getModifierIconPaths(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
            val gameType = modifierInfo.gameType
            return buildSet {
                EP_NAME.extensionList.forEachFast f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f
                    ep.addModifierIconPath(modifierInfo, element, this)
                }
            }
        }
    }
}
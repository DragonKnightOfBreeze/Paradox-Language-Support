package icu.windea.pls.ep.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxModifierInfo

/**
 * 用于为修正提供图标的图片。
 */
@WithGameTypeEP
interface ParadoxModifierIconProvider {
    /** 注意：这里加入的文件路径是不包含扩展名的。实际上，允许任何合法的扩展名（.dds, .png, .tga）。 */
    fun addModifierIconPath(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModifierIconProvider>("icu.windea.pls.modifierIconProvider")

        fun getModifierIconPaths(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
            val gameType = modifierInfo.gameType
            return buildSet {
                EP_NAME.extensionList.forEach f@{ ep ->
                    if (!PlsAnnotationManager.check(ep, gameType)) return@f
                    ep.addModifierIconPath(modifierInfo, element, this)
                }
            }
        }
    }
}

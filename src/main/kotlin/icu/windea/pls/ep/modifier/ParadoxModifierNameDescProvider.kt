package icu.windea.pls.ep.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.elementInfo.ParadoxModifierInfo

/**
 * 用于为修正提供名字和描述的本地化。
 *
 * 注意：修正的名字或描述对应的本地化的名字是忽略大小写的。
 */
@WithGameTypeEP
interface ParadoxModifierNameDescProvider {
    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierNameKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierDescKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModifierNameDescProvider>("icu.windea.pls.modifierNameDescProvider")

        fun getModifierNameKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
            val gameType = modifierInfo.gameType
            return buildSet {
                EP_NAME.extensionList.forEach f@{ ep ->
                    if (!gameType.supportsByAnnotation(ep)) return@f
                    ep.addModifierNameKey(modifierInfo, element, this)
                }
            }
        }

        fun getModifierDescKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
            val gameType = modifierInfo.gameType
            return buildSet {
                EP_NAME.extensionList.forEach f@{ ep ->
                    if (!gameType.supportsByAnnotation(ep)) return@f
                    ep.addModifierDescKey(modifierInfo, element, this)
                }
            }
        }
    }
}

package icu.windea.pls.lang.modifier

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.stub.*

/**
 * 用于为修正提供名字和描述的本地化。
 *
 * 注意：修正的名字或描述对应的本地化的名字是忽略大小写的。
 */
@WithGameTypeEP
interface ParadoxModifierNameDescProvider {
    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierNameKey(modifierData: ParadoxModifierStub, element: PsiElement, registry: MutableSet<String>)
    
    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierDescKey(modifierData: ParadoxModifierStub, element: PsiElement, registry: MutableSet<String>)
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierNameDescProvider>("icu.windea.pls.modifierNameDescProvider")
        
        fun getModifierNameKeys(element: PsiElement, modifierData: ParadoxModifierStub): Set<String> {
            val gameType = modifierData.gameType
            return buildSet {
                EP_NAME.extensionList.forEachFast f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f
                    ep.addModifierNameKey(modifierData, element, this)
                }
            }
        }
        
        fun getModifierDescKeys(element: PsiElement, modifierData: ParadoxModifierStub): Set<String> {
            val gameType = modifierData.gameType
            return buildSet {
                EP_NAME.extensionList.forEachFast f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f
                    ep.addModifierDescKey(modifierData, element, this)
                }
            }
        }
    }
}
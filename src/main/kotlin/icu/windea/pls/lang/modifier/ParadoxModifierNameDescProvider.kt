package icu.windea.pls.lang.modifier

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*

/**
 * 用于为修正提供名字和描述的本地化。
 *
 * 注意：修正的名字或描述对应的本地化的名字是忽略大小写的。
 */
@WithGameTypeEP
interface ParadoxModifierNameDescProvider {
    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierNameKey(modifierData: ParadoxModifierData, element: PsiElement, registry: MutableSet<String>)
    
    /** 注意：这里加入的本地化的名字是忽略大小写的。 */
    fun addModifierDescKey(modifierData: ParadoxModifierData, element: PsiElement, registry: MutableSet<String>)
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierNameDescProvider>("icu.windea.pls.modifierNameDescProvider")
    }
}
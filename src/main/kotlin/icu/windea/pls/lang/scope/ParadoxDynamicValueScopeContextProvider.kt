package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.*

/**
 * 用于为动态值提供作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDynamicValueScopeContextProvider {
    fun supports(element: ParadoxValueSetValueElement): Boolean
    
    fun getScopeContext(element: ParadoxValueSetValueElement): ParadoxScopeContext?
    
    //注意：同名的动态值在不同的上下文中完全可能拥有不同的作用域上下文
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDynamicValueScopeContextProvider>("icu.windea.pls.dynamicValueScopeContextProvider")
        
        fun getScopeContext(element: ParadoxValueSetValueElement): ParadoxScopeContext? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                if(!ep.supports(element)) return@f null
                ep.getScopeContext(element)
            }
        }
    }
}

package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*

/**
 * 用于为动态值提供作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDynamicValueScopeContextProvider {
    fun supports(element: ParadoxDynamicValueElement): Boolean
    
    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext?
    
    //注意：同名的动态值在不同的上下文中完全可能拥有不同的作用域上下文
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDynamicValueScopeContextProvider>("icu.windea.pls.dynamicValueScopeContextProvider")
        
        fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                if(!ep.supports(element)) return@f null
                ep.getScopeContext(element)
            }
        }
    }
}

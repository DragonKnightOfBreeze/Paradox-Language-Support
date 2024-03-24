package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * 用于为动态值提供（基于使用）推断的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDynamicValueInferredScopeContextProvider {
    fun supports(element: ParadoxDynamicValueElement): Boolean
    
    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContextInferenceInfo?
    
    //注意：同名的动态值在不同的上下文中完全可能拥有不同的作用域上下文
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDynamicValueInferredScopeContextProvider>("icu.windea.pls.dynamicValueInferredScopeContextProvider")
        
        fun getScopeContext(dynamicValue: ParadoxDynamicValueElement): ParadoxScopeContext? {
            val gameType = dynamicValue.gameType
            var map: Map<String, String>? = null
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                if(!ep.supports(dynamicValue)) return@f
                val info = ep.getScopeContext(dynamicValue) ?: return@f
                if(info.hasConflict) return null //只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
                if(map == null) {
                    map = info.scopeContextMap
                } else {
                    map = ParadoxScopeHandler.mergeScopeContextMap(map!!, info.scopeContextMap)
                }
            }
            val resultMap = map ?: return null
            val result = ParadoxScopeContext.resolve(resultMap)
            return result
        }
    }
}

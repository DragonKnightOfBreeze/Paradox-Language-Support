package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为特定类型的valueSetValue提供推断的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxValueSetValueInferredScopeContextProvider{
    fun supports(valueSetValue: ParadoxValueSetValueElement): Boolean
    
    fun getScopeContext(valueSetValue: ParadoxValueSetValueElement): ParadoxScopeContextInferenceInfo?
    
    //特定类型的valueSetValue，例如event_target、variable，即使同名，也完全有可能拥有不同的作用域上下文
    //因此插件目前不提供相关的代码检查
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxValueSetValueInferredScopeContextProvider>("icu.windea.pls.valueSetValueInferredScopeContextProvider")
        
        fun getScopeContext(valueSetValue: ParadoxValueSetValueElement): ParadoxScopeContext? {
            val gameType = valueSetValue.gameType
            var map: Map<String, String?>? = null
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                if(!ep.supports(valueSetValue)) return@f
                val info = ep.getScopeContext(valueSetValue) ?: return@f
                if(info.hasConflict) return null //只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
                if(map == null) {
                    map = info.scopeContextMap
                } else {
                    map = ParadoxScopeHandler.mergeScopeContextMap(map!!, info.scopeContextMap)
                }
            }
            val resultMap = map ?: return null
            val result = ParadoxScopeContext.resolve(resultMap)?.copyAsInferred()
            return result
        }
    }
}
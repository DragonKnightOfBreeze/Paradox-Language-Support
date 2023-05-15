package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于为定义提供推断的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDefinitionInferredScopeContextProvider {
    /**
     * 得到推断的作用域。
     * @return 推断得到的作用域上下文信息。
     */
    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo?
    
    /**
     * 当推断结果不存在冲突时要显示的消息。
     */
    fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String?
    
    /**
     * 当推断结果存在冲突时要显示的错误消息。
     */
    fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionInferredScopeContextProvider>("icu.windea.pls.definitionInferredScopeContextProvider")
        
        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensions.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getScopeContext(definition, definitionInfo)
            }
        }
        
        fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensions.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                val info = ep.getScopeContext(definition, definitionInfo)
                if(info == null || !info.hasConflict) return@f null
                ep.getErrorMessage(definition, definitionInfo, info)
            }
        }
        
        fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensions.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                val info = ep.getScopeContext(definition, definitionInfo)
                if(info == null || !info.hasConflict) return@f null
                ep.getMessage(definition, definitionInfo, info)
            }
        }
    }
}


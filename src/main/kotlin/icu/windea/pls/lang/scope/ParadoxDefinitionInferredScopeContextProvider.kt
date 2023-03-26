package icu.windea.pls.lang.scope

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于提供推断的作用域上下文。
 */
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
            for(extension in EP_NAME.extensions) {
                val info = extension.getScopeContext(definition, definitionInfo)
                if(info != null) return info
            }
            return null
        }
        
        fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            for(extension in EP_NAME.extensions) {
                val info = extension.getScopeContext(definition, definitionInfo)
                if(info != null && info.hasConflict) return extension.getErrorMessage(definition, definitionInfo, info)
            }
            return null
        }
        
        fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            for(extension in EP_NAME.extensions) {
                val info = extension.getScopeContext(definition, definitionInfo)
                if(info != null && info.hasConflict) return extension.getMessage(definition, definitionInfo, info)
            }
            return null
        }
    }
}

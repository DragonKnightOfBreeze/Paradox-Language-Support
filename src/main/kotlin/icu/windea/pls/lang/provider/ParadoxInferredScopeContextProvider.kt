package icu.windea.pls.lang.provider

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.lang.model.*

/**
 * 用于提供推断的作用域上下文。
 */
interface ParadoxInferredScopeContextProvider {
    enum class Type {
        Definition,
        LocalisationCommand, //TODO
        ValueSetValue; //TODO
    }
    
    val type: Type
    
    /**
     * 推断作用域。
     * @param contextElement 上下文PSI元素。
     * @return 推断得到的作用域上下文信息。
     */
    fun getScopeContext(contextElement: PsiElement): ParadoxScopeContextInferenceInfo?
    
    /**
     * 当推断结果不存在冲突时要显示的消息。
     */
    fun getMessage(contextElement: PsiElement, info: ParadoxScopeContextInferenceInfo): String?
    
    /**
     * 当推断结果存在冲突时要显示的错误消息。
     */
    fun getErrorMessage(contextElement: PsiElement, info: ParadoxScopeContextInferenceInfo): String?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxInferredScopeContextProvider>("icu.windea.pls.inferredScopeContextProvider")
        
        @JvmStatic
        fun inferForDefinition(contextElement: PsiElement): ParadoxScopeContextInferenceInfo? {
            for(extension in EP_NAME.extensions) {
                if(extension.type == Type.Definition) {
                    val info = extension.getScopeContext(contextElement)
                    if(info != null) return info
                }
            }
            return null
        }
        
        @JvmStatic
        fun getErrorMessageForDefinition(contextElement: PsiElement): String? {
            for(extension in EP_NAME.extensions) {
                if(extension.type == Type.Definition) {
                    val info = extension.getScopeContext(contextElement)
                    if(info != null && info.hasConflict) return extension.getErrorMessage(contextElement, info)
                }
            }
            return null
        }
    
        @JvmStatic
        fun getMessageForDefinition(contextElement: PsiElement): String? {
            for(extension in EP_NAME.extensions) {
                if(extension.type == Type.Definition) {
                    val info = extension.getScopeContext(contextElement)
                    if(info != null && info.hasConflict) return extension.getMessage(contextElement, info)
                }
            }
            return null
        }
    }
}


package icu.windea.pls.lang.provider

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.lang.model.*

/**
 * 用于支持推断的作用域上下文。基于使用处的作用域上下文。
 */
interface ParadoxInferredScopeContextProvider {
    enum class Type {
        Definition,
        LocalisationCommand, //TODO
        ValueSetValue; //TODO
    }
    
    val type: Type
    
    /**
     * @param contextElement 上下文PSI元素。
     * @param rawScopeContext 已知的作用域上下文。一般情况下为any作用域。
     * @return 推断得到的所有可能的作用域上下文。如果为空则表示无法推断。
     */
    fun infer(contextElement: PsiElement, rawScopeContext: ParadoxScopeContext): Set<ParadoxScopeContext>
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxInferredScopeContextProvider>("icu.windea.pls.inferredScopeContextProvider")
        
        @JvmStatic
        fun inferForDefinition(contextElement: PsiElement, rawScopeContext: ParadoxScopeContext): Set<ParadoxScopeContext> {
            val result = mutableSetOf<ParadoxScopeContext>()
            for(extension in EP_NAME.extensions) {
                if(extension.type == Type.Definition) {
                    val r = extension.infer(contextElement, rawScopeContext)
                    result.addAll(r)
                }
            }
            return result
        }
    }
}


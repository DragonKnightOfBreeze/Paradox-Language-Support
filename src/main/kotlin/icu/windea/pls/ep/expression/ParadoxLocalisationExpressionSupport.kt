package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供对本地化表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等功能。
 */
@WithGameTypeEP
interface ParadoxLocalisationExpressionSupport {
    fun supports(element: ParadoxLocalisationExpressionElement): Boolean
    
    fun annotate(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder) {
        
    }
    
    fun resolve(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): PsiElement? {
        return null
    }
    
    fun multiResolve(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Collection<PsiElement> {
        return resolve(element, rangeInElement, expression).toSingletonSetOrEmpty()
    }
    
    fun getReferences(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Array<out PsiReference>? {
        return null
    }
    
    fun complete(context: ProcessingContext, result: CompletionResultSet) {
        
    }
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxLocalisationExpressionSupport>("icu.windea.pls.localisationExpressionSupport")
        
        //目前来看，这里暂不需要尝试避免SOE
        
        fun annotate(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder) {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!ep.supports(element)) return@f
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.annotate(element, rangeInElement, expression, holder)
            }
        }
        
        fun resolve(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): PsiElement? {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!ep.supports(element)) return@f
                if(!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.resolve(element, rangeInElement, expression)
                if(r != null) return r
            }
            return null
        }
        
        fun multiResolve(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Collection<PsiElement> {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!ep.supports(element)) return@f
                if(!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.multiResolve(element, rangeInElement, expression).orNull()
                if(r != null) return r
            }
            return emptySet()
        }
        
        fun getReferences(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Array<out PsiReference>? {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!ep.supports(element)) return@f
                if(!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.getReferences(element, rangeInElement, expression).orNull()
                if(r != null) return r
            }
            return null
        }
        
        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val element = context.contextElement?.castOrNull<ParadoxLocalisationExpressionElement>() ?: return
            val gameType by lazy { selectGameType(element) }
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!ep.supports(element)) return@f
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.complete(context, result)
            }
        }
    }
}

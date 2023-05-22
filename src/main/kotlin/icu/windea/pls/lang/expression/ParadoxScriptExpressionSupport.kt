package icu.windea.pls.lang.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等功能。
 */
abstract class ParadoxScriptExpressionSupport {
    abstract fun supports(config: CwtConfig<*>): Boolean
    
    open fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        
    }
    
    open fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        return null
    }
    
    open fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        return resolve(element, rangeInElement, expression, config, isKey, false).toSingletonSetOrEmpty()
    }
    
    open fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
        return null
    }
    
    open fun complete(context: ProcessingContext, result: CompletionResultSet) {
        
    }
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
        
        //这里需要尝试避免SOE
        
        fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
            withRecursionGuard("icu.windea.pls.lang.expression.ParadoxScriptExpressionSupport.annotate") { 
                EP_NAME.extensionList.forEachFast f@{ ep ->
                    if(!ep.supports(config)) return@f
                    withCheckRecursion("${ep.javaClass.name}@annotate@${expression}") {
                        ep.annotate(element, rangeInElement, expression, holder, config)
                    }
                }
            }
        }
        
        fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
            return withRecursionGuard("icu.windea.pls.lang.expression.ParadoxScriptExpressionSupport.resolve") {
                EP_NAME.extensionList.firstNotNullOfOrNull p@{ ep ->
                    if(!ep.supports(config)) return@p null
                    withCheckRecursion("${ep.javaClass.name}@resolve@${expression}") {
                        ep.resolve(element, rangeInElement, expression, config, isKey, exact)
                    }
                }
            } 
        }
        
        fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
            return withRecursionGuard("icu.windea.pls.lang.expression.ParadoxScriptExpressionSupport.multiResolve") {
                EP_NAME.extensionList.firstNotNullOfOrNull p@{ ep ->
                    if(!ep.supports(config)) return@p null
                    withCheckRecursion("${ep.javaClass.name}@multiResolve@${expression}") {
                        ep.multiResolve(element, rangeInElement, expression, config, isKey).takeIfNotEmpty()
                    }
                }
            }.orEmpty()
        }
        
        fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
            return withRecursionGuard("icu.windea.pls.lang.expression.ParadoxScriptExpressionSupport.getReferences") {
                EP_NAME.extensionList.firstNotNullOfOrNull p@{ ep ->
                    if(!ep.supports(config)) return@p null
                    withCheckRecursion("${ep.javaClass.name}@multiResolve@${expression}") {
                        ep.getReferences(element, rangeInElement, expression, config, isKey).takeIfNotEmpty()
                    }
                }
            }
        }
        
        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val config = context.config ?: return
            withRecursionGuard("icu.windea.pls.lang.expression.ParadoxScriptExpressionSupport.complete") {
                EP_NAME.extensionList.forEachFast f@{ ep ->
                    if(!ep.supports(config)) return@f
                    withCheckRecursion("${ep.javaClass.name}@complete${context.keyword}") {
                        ep.complete(context, result)
                    }
                }
            }
        }
    }
}


package icu.windea.pls.lang.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
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
    
    open fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String,  config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        return null
    }
    
    open fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        return resolve(element, rangeInElement, expression, config, isKey, false).toSingletonSetOrEmpty()
    }
    
    open fun complete(config: CwtConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
        
    }
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
        
        fun getAll(config: CwtConfig<*>): List<ParadoxScriptExpressionSupport> {
            return EP_NAME.extensionList.filter { it.supports(config) }
        }
        
        fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
            EP_NAME.extensionList.forEach {
                if(it.supports(config)) {
                    it.annotate(element, rangeInElement, expression, holder, config)
                }
            }
        }
        
        fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String,  config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
            EP_NAME.extensionList.forEach {
                if(it.supports(config)) {
                    val result = it.resolve(element, rangeInElement, expression, config, isKey, exact)
                    if(result != null) return result
                }
            }
            return null
        }
        
        fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String,  config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
            EP_NAME.extensionList.forEach {
                if(it.supports(config)) {
                    val result = it.multiResolve(element, rangeInElement, expression, config, isKey)
                    if(result.isNotEmpty()) return result
                }
            }
            return emptySet()
        }
        
        fun complete(config: CwtConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
            EP_NAME.extensionList.forEach { 
                if(it.supports(config)) {
                    it.complete(config, context, result)
                }
            }
        }
    }
}


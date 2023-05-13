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
    
    open fun complete(context: ProcessingContext, result: CompletionResultSet) {
        
    }
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
        
        fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
            try {
                EP_NAME.extensionList.forEach p@{ ep ->
                    if(checkInstanceRecursion(ep)) return@p //避免SOE
                    if(!ep.supports(config)) return@p
                    ep.annotate(element, rangeInElement, expression, holder, config)
                }
            } finally {
                finishCheckInstanceRecursion()
            }
        }
        
        fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
            try {
                return EP_NAME.extensionList.firstNotNullOfOrNull p@{ ep ->
                    if(checkInstanceRecursion(ep)) return@p null //避免SOE
                    if(!ep.supports(config)) return@p null
                    ep.resolve(element, rangeInElement, expression, config, isKey, exact)
                }
            } finally {
                finishCheckInstanceRecursion()
            }
        }
        
        fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
            try {
                return EP_NAME.extensionList.firstNotNullOfOrNull p@{ ep ->
                    if(checkInstanceRecursion(ep)) return@p null //避免SOE
                    if(!ep.supports(config)) return@p null
                    ep.multiResolve(element, rangeInElement, expression, config, isKey).takeIfNotEmpty()
                }.orEmpty()
            } finally {
                finishCheckInstanceRecursion()
            }
        }
        
        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            try {
                val config = context.config ?: return
                EP_NAME.extensionList.forEach p@{ ep ->
                    if(checkInstanceRecursion(ep)) return@p //避免SOE
                    if(!ep.supports(config)) return@p
                    ep.complete(context, result)
                }
            } finally {
                finishCheckInstanceRecursion()
            }
        }
    }
}


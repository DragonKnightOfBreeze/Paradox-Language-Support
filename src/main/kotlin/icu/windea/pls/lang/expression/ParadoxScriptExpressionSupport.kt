package icu.windea.pls.lang.expression

import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本表达式的支持。
 *
 * 用于实现规则匹配、代码高亮、引用解析、代码补全等功能。
 */
abstract class ParadoxScriptExpressionSupport {
    abstract fun supports(config: CwtConfig<*>): Boolean
    
    open fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        
    }
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
        
        fun getAll(config: CwtConfig<*>): List<ParadoxScriptExpressionSupport> {
            return EP_NAME.extensionList.filter { it.supports(config) }
        }
        
        fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
            EP_NAME.extensionList.forEach {
                if(it.supports(config)) {
                    it.annotate(element, rangeInElement, expression, config, holder)
                }
            }
        }
    }
}


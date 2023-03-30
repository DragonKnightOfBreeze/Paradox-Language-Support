package icu.windea.pls.script.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

abstract class ParadoxScriptTemplateContextType(presentableName: String) : TemplateContextType(presentableName) {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        if(!file.language.isKindOf(ParadoxScriptLanguage)) return false
        return doIsInContext(templateActionContext)
    }
    
    abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean
    
    class Base : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            return true
        }
    }
    
    class Member : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.members")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            val startElement = start.parents(withSelf = false)
                .find {
                    if(it is ParadoxScriptInlineMath && it.textRange.startOffset != startOffset) return false
                    if(it is ParadoxScriptParameterConditionExpression && it.textRange.startOffset != startOffset) return false
                    it is ParadoxScriptMemberElement
                }
            return startElement != null
        }
    }
    
    class KeyExpression : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.keyExpressions")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            val startElement = start.parents(withSelf = false)
                .find {
                    if(it is ParadoxScriptInlineMath && it.textRange.startOffset != startOffset) return false
                    if(it is ParadoxScriptParameterConditionExpression && it.textRange.startOffset != startOffset) return false
                    it is ParadoxScriptPropertyKey
                }
            return startElement != null
        }
    }
    
    class ValueExpression : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.valueExpressions")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            val startElement = start.parents(withSelf = false)
                .find {
                    if(it is ParadoxScriptPropertyKey) return false
                    if(it is ParadoxScriptInlineMath && it.textRange.startOffset != startOffset) return false
                    if(it is ParadoxScriptParameterConditionExpression && it.textRange.startOffset != startOffset) return false
                    it is ParadoxScriptValue
                }
            return startElement != null
        }
    }
    
    class InlineMathExpression : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.inlineMathExpressions")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file.originalFile
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            if(start.elementType == ParadoxScriptElementTypes.INLINE_MATH_START) return false
            val startElement = start.parentOfType<ParadoxScriptInlineMath>()
            return startElement != null
        }
    }
}
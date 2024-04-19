package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的相关代码补全。基于CWT规则文件。
 */
class ParadoxInDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parentOfType<ParadoxScriptStringExpressionElement>() ?: return
        
        ProgressManager.checkCanceled()
        
        val file = parameters.originalFile
        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        
        context.initialize(parameters)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        
        //兼容参数值（包括整行或多行参数值）和内联脚本文件中内容
        
        val parameterValueQuoted = CwtConfigHandler.getConfigContext(file)?.parameterValueQuoted
        val mayBeKey = parameterValueQuoted != false && (element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isBlockValue()))
        val mayBeValue = element is ParadoxScriptString && element.isBlockValue()
        val mayBePropertyValue = parameterValueQuoted != false && (element is ParadoxScriptString && element.isPropertyValue())
        
        val resultToUse = result.withPrefixMatcher(keyword)
        if(mayBeKey) {
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMemberElement>(withSelf = true)
            if(memberElement != null) {
                ParadoxCompletionManager.addKeyCompletions(memberElement, context, resultToUse)
            }
        }
        if(mayBeValue) {
            //向上得到block或者file
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMemberElement>(withSelf = true)
            if(memberElement != null) {
                ParadoxCompletionManager.addValueCompletions(memberElement, context, resultToUse)
            }
        }
        if(mayBePropertyValue) {
            //向上得到property
            val propertyElement = element.findParentProperty() as? ParadoxScriptProperty
            if(propertyElement != null) {
                ParadoxCompletionManager.addPropertyValueCompletions(element, propertyElement, context, resultToUse)
            }
        }
    }
}

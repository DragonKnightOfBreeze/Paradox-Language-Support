package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.ep.configContext.parameterValueQuoted
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.expressionOffset
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.offsetInParent
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.rightQuoted
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentProperty
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.isPropertyValue

/**
 * 提供脚本表达式相关的代码补全。基于规则文件。
 */
class ParadoxScriptExpressionCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parentOfType<ParadoxScriptStringExpressionElement>() ?: return
        if (!element.isExpression()) return

        val file = parameters.originalFile
        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        context.expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)

        //兼容参数值（包括整行或多行参数值）和内联脚本文件中内容

        val parameterValueQuoted = ParadoxExpressionManager.getConfigContext(file)?.parameterValueQuoted
        val mayBeKey = parameterValueQuoted != false && (element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isBlockMember()))
        val mayBeValue = element is ParadoxScriptString && element.isBlockMember()
        val mayBePropertyValue = parameterValueQuoted != false && (element is ParadoxScriptString && element.isPropertyValue())

        val resultToUse = result.withPrefixMatcher(keyword)
        if (mayBeKey) {
            //向上得到block或者file
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMemberElement>(withSelf = true)
            if (memberElement != null) {
                ParadoxCompletionManager.addKeyCompletions(memberElement, context, resultToUse)
            }
        }
        if (mayBeValue) {
            //向上得到block或者file
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMemberElement>(withSelf = true)
            if (memberElement != null) {
                ParadoxCompletionManager.addValueCompletions(memberElement, context, resultToUse)
            }
        }
        if (mayBePropertyValue) {
            //向上得到property
            val propertyElement = element.findParentProperty() as? ParadoxScriptProperty
            if (propertyElement != null) {
                ParadoxCompletionManager.addPropertyValueCompletions(element, propertyElement, context, resultToUse)
            }
        }
    }
}

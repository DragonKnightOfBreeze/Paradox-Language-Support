package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 提供内联脚本调用的代码补全。
 */
class ParadoxInlineScriptInvocationCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!getSettings().completion.completeInlineScriptInvocations) return

        val file = parameters.originalFile
        if (file !is ParadoxScriptFile || selectRootFile(file) == null) return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return
        if (element is ParadoxScriptString) {
            if (!element.isBlockMember()) return
        } else if (element is ParadoxScriptPropertyKey) {
            //if element is property key, related property value should be a string or clause (after resolving)
            val propertyValue = element.propertyValue
            if (propertyValue != null && propertyValue.resolved().let { it != null && it !is ParadoxScriptString && it !is ParadoxScriptBlock }) return
        }

        //inline script invocation cannot be nested directly
        val configContext = ParadoxExpressionManager.getConfigContext(element)
        if (configContext != null && configContext.provider is CwtInlineScriptUsageConfigContextProvider) return

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

        ParadoxCompletionManager.completeInlineScriptInvocation(context, result)
    }
}

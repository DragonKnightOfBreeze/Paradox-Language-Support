package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configContext.provider
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.ep.configContext.InlineScriptUsageCwtConfigContextProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.expressionOffset
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.offsetInParent
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.rightQuoted
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.resolved

/**
 * 提供内联脚本调用的代码补全。
 */
class ParadoxInlineScriptInvocationCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeInlineScriptInvocations) return

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
        if (configContext != null && configContext.provider is InlineScriptUsageCwtConfigContextProvider) return

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

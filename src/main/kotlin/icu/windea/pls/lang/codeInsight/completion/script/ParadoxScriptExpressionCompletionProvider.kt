package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.resolve.parameterValueQuoted
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDataExpression
import icu.windea.pls.script.psi.isDirectValue
import icu.windea.pls.script.psi.isPropertyValue

/**
 * 提供脚本表达式相关的代码补全。基于规则文件。
 */
class ParadoxScriptExpressionCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (!element.isDataExpression()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext).copy(
            expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        )

        // 兼容参数值（包括整行或多行参数值）和内联脚本文件中内容

        val parameterValueQuoted = ParadoxConfigManager.getConfigContext(context.file)?.parameterValueQuoted
        val mayBeKey = parameterValueQuoted != false && (element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isDirectValue()))
        val mayBeDirectValue = element is ParadoxScriptString && element.isDirectValue()
        val mayBePropertyValue = parameterValueQuoted != false && (element is ParadoxScriptString && element.isPropertyValue())

        val resultToUse = result.withPrefixMatcher(context.keyword)
        if (mayBeKey) {
            // 向上得到 block 或者 file
            val memberContainer = element.parentOfType<ParadoxScriptMemberContainer>()
            val member = memberContainer?.parentOfType<ParadoxScriptMember>(withSelf = true)
            if (member != null) {
                ParadoxCompletionManager.addKeyCompletions(context, resultToUse, member)
            }
        }
        if (mayBeDirectValue) {
            // 向上得到 block 或者 file
            val memberContainer = element.parentOfType<ParadoxScriptMemberContainer>()
            val member = memberContainer?.parentOfType<ParadoxScriptMember>(withSelf = true)
            if (member != null) {
                ParadoxCompletionManager.addValueCompletions(context, resultToUse, member)
            }
        }
        if (mayBePropertyValue) {
            // 向上得到property
            val property = element.parentOfType<ParadoxScriptProperty>()
            if (property != null) {
                ParadoxCompletionManager.addPropertyValueCompletions(context, resultToUse, element, property)
            }
        }
    }
}

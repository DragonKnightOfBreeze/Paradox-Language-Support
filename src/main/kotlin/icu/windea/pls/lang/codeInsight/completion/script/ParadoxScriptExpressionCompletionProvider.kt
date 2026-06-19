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
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.isPropertyValue

/**
 * 提供脚本表达式相关的代码补全。基于规则文件。
 */
object ParadoxScriptExpressionCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (!element.isExpression()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext).copy(
            expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        )

        // 兼容参数值（包括整行或多行参数值）和内联脚本文件中内容

        val parameterValueQuoted = ParadoxConfigManager.getConfigContext(context.file)?.parameterValueQuoted
        val mayBeKey = parameterValueQuoted != false && (element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isBlockMember()))
        val mayBeValue = element is ParadoxScriptString && element.isBlockMember()
        val mayBePropertyValue = parameterValueQuoted != false && (element is ParadoxScriptString && element.isPropertyValue())

        val resultToUse = result.withPrefixMatcher(context.keyword)
        if (mayBeKey) {
            // 向上得到 block 或者 file
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMember>(withSelf = true)
            if (memberElement != null) {
                ParadoxCompletionManager.addKeyCompletions(context, resultToUse, memberElement)
            }
        }
        if (mayBeValue) {
            // 向上得到 block 或者 file
            val blockElement = element.parentOfType<ParadoxScriptBlockElement>()
            val memberElement = blockElement?.parentOfType<ParadoxScriptMember>(withSelf = true)
            if (memberElement != null) {
                ParadoxCompletionManager.addValueCompletions(context, resultToUse, memberElement)
            }
        }
        if (mayBePropertyValue) {
            // 向上得到property
            val propertyElement = element.parentOfType<ParadoxScriptProperty>()
            if (propertyElement != null) {
                ParadoxCompletionManager.addPropertyValueCompletions(context, resultToUse, element, propertyElement)
            }
        }
    }
}

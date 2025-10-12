package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptCompletionContributor : CompletionContributor() {
    init {
        // 当用户可能正在输入关键字时提示 - 仅限当前文件无法按文件路径匹配任何规则时提示
        val keywordPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
            .withParent(psiElement(ParadoxScriptString::class.java))
        extend(CompletionType.BASIC, keywordPattern, ParadoxKeywordCompletionProvider())

        // 当用户可能正在输入 scriptedVariable 的名字时提示 - 除非用户也可能正在输入引用的名字
        val scriptedVariableNamePattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.SCRIPTED_VARIABLE_NAME_TOKENS)
        extend(CompletionType.BASIC, scriptedVariableNamePattern, ParadoxScriptedVariableNameCompletionProvider())

        // 当用户可能正在输入定义的名字时提示
        val definitionNamePattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
        extend(CompletionType.BASIC, definitionNamePattern, ParadoxDefinitionNameCompletionProvider())

        // 当用户可能正在输入变量名时提示
        val variableNamePattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
        extend(CompletionType.BASIC, variableNamePattern, ParadoxVariableNameCompletionProvider())

        // 当用户可能正在输入 scriptedVariableReference 时提示
        val scriptedVariableReferencePattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.SCRIPTED_VARIABLE_REFERENCE_TOKENS)
        extend(null, scriptedVariableReferencePattern, ParadoxScriptedVariableCompletionProvider())

        // 当用户可能正在输入 scriptExpression 时提示
        val expressionPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
        extend(null, expressionPattern, ParadoxScriptExpressionCompletionProvider())

        // 当用户可能正在输入 eventId 时提示
        val eventIdPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
            .withParent(
                psiElement(ParadoxScriptString::class.java)
                    .withParent(
                        psiElement(ParadoxScriptProperty::class.java)
                            .withParent(
                                psiElement(ParadoxScriptBlock::class.java)
                                    .withParent(psiElement(ParadoxScriptProperty::class.java))
                            )
                    )
            )
        extend(null, eventIdPattern, ParadoxEventIdCompletionProvider())

        // 当用户可能正在输入 parameter 的名字时提示
        val parameterPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.PARAMETER_TOKENS)
        extend(null, parameterPattern, ParadoxParameterCompletionProvider())

        // 提供内联脚本调用（`inline_script = ...`）的代码补全
        val inlineScriptInvocationPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
        extend(null, inlineScriptInvocationPattern, ParadoxInlineScriptInvocationCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}

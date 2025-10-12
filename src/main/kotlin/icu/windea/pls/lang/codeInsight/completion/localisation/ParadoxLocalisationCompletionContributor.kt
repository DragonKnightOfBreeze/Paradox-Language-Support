package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_NAME_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LOCALE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.constants.PlsConstants

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
    init {
        // 当用户可能正在输入语言区域的名字时提示
        val localePattern = or(psiElement(LOCALE_TOKEN), psiElement(PROPERTY_KEY_TOKEN))
        extend(CompletionType.BASIC, localePattern, ParadoxLocalisationLocaleCompletionProvider())

        // 当用户可能正在输入本地化的名字时提示
        val localisationNamePattern = psiElement(PROPERTY_KEY_TOKEN)
        extend(CompletionType.BASIC, localisationNamePattern, ParadoxLocalisationNameCompletionProvider())

        // 当用户可能正在输入color的ID时提示（因为colorId只有一个字符，这里需要特殊处理）
        val colorPattern = psiElement().atStartOf(psiElement().afterLeaf("§"))
        extend(null, colorPattern, ParadoxLocalisationColorCompletionProvider())

        // 当用户可能正在输入 parameter 的名字时提示
        val parameterPattern = psiElement(PARAMETER_TOKEN)
        extend(null, parameterPattern, ParadoxLocalisationParameterCompletionProvider())

        // 当用户可能正在输入 scriptedVariableReference 的名字时提示
        val scriptedVariableReferencePattern = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKEN)
        extend(null, scriptedVariableReferencePattern, ParadoxScriptedVariableCompletionProvider())

        // 当用户可能正在输入 localisationExpression 时提示
        val expressionPattern = psiElement().withElementType(ParadoxLocalisationTokenSets.EXPRESSION_TOKENS)
        extend(null, expressionPattern, ParadoxLocalisationExpressionCompletionProvider())

        // 当用户可能正在输入 icon 的名字时提示
        val iconPattern = psiElement(ICON_TOKEN)
        extend(null, iconPattern, ParadoxLocalisationIconCompletionProvider())

        // 当用户可能正在输入 conceptName 时提示
        val conceptNamePattern = psiElement(CONCEPT_NAME_TOKEN)
        extend(null, conceptNamePattern, ParadoxLocalisationConceptCompletionProvider())

        // 当用户可能正在输入 textFormat 时提示
        val textFormatPattern = psiElement(TEXT_FORMAT_TOKEN)
        extend(null, textFormatPattern, ParadoxLocalisationTextFormatCompletionProvider())

        // 当用户可能正在输入 textIcon 时提示
        val textIconPattern = psiElement(TEXT_ICON_TOKEN)
        extend(null, textIconPattern, ParadoxLocalisationTextIconCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}

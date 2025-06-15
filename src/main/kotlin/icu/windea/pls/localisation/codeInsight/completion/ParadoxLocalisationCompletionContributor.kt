package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constants.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
    init {
        //当用户可能正在输入一个locale的名字时提示
        val localePattern = or(psiElement(LOCALE_TOKEN), psiElement(PROPERTY_KEY_TOKEN))
        extend(localePattern, ParadoxLocalisationLocaleCompletionProvider())

        //当用户可能正在输入一个localisation的名字时提示
        val localisationNamePattern = psiElement(PROPERTY_KEY_TOKEN)
        extend(CompletionType.BASIC, localisationNamePattern, ParadoxLocalisationNameCompletionProvider())

        //当用户可能正在输入一个color的ID时提示（因为colorId只有一个字符，这里需要特殊处理）
        val colorPattern = psiElement().atStartOf(psiElement().afterLeaf("§"))
        extend(colorPattern, ParadoxLocalisationColorCompletionProvider())

        //当用户可能正在输入一个 parameter 的名字时提示
        val parameterPattern = psiElement(PARAMETER_TOKEN)
        extend(parameterPattern, ParadoxLocalisationParameterCompletionProvider())

        //当用户可能正在输入一个 scriptedVariableReference 的名字时提示
        val scriptedVariableReferencePattern = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKEN)
        extend(scriptedVariableReferencePattern, ParadoxScriptedVariableCompletionProvider())

        //当用户可能正在输入一个 localisationExpression 时提示
        val expressionPattern = psiElement().withElementType(ParadoxLocalisationTokenSets.EXPRESSION_TOKENS)
        extend(expressionPattern, ParadoxLocalisationExpressionCompletionProvider())

        //当用户可能正在输入一个 icon 的名字时提示
        val iconPattern = psiElement(ICON_TOKEN)
        extend(iconPattern, ParadoxLocalisationIconCompletionProvider())

        //当用户可能正在输入一个 conceptName 时提示
        val conceptNamePattern = psiElement(CONCEPT_NAME_TOKEN)
        extend(conceptNamePattern, ParadoxLocalisationConceptCompletionProvider())

        //当用户可能正在输入一个 textFormat 时提示
        val textFormatPattern = psiElement(TEXT_FORMAT_TOKEN)
        extend(textFormatPattern, ParadoxLocalisationTextFormatCompletionProvider())

        //当用户可能正在输入一个 textIcon 时提示
        val textIconPattern = psiElement(TEXT_ICON_TOKEN)
        extend(textIconPattern, ParadoxLocalisationTextIconCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}

package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.editor.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxResolutionManager
import icu.windea.pls.model.expressions.ParadoxExpression

// Core (limited support)

/**
 * @see CwtDataTypes.Definition
 */
class ParadoxCsvDefinitionExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Definition
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {
        val attributesKey = ParadoxSemanticHighlighterColors.definitionReference(element.language)
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.value ?: return null
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(text, type, selector).find()
    }

    override fun resolveAll(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): List<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.value ?: return emptyList()
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(text, type, selector).findAll()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeDefinition(context, result)
    }
}

/**
 * @see CwtDataTypes.EnumValue
 */
class ParadoxCsvEnumValueExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {
        val configGroup = config.configGroup
        val enumName = config.configExpression.value ?: return
        val attributesKey = when {
            configGroup.complexEnums[enumName] != null -> ParadoxSemanticHighlighterColors.complexEnumValue(element.language)
            else -> ParadoxSemanticHighlighterColors.enumValue(element.language)
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        return ParadoxResolutionManager.resolveEnumValue(element, text, config)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeEnumValue(context, result)
    }
}

/**
 * @see CwtDataTypes.UnionValue
 */
class ParadoxCsvUnionValueExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.UnionValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {
        val configGroup = config.configGroup
        val unionName = config.configExpression.value ?: return
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedCsvUnionCandidate(element, expression, unionName, configGroup) ?: return
        ParadoxExpressionService.annotateCsvExpression(element, rangeInElement, text, valueConfig, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        val configGroup = config.configGroup
        val unionName = config.configExpression.value ?: return null
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedCsvUnionCandidate(element, expression, unionName, configGroup) ?: return null
        return ParadoxExpressionManager.resolveCsvExpression(element, rangeInElement, valueConfig)
    }

    override fun resolveAll(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): List<PsiElement> {
        val configGroup = config.configGroup
        val unionName = config.configExpression.value ?: return emptyList()
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedCsvUnionCandidate(element, expression, unionName, configGroup) ?: return emptyList()
        return ParadoxExpressionManager.resolveAllCsvExpression(element, rangeInElement, valueConfig)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // if (context.keyword.isParameterized()) return // 2.2.0 兼容可能带参数的情况
        ParadoxExpressionCompletionManager.completeCsvUnionValue(context, result)
    }
}

/**
 * @see CwtDataTypeSets.DynamicValue
 */
class ParadoxCsvDynamicValueExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.DynamicValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {
        val attributesKey = ParadoxSemanticHighlighterColors.dynamicValue(element.language)
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        return ParadoxResolutionManager.resolveDynamicValue(element, text, config)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
    }
}

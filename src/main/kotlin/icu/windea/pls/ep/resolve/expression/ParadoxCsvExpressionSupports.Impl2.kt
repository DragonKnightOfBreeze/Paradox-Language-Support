package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.editor.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxResolutionManager

// Core (limited support)

/**
 * @see CwtDataTypes.Definition
 */
class ParadoxCsvDefinitionExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Definition
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val attributesKey = ParadoxSemanticHighlighterColors.definitionReference(element.language)
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.value ?: return null
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = selector(project, element).definition().contextSensitive()
        return ParadoxDefinitionSearch.searchElement(expressionText, type, selector).find()
    }

    override fun multiResolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.value ?: return emptySet()
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = selector(project, element).definition().contextSensitive()
        return ParadoxDefinitionSearch.searchElement(expressionText, type, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDefinition(context, result)
    }
}

/**
 * @see CwtDataTypes.EnumValue
 */
class ParadoxCsvEnumValueExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
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

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        return ParadoxResolutionManager.resolveEnumValue(element, expressionText, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeEnumValue(context, result)
    }
}

/**
 * @see CwtDataTypeSets.DynamicValue
 */
class ParadoxCsvDynamicValueExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.DynamicValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val attributesKey = ParadoxSemanticHighlighterColors.dynamicValue(element.language)
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        return ParadoxResolutionManager.resolveDynamicValue(element, expressionText, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDynamicValue(context, result)
    }
}

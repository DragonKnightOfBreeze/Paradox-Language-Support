package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.highlighting.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.manipulation.ParadoxConfigManipulationService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.expressions.ParadoxExpression

// Core (limited support)

/**
 * @see CwtDataTypes.Definition
 */
class ParadoxCsvDefinitionExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Definition

    override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
        val attributesKey = ParadoxSemanticHighlighterColors.definitionReference(element.language)
        ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
        return true
    }

    override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.metadata.value ?: return null
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(text, type, selector).find()
    }

    override fun resolveAll(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.metadata.value ?: return emptyList()
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
class ParadoxCsvEnumValueExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.EnumValue

    override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
        val configGroup = config.configGroup
        val enumName = config.configExpression.metadata.value ?: return false
        val attributesKey = when {
            configGroup.complexEnums[enumName] != null -> ParadoxSemanticHighlighterColors.complexEnumValue(element.language)
            else -> ParadoxSemanticHighlighterColors.enumValue(element.language)
        }
        ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
        return true
    }

    override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
        return ParadoxExpressionSupportFactory.resolveEnumValue(element, text, config)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeEnumValue(context, result)
    }
}

/**
 * @see CwtDataTypeSets.DynamicValue
 */
class ParadoxCsvDynamicValueExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.DynamicValue

    override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
        val attributesKey = ParadoxSemanticHighlighterColors.dynamicValue(element.language)
        ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
        return true
    }

    override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
        return ParadoxExpressionSupportFactory.resolveDynamicValue(element, text, config)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
    }
}

/**
 * @see CwtDataTypes.UnionValue
 */
class ParadoxCsvUnionValueExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

    // NOTE 3.0.1 recursion guard is required here for various operations

    override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
        val configGroup = config.configGroup
        val unionName = config.configExpression.metadata.value ?: return false
        // NOTE 3.0.1 recursion guard is required here
        // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
        val processor = ProcessorFactory.find<CwtValueConfig>()
        runWithRecursionGuard("csvExpression.annotate.union", unionName) {
            val expression = ParadoxExpression.resolve(element)
            ParadoxConfigManipulationService.expandMatchedUnionValues(element, expression, unionName, configGroup) {
                processor.process(it)
            }
        }
        val unionValueConfig = processor.result ?: return false
        return ParadoxExpressionService.annotateCsvExpression(element, text, rangeInExpression, unionValueConfig, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
        val configGroup = config.configGroup
        val unionName = config.configExpression.metadata.value ?: return null
        // NOTE 3.0.1 recursion guard is required here
        // NOTE 3.0.3 use first matched config directly atm, event if the result from this config is null or empty
        val processor = ProcessorFactory.find<PsiElement>()
        runWithRecursionGuard("csvExpression.resolve.union", unionName) {
            val expression = ParadoxExpression.resolve(element)
            ParadoxConfigManipulationService.expandMatchedUnionValues(element, expression, unionName, configGroup) p@{ unionValueConfig ->
                val r = ParadoxExpressionService.resolveCsvExpression(element, text, rangeInExpression, unionValueConfig)
                if (r == null) return@p true
                processor.process(r)
            }
        }
        return processor.result
    }

    override fun resolveAll(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
        val configGroup = config.configGroup
        val unionName = config.configExpression.metadata.value ?: return emptyList()
        // NOTE 3.0.1 recursion guard is required here
        val processor = ProcessorFactory.find<List<PsiElement>>()
        runWithRecursionGuard("csvExpression.resolveAll.union", unionName) {
            val expression = ParadoxExpression.resolve(element)
            ParadoxConfigManipulationService.expandMatchedUnionValues(element, expression, unionName, configGroup) p@{ unionValueConfig ->
                val r = ParadoxExpressionService.resolveAllCsvExpression(element, text, rangeInExpression, unionValueConfig).orNull()
                if (r == null) return@p true
                processor.process(r)
            }
        }
        return processor.result.orEmpty()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // if (context.keyword.isParameterized()) return // 2.2.0 兼容可能带参数的情况
        ParadoxExpressionCompletionManager.completeCsvUnionValue(context, result)
    }
}

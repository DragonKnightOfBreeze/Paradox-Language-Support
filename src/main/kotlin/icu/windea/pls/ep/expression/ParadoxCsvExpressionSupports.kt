package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.complexEnumValue
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

// 目前仅提供有限的支持

class ParadoxCsvBoolExpressionSupport: ParadoxCsvExpressionSupport {
    override fun supports(config: CwtValueConfig): Boolean {
        return config.configExpression.type == CwtDataTypes.Bool
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(ParadoxCompletionManager.yesLookupElement, context)
        result.addElement(ParadoxCompletionManager.noLookupElement, context)
    }
}

class ParadoxCsvDefinitionExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(config: CwtValueConfig): Boolean {
        return config.configExpression.type == CwtDataTypes.Definition
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
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
        return ParadoxDefinitionSearch.search(expressionText, type, selector).find()
    }

    override fun multiResolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression.value ?: return emptySet()
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = selector(project, element).definition().contextSensitive()
        return ParadoxDefinitionSearch.search(expressionText, type, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDefinition(context, result)
    }
}

class ParadoxCsvEnumValueExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(config: CwtValueConfig): Boolean {
        return config.configExpression.type == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val configGroup = config.configGroup
        val enumName = config.configExpression.value ?: return
        val attributesKey = when {
            configGroup.enums[enumName] != null -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
            configGroup.complexEnums[enumName] != null -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        val enumName = config.configExpression.value ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        // 尝试解析为简单枚举
        val enumConfig = configGroup.enums[enumName]
        if (enumConfig != null) {
            return ParadoxExpressionManager.resolvePredefinedEnumValue(expressionText, enumName, configGroup)
        }
        // 尝试解析为复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            val searchScope = complexEnumConfig.searchScopeType
            val selector = selector(project, element).complexEnumValue()
                .withSearchScopeType(searchScope)
            // .contextSensitive(exact) // unnecessary
            val info = ParadoxComplexEnumValueSearch.search(expressionText, enumName, selector).findFirst()
            if (info != null) {
                val readWriteAccess = ReadWriteAccessDetector.Access.Read // usage
                return ParadoxComplexEnumValueElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
            }
        }
        return null
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeEnumValue(context, result)
    }
}

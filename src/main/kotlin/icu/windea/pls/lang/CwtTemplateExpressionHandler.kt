package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.psi.*

object CwtTemplateExpressionHandler {
    fun extract(expression: CwtTemplateExpression, referenceName: String): String {
        if(expression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for(snippetExpression in expression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }
    
    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if(templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            for(snippetExpression in templateExpression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }
    
    private val regexCache = CacheBuilder.newBuilder().buildCache<CwtTemplateExpression, Regex> { doToRegex(it) }
    
    private fun toRegex(configExpression: CwtTemplateExpression): Regex {
        return regexCache.get(configExpression)
    }
    
    private fun doToRegex(configExpression: CwtTemplateExpression): Regex {
        return buildString {
            configExpression.snippetExpressions.forEach {
                if(it.type == CwtDataType.Constant) {
                    append("\\Q").append(it.expressionString).append("\\E")
                } else {
                    append("(.*?)")
                }
            }
        }.toRegex(RegexOption.IGNORE_CASE)
    }
    
    fun matches(text: String, element: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, matchOptions: Int = ParadoxConfigMatcher.Options.Default): Boolean {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return false
        val expressionString = text.unquote()
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if(configExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        for(snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if(snippetExpression.type != CwtDataType.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return false
                val referenceName = matchGroup.value
                val expression = ParadoxDataExpression.resolve(referenceName, false)
                val matched = try {
                    ParadoxConfigMatcher.matches(element, expression, snippetExpression, null, configGroup, matchOptions).get(matchOptions)
                } catch(e: Exception) {
                    //java.lang.Throwable: Indexing process should not rely on non-indexed file data
                    return false
                }
                if(!matched) return false
            }
        }
        return true
    }
    
    fun resolve(text: String, element: ParadoxScriptStringExpressionElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        //需要保证里面的每个引用都能解析
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val references = resolveReferences(text, element, configExpression, configGroup)
        if(references.isEmpty()) return null
        return ParadoxTemplateExpressionElement(element, text, configExpression, gameType, project, references)
    }
    
    fun resolveReferences(text: String, element: ParadoxScriptStringExpressionElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetExpressionReference> {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return emptyList()
        val expressionString = text
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return emptyList()
        if(configExpression.referenceExpressions.size != matchResult.groups.size - 1) return emptyList()
        val references = mutableListOf<ParadoxTemplateSnippetExpressionReference>()
        var i = 1
        for(snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if(snippetExpression.type != CwtDataType.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return emptyList()
                val referenceName = matchGroup.value
                val range = TextRange.create(matchGroup.range.first, matchGroup.range.last)
                val reference = ParadoxTemplateSnippetExpressionReference(element, range, referenceName, snippetExpression, configGroup)
                references.add(reference)
            } else {
                //ignore
            }
        }
        return references
    }
    
    fun processResolveResult(contextElement: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>) {
        doProcessResolveResult(contextElement, configExpression, configGroup, processor, 0, "")
    }
    
    private fun doProcessResolveResult(
        contextElement: PsiElement,
        configExpression: CwtTemplateExpression,
        configGroup: CwtConfigGroup,
        processor: Processor<String>,
        index: Int,
        builder: String
    ) {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        if(index == configExpression.snippetExpressions.size) {
            if(builder.isNotEmpty()) {
                processor.process(builder)
            }
            return
        }
        val snippetExpression = configExpression.snippetExpressions[index]
        when(snippetExpression.type) {
            CwtDataType.Constant -> {
                val text = snippetExpression.expressionString
                doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + text)
            }
            CwtDataType.Definition -> {
                val typeExpression = snippetExpression.value ?: return
                val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
                ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name ?: return@p true
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    true
                }
            }
            CwtDataType.EnumValue -> {
                val enumName = snippetExpression.value ?: return
                //提示简单枚举
                val enumConfig = configGroup.enums[enumName]
                if(enumConfig != null) {
                    ProgressManager.checkCanceled()
                    val enumValueConfigs = enumConfig.valueConfigMap.values
                    if(enumValueConfigs.isEmpty()) return
                    for(enumValueConfig in enumValueConfigs) {
                        val name = enumValueConfig.value
                        doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    }
                }
                //提示复杂枚举值
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    ProgressManager.checkCanceled()
                    val searchScope = complexEnumConfig.searchScopeType
                    val selector = complexEnumValueSelector(project, contextElement)
                        .withSearchScopeType(searchScope)
                        .contextSensitive()
                        .distinctByName()
                    ParadoxComplexEnumValueSearch.search(enumName, selector).processQueryAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        val name = info.name
                        doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                        true
                    }
                }
            }
            CwtDataType.Value -> {
                val valueSetName = snippetExpression.value ?: return
                ProgressManager.checkCanceled()
                val valueConfig = configGroup.values[valueSetName] ?: return
                val valueSetValueConfigs = valueConfig.valueConfigMap.values
                if(valueSetValueConfigs.isEmpty()) return
                for(valueSetValueConfig in valueSetValueConfigs) {
                    val name = valueSetValueConfig.value
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                }
                ProgressManager.checkCanceled()
                val selector = valueSetValueSelector(project, contextElement).distinctByName()
                ParadoxValueSetValueSearch.search(valueSetName, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    //去除后面的作用域信息
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + info.name)
                    true
                }
            }
            else -> pass()
        }
    }
}
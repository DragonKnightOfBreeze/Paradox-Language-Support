package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

object CwtTemplateExpressionHandler {
    fun extract(expression: CwtTemplateExpression, referenceName: String): String {
        if(expression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for(snippetExpression in expression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
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
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
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
                if(it.type == CwtDataTypes.Constant) {
                    append("\\Q").append(it.expressionString).append("\\E")
                } else {
                    append("(.*?)")
                }
            }
        }.toRegex(RegexOption.IGNORE_CASE)
    }
    
    fun matches(text: String, contextElement: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, matchOptions: Int = CwtConfigMatcher.Options.Default): Boolean {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return false
        val expressionString = text.unquote()
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if(configExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        for(snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if(snippetExpression.type != CwtDataTypes.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return false
                val referenceName = matchGroup.value
                val expression = ParadoxDataExpression.resolve(referenceName, false)
                val matched = CwtConfigMatcher.matches(contextElement, expression, snippetExpression, null, configGroup, matchOptions).get(matchOptions)
                if(!matched) return false
            }
        }
        return true
    }
    
    fun resolve(text: String, contextElement: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        //需要保证里面的每个引用都能解析
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val references = resolveReferences(text, configExpression, configGroup)
        if(references.isEmpty()) return null
        return ParadoxTemplateExpressionElement(contextElement, text, configExpression, gameType, project, references)
    }
    
    fun resolveReferences(text: String, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetExpressionReference> {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return emptyList()
        val expressionString = text
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return emptyList()
        if(configExpression.referenceExpressions.size != matchResult.groups.size - 1) return emptyList()
        //element仅仅表示上下文元素，因此这里需要生成ParadoxScriptStringExpressionElement并传给ParadoxTemplateSnippetExpressionReference
        val templateElement by lazy { ParadoxScriptElementFactory.createString(configGroup.project, text) }
        val templateReferences = mutableListOf<ParadoxTemplateSnippetExpressionReference>()
        var i = 1
        for(snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if(snippetExpression.type != CwtDataTypes.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return emptyList()
                val referenceName = matchGroup.value
                val range = TextRange.create(matchGroup.range.first, matchGroup.range.last)
                val reference = ParadoxTemplateSnippetExpressionReference(templateElement, range, referenceName, snippetExpression, configGroup)
                templateReferences.add(reference)
            } else {
                //ignore
            }
        }
        return templateReferences
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
            CwtDataTypes.Constant -> {
                val text = snippetExpression.expressionString
                doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + text)
            }
            CwtDataTypes.Definition -> {
                val typeExpression = snippetExpression.value ?: return
                val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
                ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name ?: return@p true
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    true
                }
            }
            CwtDataTypes.EnumValue -> {
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
            CwtDataTypes.Value -> {
                val dynamicValueType = snippetExpression.value ?: return
                ProgressManager.checkCanceled()
                val valueConfig = configGroup.dynamicValues[dynamicValueType] ?: return
                val dynamicValueConfigs = valueConfig.valueConfigMap.values
                if(dynamicValueConfigs.isEmpty()) return
                for(dynamicValueConfig in dynamicValueConfigs) {
                    val name = dynamicValueConfig.value
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                }
                ProgressManager.checkCanceled()
                val selector = dynamicValueSelector(project, contextElement).distinctByName()
                ParadoxDynamicValueSearch.search(dynamicValueType, selector).processQueryAsync p@{ info ->
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
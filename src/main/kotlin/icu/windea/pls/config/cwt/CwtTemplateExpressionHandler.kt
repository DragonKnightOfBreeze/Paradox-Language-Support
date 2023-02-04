package icu.windea.pls.config.cwt

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

object CwtTemplateExpressionHandler {
    @JvmStatic
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
    
    @JvmStatic
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
    
    @JvmStatic
    fun matches(text: String, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
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
                val isMatched = CwtConfigHandler.matchesScriptExpression(null, expression, snippetExpression, null, configGroup, matchType)
                if(!isMatched) return false
            }
        }
        return true
    }
    
    @JvmStatic
    fun resolve(element: ParadoxScriptStringExpressionElement, text: String, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        //需要保证里面的每个引用都能解析
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val references = resolveReferences(element, text, configExpression, configGroup)
        if(references.isEmpty()) return null
        return ParadoxTemplateExpressionElement(element, text, configExpression, project, gameType, references)
    }
    
    
    @JvmStatic
    fun resolveReferences(element: ParadoxScriptStringExpressionElement, text: String, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): List<ParadoxInTemplateExpressionReference> {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return emptyList()
        val expressionString = text
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return emptyList()
        if(configExpression.referenceExpressions.size != matchResult.groups.size - 1) return emptyList()
        val references = SmartList<ParadoxInTemplateExpressionReference>()
        var i = 1
        for(snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if(snippetExpression.type != CwtDataType.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return emptyList()
                val referenceName = matchGroup.value
                val range = TextRange.create(matchGroup.range.first, matchGroup.range.last)
                val reference = ParadoxInTemplateExpressionReference(element, range, referenceName, snippetExpression, configGroup)
                references.add(reference)
            } else {
                //ignore
            }
        }
        return references
    }
    
    @JvmStatic
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
        val gameType = configGroup.gameType ?: return
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
                val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
                val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
                definitionQuery.processQuery { definition ->
                    val name = definition.definitionInfo?.name ?: return@processQuery true
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    true
                }
            }
            CwtDataType.Enum -> {
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
                //提示复杂枚举
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    ProgressManager.checkCanceled()
                    val searchScope = complexEnumConfig.searchScope
                    val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, contextElement).preferRootFrom(contextElement).distinctByName()
                    val query = ParadoxComplexEnumValueSearch.searchAll(enumName, project, selector = selector)
                    query.processQuery { complexEnum ->
                        val name = complexEnum.value
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
                val selector = valueSetValueSelector().gameType(gameType)
                    .notSamePosition(contextElement)
                    .distinctByValue()
                val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
                valueSetValueQuery.processQuery { valueSetValue ->
                    //去除后面的作用域信息
                    val name = ParadoxValueSetValueHandler.getName(valueSetValue) ?: return@processQuery true
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    true
                }
            }
            else -> pass()
        }
    }
}
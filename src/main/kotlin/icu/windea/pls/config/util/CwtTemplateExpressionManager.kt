package icu.windea.pls.config.util

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

object CwtTemplateExpressionManager {
    fun extract(templateExpression: CwtTemplateExpression, referenceName: String): String {
        if (templateExpression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for (snippetExpression in templateExpression.snippetExpressions) {
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }

    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if (templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            for (snippetExpression in templateExpression.snippetExpressions) {
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }

    fun toRegex(templateExpression: CwtTemplateExpression): Regex {
        return regexCache.get(templateExpression)
    }

    private val regexCache = CacheBuilder.newBuilder().buildCache<CwtTemplateExpression, Regex> { doToRegex(it) }

    private fun doToRegex(templateExpression: CwtTemplateExpression): Regex {
        return buildString {
            templateExpression.snippetExpressions.forEach {
                if (it.type == CwtDataTypes.Constant) {
                    append("\\Q").append(it.expressionString).append("\\E")
                } else {
                    append("(.*?)")
                }
            }
        }.toRegex(RegexOption.IGNORE_CASE)
    }

    fun matches(text: String, contextElement: PsiElement, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, matchOptions: Int = ParadoxExpressionMatcher.Options.Default): Boolean {
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val expressionString = text.unquote()
        val regex = toRegex(templateExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        for (snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if (snippetExpression.type != CwtDataTypes.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return false
                val referenceName = matchGroup.value
                val expression = ParadoxDataExpression.resolve(referenceName, false)
                val matched = ParadoxExpressionMatcher.matches(contextElement, expression, snippetExpression, null, configGroup, matchOptions).get(matchOptions)
                if (!matched) return false
            }
        }
        return true
    }

    fun resolve(text: String, contextElement: PsiElement, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        //需要保证里面的每个引用都能解析
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val references = resolveReferences(text, templateExpression, configGroup)
        if (references.isEmpty()) return null
        return ParadoxTemplateExpressionElement(contextElement, text, templateExpression, gameType, project, references)
    }

    fun resolveReferences(text: String, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetPsiExpressionReference> {
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return emptyList()
        val expressionString = text
        val regex = toRegex(templateExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return emptyList()
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return emptyList()
        //element仅仅表示上下文元素，因此这里需要生成ParadoxScriptStringExpressionElement并传给ParadoxTemplateSnippetExpressionReference
        val templateElement by lazy { ParadoxScriptElementFactory.createString(configGroup.project, text) }
        val templateReferences = mutableListOf<ParadoxTemplateSnippetPsiExpressionReference>()
        var i = 1
        for (snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if (snippetExpression.type != CwtDataTypes.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return emptyList()
                val referenceName = matchGroup.value.intern() //intern to optimize memory
                val range = TextRange.create(matchGroup.range.first, matchGroup.range.last)
                val reference = ParadoxTemplateSnippetPsiExpressionReference(templateElement, range, referenceName, snippetExpression, configGroup)
                templateReferences.add(reference)
            } else {
                //ignore
            }
        }
        return templateReferences
    }

    fun processResolveResult(contextElement: PsiElement, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>) {
        doProcessResolveResult(contextElement, templateExpression, configGroup, processor, 0, "")
    }

    private fun doProcessResolveResult(contextElement: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>, index: Int, builder: String) {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        if (index == configExpression.snippetExpressions.size) {
            if (builder.isNotEmpty()) {
                processor.process(builder)
            }
            return
        }
        val snippetExpression = configExpression.snippetExpressions[index]
        when (snippetExpression.type) {
            CwtDataTypes.Constant -> {
                val text = snippetExpression.expressionString
                doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + text)
            }
            CwtDataTypes.Definition -> {
                val typeExpression = snippetExpression.value ?: return
                val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
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
                if (enumConfig != null) {
                    ProgressManager.checkCanceled()
                    val enumValueConfigs = enumConfig.valueConfigMap.values
                    if (enumValueConfigs.isEmpty()) return
                    for (enumValueConfig in enumValueConfigs) {
                        val name = enumValueConfig.value
                        doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    }
                }
                //提示复杂枚举值
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if (complexEnumConfig != null) {
                    ProgressManager.checkCanceled()
                    val searchScope = complexEnumConfig.searchScopeType
                    val selector = selector(project, contextElement).complexEnumValue()
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
                val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return
                val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
                if (dynamicValueTypeConfigs.isEmpty()) return
                for (dynamicValueTypeConfig in dynamicValueTypeConfigs) {
                    val name = dynamicValueTypeConfig.value
                    doProcessResolveResult(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                }
                ProgressManager.checkCanceled()
                val selector = selector(project, contextElement).dynamicValue().distinctByName()
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

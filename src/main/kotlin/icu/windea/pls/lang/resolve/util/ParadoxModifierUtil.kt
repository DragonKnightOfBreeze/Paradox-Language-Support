package icu.windea.pls.lang.resolve.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.noneFast
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxEconomicCategoryManager
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxModifierUtil {
    fun processPredefinedModifierConfig(configGroup: CwtConfigGroup, processor: Processor<CwtModifierConfig>): Boolean {
        val modifiers = configGroup.predefinedModifiers
        if (modifiers.isEmpty()) return true
        for (modifierConfig in modifiers.values) {
            ProgressManager.checkCanceled()
            val r = processor.process(modifierConfig)
            if (!r) return false
        }
        return true
    }

    fun processGeneratedModifierConfig(configGroup: CwtConfigGroup, processor: Processor<CwtModifierConfig>): Boolean {
        val modifiers = configGroup.generatedModifiers
        if (modifiers.isEmpty()) return true
        for (modifierConfig in modifiers.values) {
            ProgressManager.checkCanceled()
            if (modifierConfig.template.expressionString.isEmpty()) continue
            val r = processor.process(modifierConfig)
            if (!r) return false
        }
        return true
    }

    fun processModifierTemplate(element: PsiElement, configGroup: CwtConfigGroup, templateExpression: CwtTemplateExpression, processor: Processor<String>): Boolean {
        return processModifierTemplateRecursively(element, configGroup, templateExpression, 0, "", processor)
    }

    private fun processModifierTemplateRecursively(element: PsiElement, configGroup: CwtConfigGroup, templateExpression: CwtTemplateExpression, index: Int, builder: String, processor: Processor<String>): Boolean {
        ProgressManager.checkCanceled()
        if (index == templateExpression.snippetExpressions.size) {
            if (builder.isNotEmpty()) {
                return processor.process(builder)
            }
            return true
        }
        val snippetExpression = templateExpression.snippetExpressions[index]
        return processModifierTemplateSnippet(element, configGroup, snippetExpression) { processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + it, processor) }
    }

    /**
     * 仅限特定数据类型（[CwtDataTypeSets.ModifierTemplateAware]）的片段。
     */
    fun processModifierTemplateSnippet(element: PsiElement, configGroup: CwtConfigGroup, snippetExpression: CwtDataExpression, processor: Processor<String>): Boolean {
        val project = configGroup.project
        return when (snippetExpression.type) {
            CwtDataTypes.Constant -> {
                val text = snippetExpression.expressionString
                processor.process(text)
            }
            CwtDataTypes.Definition -> {
                // 遍历已索引的定义
                val typeExpression = snippetExpression.metadata.value ?: return true
                ProgressManager.checkCanceled()
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive().distinct()
                ParadoxDefinitionSearch.searchElement(null, typeExpression, selector).processAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name
                    if (name.isNullOrEmpty()) return@p true
                    processor.process(name)
                }
            }
            CwtDataTypes.EnumValue -> {
                val enumName = snippetExpression.metadata.value ?: return true
                // 遍历预定义的简单枚举值
                run {
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val enumValueConfigs = enumConfig.valueConfigMap.values
                    if (enumValueConfigs.isEmpty()) return@run
                    ProgressManager.checkCanceled()
                    val r = enumValueConfigs.process { enumValueConfig ->
                        val name = enumValueConfig.value
                        processor.process(name)
                    }
                    if (!r) return false
                }
                // 遍历已索引的复杂枚举值
                run {
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    ProgressManager.checkCanceled()
                    val searchScopeType = complexEnumConfig.searchScopeType
                    val selector = ParadoxComplexEnumValueSearch.selector(project, element).contextSensitive().distinct()
                        .withSearchScopeType(searchScopeType)
                    val r = ParadoxComplexEnumValueSearch.search(null, enumName, selector).processAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        val name = info.name
                        processor.process(name)
                    }
                    if (!r) return false
                }
                true
            }
            CwtDataTypes.Value -> {
                val dynamicValueType = snippetExpression.metadata.value ?: return true
                // 遍历预定义的动态值
                run {
                    val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return@run
                    val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
                    if (dynamicValueTypeConfigs.isEmpty()) return@run
                    ProgressManager.checkCanceled()
                    val r = dynamicValueTypeConfigs.process { dynamicValueTypeConfig ->
                        val name = dynamicValueTypeConfig.value
                        processor.process(name)
                    }
                    if (!r) return false
                }
                // 遍历已索引的动态值
                run {
                    ProgressManager.checkCanceled()
                    val selector = ParadoxDynamicValueSearch.selector(project, element).distinct()
                    val r = ParadoxDynamicValueSearch.search(null, dynamicValueType, selector).processAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        // 去除后面的作用域信息
                        val name = info.name
                        processor.process(name)
                    }
                    if (!r) return false
                }
                true
            }
            else -> true
        }
    }

    fun processEconomicCategoryInfo(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxEconomicCategoryInfo>): Boolean {
        ProgressManager.checkCanceled()
        val selector = ParadoxDefinitionSearch.selector(configGroup.project, element).contextSensitive().distinct()
            .withConstraint(ParadoxDefinitionIndexConstraint.EconomicCategory)
        return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).processAsync p@{ economicCategory ->
            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            ProgressManager.checkCanceled()
            processor.process(economicCategoryInfo)
        }
    }

    fun processOrderedEconomicCategoryInfo(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxEconomicCategoryInfo>): Boolean {
        ProgressManager.checkCanceled()
        val selector = ParadoxDefinitionSearch.selector(configGroup.project, element).contextSensitive().distinct()
            .withConstraint(ParadoxDefinitionIndexConstraint.EconomicCategory)
        return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).findAll().process p@{ economicCategory ->
            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            ProgressManager.checkCanceled()
            processor.process(economicCategoryInfo)
        }
    }

    /**
     * 检查指定的 [templateExpression] 是否可以被精确匹配（不存在可能有歧义的引用）。
     *
     * 这个方法不会尝试解析动态片段的引用。
     */
    fun checkModifierTemplate(templateExpression: ParadoxTemplateExpression): Boolean {
        return templateExpression.nodes.noneFast { it is ParadoxTemplateSnippetNode && !checkModifierTemplateSnippet(it) }
    }

    /**
     * 检查指定的 [templateExpression] 是否可以被精确匹配（不存在可能有歧义的引用）。
     *
     * 这个方法会尝试解析动态片段的引用，并使用 [element] 作为上下文。
     */
    fun checkModifierTemplate(templateExpression: ParadoxTemplateExpression, element: PsiElement): Boolean {
        return templateExpression.nodes.noneFast { it is ParadoxTemplateSnippetNode && !checkModifierTemplateSnippet(it, element) }
    }

    /**
     * 仅限特定数据类型（[CwtDataTypeSets.ModifierTemplateAware]）的片段。
     */
    fun checkModifierTemplateSnippet(snippet: ParadoxTemplateSnippetNode): Boolean {
        val snippetExpression = snippet.configExpression
        val configGroup = snippet.configGroup
        return when (snippetExpression.type) {
            CwtDataTypes.Constant -> true
            CwtDataTypes.Definition -> false
            CwtDataTypes.EnumValue -> snippetExpression.metadata.value !in configGroup.complexEnums.keys
            CwtDataTypes.Value -> true // anything
            else -> false // unexpected
        }
    }

    /**
     * 仅限特定数据类型（[CwtDataTypeSets.ModifierTemplateAware]）的片段。并且，匹配时忽略大小写。
     */
    fun checkModifierTemplateSnippet(snippet: ParadoxTemplateSnippetNode, element: PsiElement): Boolean {
        // NOTE 3.0.1 clarify: ignore case when matching (#385)
        val snippetExpression = snippet.configExpression
        val configGroup = snippet.configGroup
        val project = configGroup.project
        val snippetText = snippet.text
        return when (snippetExpression.type) {
            CwtDataTypes.Constant -> {
                val text = snippetExpression.expressionString
                snippetText.equals(text, true)
            }
            CwtDataTypes.Definition -> {
                // 遍历已索引的定义
                val typeExpression = snippetExpression.metadata.value ?: return false
                ProgressManager.checkCanceled()
                val processor = ProcessorFactory.any<String> { snippetText.equals(it, true) }
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive().distinct()
                ParadoxDefinitionSearch.searchElement(null, typeExpression, selector).processAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name
                    if (name.isNullOrEmpty()) return@p true
                    processor.process(name)
                }
                processor.result
            }
            CwtDataTypes.EnumValue -> {
                val enumName = snippetExpression.metadata.value ?: return false
                // 遍历预定义的简单枚举值
                run {
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val r = enumConfig.values.contains(snippetText) // `enumConfig.values` is a case-insensitive set
                    if (r) return true
                }
                // 遍历已索引的复杂枚举值
                run {
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    ProgressManager.checkCanceled()
                    val processor = ProcessorFactory.any<String> { snippetText.equals(it, true) }
                    val searchScopeType = complexEnumConfig.searchScopeType
                    val selector = ParadoxComplexEnumValueSearch.selector(project, element).contextSensitive().distinct()
                        .withSearchScopeType(searchScopeType)
                    ParadoxComplexEnumValueSearch.search(null, enumName, selector).processAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        val name = info.name
                        processor.process(name)
                    }
                    val r = processor.result
                    if (r) return true
                }
                false
            }
            CwtDataTypes.Value -> true // anything
            else -> false // unexpected
        }
    }
}

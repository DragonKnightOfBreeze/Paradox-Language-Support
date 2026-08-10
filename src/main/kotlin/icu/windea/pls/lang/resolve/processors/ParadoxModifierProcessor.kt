package icu.windea.pls.lang.resolve.processors

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxEconomicCategoryManager
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxModifierProcessor {
    fun processPredefinedModifierConfig(configGroup: CwtConfigGroup, processor: Processor<CwtModifierConfig>): Boolean {
        val modifiers = configGroup.predefinedModifiers
        if (modifiers.isEmpty()) return true
        for (modifierConfig in modifiers.values) {
            val r = processor.process(modifierConfig)
            if (!r) return false
        }
        return true
    }

    fun processGeneratedModifierConfig(configGroup: CwtConfigGroup, processor: Processor<CwtModifierConfig>): Boolean {
        val modifiers = configGroup.generatedModifiers
        if (modifiers.isEmpty()) return true
        for (modifierConfig in modifiers.values) {
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
        // 注意：这里仅支持部分数据类型
        ProgressManager.checkCanceled()
        val project = configGroup.project
        if (index == templateExpression.snippetExpressions.size) {
            if (builder.isNotEmpty()) {
                return processor.process(builder)
            }
            return true
        }
        val snippetExpression = templateExpression.snippetExpressions[index]
        when (snippetExpression.type) {
            CwtDataTypes.Constant -> {
                val text = snippetExpression.expressionString
                return processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + text, processor)
            }
            CwtDataTypes.Definition -> {
                val typeExpression = snippetExpression.metadata.value ?: return true
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive().distinct()
                return ParadoxDefinitionSearch.searchElement(null, typeExpression, selector).processAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name
                    if (name.isNullOrEmpty()) return@p true
                    processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + name, processor)
                    true
                }
            }
            CwtDataTypes.EnumValue -> {
                val enumName = snippetExpression.metadata.value ?: return true
                // 遍历简单枚举值
                run {
                    val enumConfig = configGroup.enums[enumName] ?: return@run
                    val enumValueConfigs = enumConfig.valueConfigMap.values
                    if (enumValueConfigs.isEmpty()) return@run
                    ProgressManager.checkCanceled()
                    for (enumValueConfig in enumValueConfigs) {
                        val name = enumValueConfig.value
                        val r = processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + name, processor)
                        if (!r) return false
                    }
                }
                // 遍历复杂枚举值
                run {
                    val complexEnumConfig = configGroup.complexEnums[enumName] ?: return@run
                    ProgressManager.checkCanceled()
                    val searchScopeType = complexEnumConfig.searchScopeType
                    val selector = ParadoxComplexEnumValueSearch.selector(project, element).contextSensitive().distinct()
                        .withSearchScopeType(searchScopeType)
                    val r = ParadoxComplexEnumValueSearch.search(null, enumName, selector).processAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        val name = info.name
                        processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + name, processor)
                        true
                    }
                    if (!r) return false
                }
                return true
            }
            CwtDataTypes.Value -> {
                val dynamicValueType = snippetExpression.metadata.value ?: return true
                // 提示预定义的动态值
                run {
                    val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return@run
                    val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
                    if (dynamicValueTypeConfigs.isEmpty()) return@run
                    ProgressManager.checkCanceled()
                    for (dynamicValueTypeConfig in dynamicValueTypeConfigs) {
                        val name = dynamicValueTypeConfig.value
                        val r = processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + name, processor)
                        if (!r) return false
                    }
                }
                // 提示已索引的动态值
                run {
                    ProgressManager.checkCanceled()
                    val selector = ParadoxDynamicValueSearch.selector(project, element).distinct()
                    val r = ParadoxDynamicValueSearch.search(null, dynamicValueType, selector).processAsync p@{ info ->
                        ProgressManager.checkCanceled()
                        // 去除后面的作用域信息
                        processModifierTemplateRecursively(element, configGroup, templateExpression, index + 1, builder + info.name, processor)
                        true
                    }
                    if (!r) return false
                }
                return true
            }
            else -> return true
        }
    }

    fun processEconomicCategoryInfo(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxEconomicCategoryInfo>): Boolean {
        val selector = ParadoxDefinitionSearch.selector(configGroup.project, element).contextSensitive().distinct()
        return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).processAsync p@{ economicCategory ->
            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            processor.process(economicCategoryInfo)
        }
    }
}

package icu.windea.pls.lang.util

import com.google.common.cache.*
import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

object ParadoxModifierManager {
    object Keys : KeyRegistry() {
        val modifierNameKeys by createKey<Set<String>>(this)
        val modifierDescKeys by createKey<Set<String>>(this)
        val modifierIconPaths by createKey<Set<String>>(this)
    }

    //可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
    //不同的游戏类型存在一些通过不同逻辑生成的修正
    //插件使用的modifiers.cwt中应当去除生成的修正

    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        return ParadoxModifierSupport.matchModifier(name, element, configGroup)
    }

    fun resolveModifier(element: ParadoxScriptStringExpressionElement): ParadoxModifierElement? {
        val name = element.value
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return resolveModifier(name, element, configGroup)
    }

    fun resolveModifier(name: String, element: PsiElement): ParadoxModifierElement? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return resolveModifier(name, element, configGroup)
    }

    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        val modifierInfo = getModifierInfo(name, element, configGroup, useSupport)
        return modifierInfo?.toPsiElement(element)
    }

    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement!!
        if (element !is ParadoxScriptStringExpressionElement) return

        val modifierNames = mutableSetOf<String>()
        ParadoxModifierSupport.completeModifier(context, result, modifierNames)
    }

    fun completeTemplateModifier(contextElement: PsiElement, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>) {
        doCompleteTemplateModifier(contextElement, templateExpression, configGroup, processor, 0, "")
    }

    private fun doCompleteTemplateModifier(contextElement: PsiElement, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>, index: Int, builder: String) {
        //用于提示生成的修正，为了优化性能，这里仅支持部分数据类型
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
                doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + text)
            }
            CwtDataTypes.Definition -> {
                val typeExpression = snippetExpression.value ?: return
                val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
                ParadoxDefinitionSearch.search(typeExpression, selector).processQueryAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val name = definition.definitionInfo?.name ?: return@p true
                    doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
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
                        doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                    }
                }
                ProgressManager.checkCanceled()
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
                        doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
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
                    doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + name)
                }
                ProgressManager.checkCanceled()
                val selector = selector(project, contextElement).dynamicValue().distinctByName()
                ParadoxDynamicValueSearch.search(dynamicValueType, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    //去除后面的作用域信息
                    doCompleteTemplateModifier(contextElement, configExpression, configGroup, processor, index + 1, builder + info.name)
                    true
                }
            }
            else -> pass()
        }
    }

    fun getModifierInfo(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierInfo? {
        val rootFile = selectRootFile(element) ?: return null
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name
        val modifierInfo = cache.get(cacheKey) {
            //进行代码补全时，可能需要使用指定的扩展点解析修正
            useSupport?.resolveModifier(name, element, configGroup)?.also { it.support = useSupport }
                ?: ParadoxModifierSupport.resolveModifier(name, element, configGroup)
                ?: ParadoxModifierInfo.EMPTY
        }
        if (modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }

    fun getModifierInfo(name: String, element: PsiElement): ParadoxModifierInfo? {
        val gameType = selectGameType(element) ?: return null
        val rootFile = selectRootFile(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name
        val modifierInfo = cache.get(cacheKey) {
            ParadoxModifierSupport.resolveModifier(name, element, configGroup) ?: ParadoxModifierInfo.EMPTY
        }
        if (modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }

    fun getModifierInfo(modifierElement: ParadoxModifierElement): ParadoxModifierInfo? {
        val gameType = modifierElement.gameType
        val rootFile = selectRootFile(modifierElement) ?: return null
        val project = modifierElement.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = modifierElement.name
        val modifierInfo = cache.get(cacheKey) {
            modifierElement.toInfo()
        }
        return modifierInfo
    }

    fun getModifierNameKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierNameKeys) {
            ParadoxModifierNameDescProvider.getModifierNameKeys(element, modifierInfo)
        }
    }

    fun getModifierDescKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierDescKeys) {
            ParadoxModifierNameDescProvider.getModifierDescKeys(element, modifierInfo)
        }
    }

    fun getModifierIconPaths(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierIconPaths) {
            ParadoxModifierIconProvider.getModifierIconPaths(element, modifierInfo)
        }
    }

    fun getModifierLocalizedNames(name: String, element: PsiElement, project: Project): Set<String> {
        ProgressManager.checkCanceled()
        val keys = getModifierNameKeys(name, element)
        return keys.firstNotNullOfOrNull { key ->
            val selector = selector(project, element).localisation()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
            val localizedNames = mutableSetOf<String>()
            ParadoxLocalisationSearch.search(key, selector).processQueryAsync { localisation ->
                ProgressManager.checkCanceled()
                val r = ParadoxLocalisationTextRenderer().render(localisation).orNull()
                if (r != null) localizedNames.add(r)
                true
            }
            localizedNames.orNull()
        }.orEmpty()
    }

    fun resolveModifierCategory(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        val finalValue = value ?: "economic_unit" //default to economic_unit
        val enumConfig = configGroup.enums.getValue("scripted_modifier_category")
        var keys = getModifierCategoryOptionValues(enumConfig, finalValue)
        if (keys == null) keys = getModifierCategoryOptionValues(enumConfig, "economic_unit")
        if (keys == null) keys = emptySet() //unexpected
        val modifierCategories = configGroup.modifierCategories
        return keys.associateWith { modifierCategories.getValue(it) }
    }

    private fun getModifierCategoryOptionValues(enumConfig: CwtEnumConfig, finalValue: String): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap.getValue(finalValue)
        return valueConfig.getOrPutUserData(ParadoxEconomicCategoryManager.Keys.modifierCategories, emptySet()) {
            valueConfig.findOption("modifier_categories")?.getOptionValues()
        }
    }
}

//rootFile -> cacheKey -> modifierInfo
//depends on config group
private val CwtConfigGroup.modifierInfoCache by createKey(CwtConfigContext.Keys) {
    createNestedCache<VirtualFile, _, _, _> {
        CacheBuilder.newBuilder().buildCache<String, ParadoxModifierInfo>().trackedBy { it.modificationTracker }
    }
}

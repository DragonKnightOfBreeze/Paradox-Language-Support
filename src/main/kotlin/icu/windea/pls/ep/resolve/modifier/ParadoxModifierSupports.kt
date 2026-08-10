package icu.windea.pls.ep.resolve.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionLookupProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.processors.ParadoxModifierProcessor
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.text.appendPsiLink
import icu.windea.pls.lang.text.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.util.ParadoxEconomicCategoryManager
import icu.windea.pls.lang.util.ParadoxModificationTrackers
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.economicCategoryInfo
import icu.windea.pls.model.economicCategoryModifierInfo
import icu.windea.pls.model.modifierConfig
import icu.windea.pls.model.templateExpression
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 提供对预定义的修正的支持。
 *
 * 示例：`pop_happiness`（来自 `modifiers.cwt`）
 */
class ParadoxPredefinedModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.predefinedModifiers[modifierName] != null
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val modifierConfig = configGroup.predefinedModifiers[modifierName] ?: return null
        val modifierInfo = ParadoxModifierInfo(modifierName, configGroup.project, configGroup.gameType)
        modifierInfo.modifierConfig = modifierConfig
        return modifierInfo
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processPredefinedModifierConfig(configGroup) p@{ modifierConfig ->
            // 排除重复的
            if (!modifierNames.add(modifierConfig.name)) return@p true

            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, modifierConfig.config, withConfigExpression = false)
            val template = modifierConfig.template
            if (template.expressionString.isNotEmpty()) return@p true
            val typeFile = modifierConfig.pointer.containingFile
            val typeText = typeFile?.name
            val typeIcon = typeFile?.icon
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p true
            val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
            lookupElement.addToResult(context, result)
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processPredefinedModifierConfig(configGroup) p@{ modifierConfig ->
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p true
            processor.process(modifierElement)
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        return modifierElement.modifierConfig?.categoryConfigMap
    }
}

/**
 * 提供对从模板表达式生成的修正的支持。
 *
 * 示例：`job_researcher_add`（来自 `modifiers.cwt` 中的 `job_<job>_add`）
 */
class ParadoxTemplateModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        var matched = false
        ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            val templateExpression = ParadoxTemplateExpression.resolve(modifierName, null, configGroup, modifierConfig)
            if (templateExpression == null) return@p true
            matched = true
            false
        }
        return matched
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        // NOTE 2.1.8 如果存在多个非精确匹配的候选项，需要检查是否精确匹配，或者回退为第一个
        val modifierName = name
        val gameType = configGroup.gameType
        val project = configGroup.project
        val modifierInfoCandidates = mutableListOf<ParadoxModifierInfo>()
        for (modifierConfig in configGroup.generatedModifiers.values) {
            ProgressManager.checkCanceled()
            val templateExpression = ParadoxTemplateExpression.resolve(modifierName, null, configGroup, modifierConfig) ?: continue
            val modifierInfo = ParadoxModifierInfo(modifierName, project, gameType)
            modifierInfo.modifierConfig = modifierConfig
            modifierInfo.templateExpression = templateExpression
            if (templateExpression.isExactMatched()) return modifierInfo
            modifierInfoCandidates += modifierInfo
        }
        if (modifierInfoCandidates.isEmpty()) return null
        return modifierInfoCandidates.singleOrNull()
            ?: modifierInfoCandidates.find { it.templateExpression!!.checkExactMatched(element) }
            ?: modifierInfoCandidates.firstOrNull()
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val typeFile = modifierConfig.pointer.containingFile
            val typeText = typeFile?.name
            val typeIcon = typeFile?.icon
            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, modifierConfig.config, withConfigExpression = true)
            // 生成的 modifier
            ParadoxModifierProcessor.processModifierTemplate(element, configGroup, modifierConfig.template) p1@{ name ->
                // 排除重复的
                if (!modifierNames.add(modifierConfig.name)) return@p1 true

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
                lookupElement.addToResult(context, result)
            }
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            ParadoxModifierProcessor.processModifierTemplate(element, configGroup, modifierConfig.template) p1@{ name ->
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                processor.process(modifierElement)
            }
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        // TODO 可以进一步缩小范围
        return ParadoxModificationTrackers.scriptFileFromFilePathPatterns("**/*.txt")
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        return modifierElement.modifierConfig?.categoryConfigMap
    }

    override fun buildDocumentationDefinition(modifierElement: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifierConfig = modifierElement.modifierConfig ?: return false
        val templateExpression = modifierElement.templateExpression ?: return false

        // 加上名字
        val configGroup = modifierConfig.configGroup
        val name = modifierElement.name
        append(ChronicleStrings.modifierPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        // 加上模板信息
        val templateConfigExpression = modifierConfig.template
        if (templateConfigExpression.expressionString.isNotEmpty()) {
            val gameType = modifierElement.gameType
            val templateString = templateConfigExpression.expressionString

            appendBr().appendIndent()
            append(ChronicleBundle.message("doc.text.fromTemplate")).append(" ")
            val templateLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.modifiers, templateString, gameType)
            appendPsiLinkOrUnresolved(templateLink.escapeXml(), templateString.escapeXml())

            // 加上生成源信息
            val snippetNodes = templateExpression.nodes.filterIsInstance<ParadoxTemplateSnippetNode>()
            if (snippetNodes.isNotEmpty()) {
                for (snippetNode in snippetNodes) {
                    ProgressManager.checkCanceled()
                    appendBr().appendIndent()
                    val configExpression = snippetNode.configExpression
                    when (configExpression.type) {
                        CwtDataTypes.Definition -> {
                            val definitionName = snippetNode.text
                            val definitionType = configExpression.metadata.value!!
                            val definitionTypes = definitionType.split('.')
                            append(ChronicleBundle.message("doc.text.generatedFromDefinition"))
                            append(" ")
                            val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType, gameType)
                            appendPsiLinkOrUnresolved(link.escapeXml(), definitionName.escapeXml(), context = modifierElement)
                            append(": ")

                            val type = definitionTypes.first()
                            val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.types, type, gameType)
                            appendPsiLinkOrUnresolved(typeLink.escapeXml(), type.escapeXml())
                            for ((index, t) in definitionTypes.withIndex()) {
                                if (index == 0) continue
                                append(", ")
                                val subtypeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.types, "$type/$t", gameType)
                                appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), t.escapeXml())
                            }
                        }
                        CwtDataTypes.EnumValue -> {
                            val enumValueName = snippetNode.text
                            val enumName = configExpression.metadata.value!!
                            append(ChronicleBundle.message("doc.text.generatedFromEnumValue"))
                            append(" ")
                            if (configGroup.enums.containsKey(enumName)) {
                                val link = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.enums, "$enumName/$enumValueName", gameType)
                                appendPsiLinkOrUnresolved(link.escapeXml(), enumName.escapeXml(), context = modifierElement)
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.enums, enumName, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = modifierElement)
                            } else if (configGroup.complexEnums.containsKey(enumName)) {
                                append(enumValueName.escapeXml())
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.complexEnums, enumName, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = modifierElement)
                            } else {
                                // unexpected
                                append(enumValueName.escapeXml())
                                append(": ")
                                append(enumName.escapeXml())
                            }
                        }
                        CwtDataTypes.Value -> {
                            val dynamicValueType = snippetNode.text
                            val valueName = configExpression.metadata.value!!
                            append(ChronicleBundle.message("doc.text.generatedFromDynamicValue"))
                            if (configGroup.dynamicValueTypes.containsKey(valueName)) {
                                val link = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.values, "$dynamicValueType/$valueName", gameType)
                                appendPsiLinkOrUnresolved(link.escapeXml(), valueName.escapeXml(), context = modifierElement)
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.values, dynamicValueType, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), valueName.escapeXml(), context = modifierElement)
                            } else {
                                append(valueName.escapeXml())
                                append(": ")
                                append(dynamicValueType.escapeXml())
                            }
                        }
                        else -> pass()
                    }
                }
            }
        }

        return true
    }

    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifiers = definitionInfo.modifiers
        if (modifiers.isEmpty()) return false
        val gameType = definitionInfo.gameType
        for (modifier in modifiers) {
            ProgressManager.checkCanceled()
            appendBr()
            append(ChronicleStrings.generatedModifierPrefix).append(" ")
            val link = ReferenceLinkType.Modifier.createLink(modifier.name, gameType)
            appendPsiLink(link.escapeXml(), modifier.name.escapeXml())
            // 2.1.8 文本可能过长，因此这里目前改为不显示
            // append(" ")
            // grayed {
            //     append(ChronicleBundle.message("fromTemplate"))
            //     append(" ")
            //     val key = modifier.config.name
            //     val templateLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.modifiers, key, gameType)
            //     appendPsiLinkOrUnresolved(templateLink.escapeXml(), key.escapeXml())
            // }
        }
        return true
    }
}

/**
 * 提供对从经济分类（`economic_category`）生成的修正的支持。
 *
 * 示例：`country_base_energy_produces_add`（来自经济分类 `country_base`）
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierSupport : ParadoxModifierSupport {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        var matched = false
        ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                if (!economicCategoryModifierInfo.name.equals(modifierName, ignoreCase = true)) return@p1 true
                matched = true
                false
            }
        }
        return matched
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        var result: ParadoxModifierInfo? = null
        ParadoxModifierProcessor.processOrderedEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                if (!economicCategoryModifierInfo.name.equals(modifierName, ignoreCase = true)) return@p1 true
                val modifierInfo = ParadoxModifierInfo(modifierName, configGroup.project, configGroup.gameType)
                modifierInfo.economicCategoryInfo = economicCategoryInfo
                modifierInfo.economicCategoryModifierInfo = economicCategoryModifierInfo
                result = modifierInfo
                false
            }
        }
        return result
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            // 排除不匹配 modifier 的 supported_scopes 的情况
            val modifierCategories = ParadoxModifierManager.resolveModifierCategory(economicCategoryInfo.modifierCategory, configGroup)
            val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val typeText = economicCategoryInfo.name
            val typeIcon = ChronicleIcons.Nodes.Definition(ParadoxDefinitionTypes.economicCategory)
            val hintText = " from economic category " + economicCategoryInfo.name
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                val name = economicCategoryModifierInfo.name
                if (!modifierNames.add(name)) return@p1 true // 排除重复的
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
                lookupElement.addToResult(context, result)
            }
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                val name = economicCategoryModifierInfo.name
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                processor.process(modifierElement)
            }
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ParadoxModificationTrackers.scriptFileFromFilePathPatterns("common/economic_categories/**/*.txt")
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        val economicCategoryInfo = modifierElement.economicCategoryInfo ?: return null
        val modifierCategory = economicCategoryInfo.modifierCategory // may be null
        val configGroup = ChronicleFacade.getConfigGroup(modifierElement.project, modifierElement.gameType)
        return ParadoxModifierManager.resolveModifierCategory(modifierCategory, configGroup)
    }

    override fun buildDocumentationDefinition(modifierElement: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val economicCategoryInfo = modifierElement.economicCategoryInfo ?: return false
        val modifierInfo = modifierElement.economicCategoryModifierInfo ?: return false
        val gameType = modifierElement.gameType

        // 加上名字
        val name = modifierElement.name.orNull()
        append(ChronicleStrings.modifierPrefix).append(" <b>").append(name?.escapeXml().or.anonymous()).append("</b>")
        // 加上经济分类信息
        appendBr().appendIndent()
        append(ChronicleBundle.message("doc.text.generatedFromEconomicCategory"))
        append(" ")
        val ecLink = ReferenceLinkType.Definition.createLink(economicCategoryInfo.name, ParadoxDefinitionTypes.economicCategory, gameType)
        appendPsiLinkOrUnresolved(ecLink.escapeXml(), economicCategoryInfo.name.escapeXml(), context = modifierElement)
        if (modifierInfo.resource != null) {
            appendBr().appendIndent()
            append(ChronicleBundle.message("doc.text.generatedFromResource"))
            append(" ")
            val resourceLink = ReferenceLinkType.Definition.createLink(modifierInfo.resource, ParadoxDefinitionTypes.resource, gameType)
            appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = modifierElement)
        }

        return true
    }

    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val configGroup = definitionInfo.configGroup
        val project = configGroup.project
        val selector = ParadoxDefinitionSearch.selector(project, definition).contextSensitive()
            .withConstraint(ParadoxDefinitionIndexConstraint.EconomicCategory)
        val economicCategory = ParadoxDefinitionSearch.searchProperty(definitionInfo.name, ParadoxDefinitionTypes.economicCategory, selector).find() ?: return false
        val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return false
        val gameType = definitionInfo.gameType
        for (modifierInfo in economicCategoryInfo.modifiers) {
            ProgressManager.checkCanceled()

            appendBr()
            append(ChronicleStrings.generatedModifierPrefix).append(" ")
            val modifierLink = ReferenceLinkType.Modifier.createLink(modifierInfo.name, gameType)
            appendPsiLink(modifierLink.escapeXml(), modifierInfo.name.escapeXml())
            if (modifierInfo.resource != null) {
                append(" ")
                grayed {
                    append(ChronicleBundle.message("doc.text.fromResource"))
                    append(" ")
                    val resourceLink = ReferenceLinkType.Definition.createLink(modifierInfo.resource, ParadoxDefinitionTypes.resource, gameType)
                    appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = definition)
                }
            }
        }
        return true
    }
}

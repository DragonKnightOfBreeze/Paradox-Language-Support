package icu.windea.pls.ep.resolve.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.withSync
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionUtil
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forExpression
import icu.windea.pls.lang.codeInsight.completion.withModifierLocalizedNamesIfNecessary
import icu.windea.pls.lang.codeInsight.completion.withPatchableIcon
import icu.windea.pls.lang.codeInsight.completion.withPatchableTailText
import icu.windea.pls.lang.codeInsight.completion.withScopeMatched
import icu.windea.pls.lang.match.ParadoxConfigExpressionMatchService
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.text.appendPsiLink
import icu.windea.pls.lang.text.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.util.ParadoxEconomicCategoryManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.ParadoxEconomicCategoryModifierInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// region Extensions

val ParadoxModifierSupport.Keys.templateExpression by registerKey<ParadoxTemplateExpression>(ParadoxModifierSupport.Keys).withSync()
val ParadoxModifierSupport.Keys.economicCategoryInfo by registerKey<ParadoxEconomicCategoryInfo>(ParadoxModifierSupport.Keys).withSync()
val ParadoxModifierSupport.Keys.economicCategoryModifierInfo by registerKey<ParadoxEconomicCategoryModifierInfo>(ParadoxModifierSupport.Keys).withSync()

var ParadoxModifierInfo.templateExpression by ParadoxModifierSupport.Keys.templateExpression
var ParadoxModifierInfo.economicCategoryInfo by ParadoxModifierSupport.Keys.economicCategoryInfo
var ParadoxModifierInfo.economicCategoryModifierInfo by ParadoxModifierSupport.Keys.economicCategoryModifierInfo

var ParadoxModifierLightElement.templateExpression by ParadoxModifierSupport.Keys.templateExpression
var ParadoxModifierLightElement.economicCategoryInfo by ParadoxModifierSupport.Keys.economicCategoryInfo
var ParadoxModifierLightElement.economicCategoryModifierInfo by ParadoxModifierSupport.Keys.economicCategoryModifierInfo

// endregion

/**
 * 提供对预定义的修正的支持。
 */
class ParadoxPredefinedModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.predefinedModifiers[modifierName] != null
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val modifierConfig = configGroup.predefinedModifiers[modifierName] ?: return null
        val gameType = configGroup.gameType
        val project = configGroup.project
        val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
        modifierInfo.modifierConfig = modifierConfig
        return modifierInfo
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.predefinedModifiers
        if (modifiers.isEmpty()) return

        for (modifierConfig in modifiers.values) {
            ProgressManager.checkCanceled()

            // 排除重复的
            if (!modifierNames.add(modifierConfig.name)) continue

            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val tailText = ParadoxCompletionUtil.getPatchableTailText(context, modifierConfig.config, withConfigExpression = false)
            val template = modifierConfig.template
            if (template.expressionString.isNotEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this)
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPatchableIcon(ChronicleIcons.Nodes.Modifier)
                .withPatchableTailText(tailText)
                .withScopeMatched(scopeMatched)
                .withModifierLocalizedNamesIfNecessary(name, element)
                .forExpression(context)
            result.addElement(lookupElement, context)
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
 * 提供对从模板表达式生成的修正的支持。（如：`job_<job>_add` -> `job_researcher_add`）
 */
class ParadoxTemplateModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.generatedModifiers.values.any { config ->
            ParadoxConfigExpressionMatchService.matchesTemplate(element, configGroup, modifierName, config.template)
        }
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
            val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
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

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.generatedModifiers
        if (modifiers.isEmpty()) return

        for (modifierConfig in modifiers.values) {
            ProgressManager.checkCanceled()

            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val tailText = ParadoxCompletionUtil.getPatchableTailText(context, modifierConfig.config, withConfigExpression = true)
            val template = modifierConfig.template
            if (template.expressionString.isEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            // 生成的 modifier
            ParadoxModifierManager.completeTemplateModifier(element, template, configGroup) p@{ name ->
                // 排除重复的
                if (!modifierNames.add(name)) return@p true

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this)
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withPatchableIcon(ChronicleIcons.Nodes.Modifier)
                    .withPatchableTailText(tailText)
                    .withScopeMatched(scopeMatched)
                    .withModifierLocalizedNamesIfNecessary(name, element)
                    .forExpression(context)
                result.addElement(lookupElement, context)
                true
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
            append(ChronicleBundle.message("fromTemplate")).append(" ")
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
                            val definitionType = configExpression.value!!
                            val definitionTypes = definitionType.split('.')
                            append(ChronicleBundle.message("generatedFromDefinition"))
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
                            val enumName = configExpression.value!!
                            append(ChronicleBundle.message("generatedFromEnumValue"))
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
                            val valueName = configExpression.value!!
                            append(ChronicleBundle.message("generatedFromDynamicValue"))
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
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        val project = configGroup.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive().distinct()
        val economicCategories = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).findAll()
        for (economicCategory in economicCategories) {
            ProgressManager.checkCanceled()

            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: continue
            for (economicCategoryModifierInfo in economicCategoryInfo.modifiers) {
                if (economicCategoryModifierInfo.name == modifierName) return true
            }
        }
        return false
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val gameType = configGroup.gameType
        val project = configGroup.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive().distinct()
        val economicCategories = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).findAll()
        for (economicCategory in economicCategories) {
            ProgressManager.checkCanceled()

            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: continue
            for (economicCategoryModifierInfo in economicCategoryInfo.modifiers) {
                if (economicCategoryModifierInfo.name == modifierName) {
                    val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
                    modifierInfo.economicCategoryInfo = economicCategoryInfo
                    modifierInfo.economicCategoryModifierInfo = economicCategoryModifierInfo
                    return modifierInfo
                }
            }
        }
        return null
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return

        val selector = ParadoxDefinitionSearch.selector(configGroup.project, element).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.economicCategory, selector).processAsync p@{ economicCategory ->
            ProgressManager.checkCanceled()

            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            // 排除不匹配 modifier 的 supported_scopes 的情况
            val modifierCategories = ParadoxModifierManager.resolveModifierCategory(economicCategoryInfo.modifierCategory, configGroup)
            val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return@p true

            val tailText = " from economic category " + economicCategoryInfo.name
            val typeText = economicCategoryInfo.name
            val typeIcon = ChronicleIcons.Nodes.Definition(ParadoxDefinitionTypes.economicCategory)
            for (economicCategoryModifierInfo in economicCategoryInfo.modifiers) {
                val name = economicCategoryModifierInfo.name
                // 排除重复的
                if (!modifierNames.add(name)) continue

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this)
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                    .withTypeText(typeText, typeIcon, true)
                    .withPatchableIcon(ChronicleIcons.Nodes.Modifier)
                    .withPatchableTailText(tailText)
                    .withModifierLocalizedNamesIfNecessary(name, element)
                    .forExpression(context)
                result.addElement(lookupElement, context)
            }
            true
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
        append(ChronicleBundle.message("generatedFromEconomicCategory"))
        append(" ")
        val ecLink = ReferenceLinkType.Definition.createLink(economicCategoryInfo.name, ParadoxDefinitionTypes.economicCategory, gameType)
        appendPsiLinkOrUnresolved(ecLink.escapeXml(), economicCategoryInfo.name.escapeXml(), context = modifierElement)
        if (modifierInfo.resource != null) {
            appendBr().appendIndent()
            append(ChronicleBundle.message("generatedFromResource"))
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
                    append(ChronicleBundle.message("fromResource"))
                    append(" ")
                    val resourceLink = ReferenceLinkType.Definition.createLink(modifierInfo.resource, ParadoxDefinitionTypes.resource, gameType)
                    appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = definition)
                }
            }
        }
        return true
    }
}

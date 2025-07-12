package icu.windea.pls.ep.modifier

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.modifier.ParadoxModifierSupport.Keys.synced
import icu.windea.pls.ep.reference.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

//region Extensions

val ParadoxModifierSupport.Keys.templateExpression by createKey<ParadoxTemplateExpression>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.economicCategoryInfo by createKey<ParadoxEconomicCategoryInfo>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.economicCategoryModifierInfo by createKey<ParadoxEconomicCategoryInfo.ModifierInfo>(ParadoxModifierSupport.Keys).synced()

var ParadoxModifierInfo.templateExpression by ParadoxModifierSupport.Keys.templateExpression
var ParadoxModifierInfo.economicCategoryInfo by ParadoxModifierSupport.Keys.economicCategoryInfo
var ParadoxModifierInfo.economicCategoryModifierInfo by ParadoxModifierSupport.Keys.economicCategoryModifierInfo

var ParadoxModifierElement.templateExpression by ParadoxModifierSupport.Keys.templateExpression
var ParadoxModifierElement.economicCategoryInfo by ParadoxModifierSupport.Keys.economicCategoryInfo
var ParadoxModifierElement.economicCategoryModifierInfo by ParadoxModifierSupport.Keys.economicCategoryModifierInfo

//endregion

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
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
        modifierInfo.modifierConfig = modifierConfig
        return modifierInfo
    }

    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement!!
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.predefinedModifiers
        if (modifiers.isEmpty()) return

        for (modifierConfig in modifiers.values) {
            //排除重复的
            if (!modifierNames.add(modifierConfig.name)) continue

            //排除不匹配modifier的supported_scopes的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val tailText = ParadoxCompletionManager.getExpressionTailText(context, modifierConfig.config, withConfigExpression = false)
            val template = modifierConfig.template
            if (template.expressionString.isNotEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this@ParadoxPredefinedModifierSupport)
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPatchableIcon(PlsIcons.Nodes.Modifier)
                .withPatchableTailText(tailText)
                .withScopeMatched(scopeMatched)
                .withModifierLocalizedNamesIfNecessary(name, element)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return modifierElement.modifierConfig?.categoryConfigMap
    }
}

/**
 * 提供对通过模版表达式生成的修正的支持。（如：`job_<job>_add` -> `job_researcher_add`）
 */
class ParadoxTemplateModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.generatedModifiers.values.any { config ->
            CwtTemplateExpressionManager.matches(element, modifierName, config.template, configGroup)
        }
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        var modifierConfig: CwtModifierConfig? = null
        val templateExpression = configGroup.generatedModifiers.values.firstNotNullOfOrNull { config ->
            ProgressManager.checkCanceled()
            val templateExpression = ParadoxTemplateExpression.resolve(modifierName, TextRange.from(0, modifierName.length), configGroup, config)
            if (templateExpression != null) modifierConfig = config
            templateExpression
        }
        if (modifierConfig == null) return null
        val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
        modifierInfo.modifierConfig = modifierConfig
        modifierInfo.templateExpression = templateExpression
        return modifierInfo
    }

    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement!!
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.generatedModifiers
        if (modifiers.isEmpty()) return

        for (modifierConfig in modifiers.values) {
            ProgressManager.checkCanceled()

            //排除不匹配modifier的supported_scopes的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) continue

            val tailText = ParadoxCompletionManager.getExpressionTailText(context, modifierConfig.config, withConfigExpression = true)
            val template = modifierConfig.template
            if (template.expressionString.isEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            //生成的modifier
            ParadoxModifierManager.completeTemplateModifier(element, template, configGroup) p@{ name ->
                //排除重复的
                if (!modifierNames.add(name)) return@p true

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this@ParadoxTemplateModifierSupport)
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withPatchableIcon(PlsIcons.Nodes.Modifier)
                    .withPatchableTailText(tailText)
                    .withScopeMatched(scopeMatched)
                    .withModifierLocalizedNamesIfNecessary(name, element)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
                true
            }
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        //TODO 可以进一步缩小范围
        return ParadoxModificationTrackers.ScriptFileTracker("**/*.txt")
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return modifierElement.modifierConfig?.categoryConfigMap
    }

    override fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifierConfig = modifierElement.modifierConfig ?: return false
        val templateExpression = modifierElement.templateExpression ?: return false

        //加上名字
        val configGroup = modifierConfig.configGroup
        val name = modifierElement.name
        append(PlsStringConstants.modifierPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上模版信息
        val templateConfigExpression = modifierConfig.template
        if (templateConfigExpression.expressionString.isNotEmpty()) {
            val gameType = modifierElement.gameType
            val templateString = templateConfigExpression.expressionString

            appendBr().appendIndent()
            append(PlsBundle.message("byTemplate")).append(" ")
            val templateLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "modifiers", templateString)
            appendPsiLinkOrUnresolved(templateLink.escapeXml(), templateString.escapeXml())

            //加上生成源信息
            val snippetNodes = templateExpression.nodes.filterIsInstance<ParadoxTemplateSnippetNode>()
            if (snippetNodes.isNotEmpty()) {
                for (snippetNode in snippetNodes) {
                    appendBr().appendIndent()
                    val configExpression = snippetNode.configExpression
                    when (configExpression.type) {
                        CwtDataTypes.Definition -> {
                            val definitionName = snippetNode.text
                            val definitionType = configExpression.value!!
                            val definitionTypes = definitionType.split('.')
                            append(PlsBundle.message("generatedFromDefinition"))
                            append(" ")
                            val link = ParadoxReferenceLinkType.Definition.createLink(gameType, definitionName, definitionType)
                            appendPsiLinkOrUnresolved(link.escapeXml(), definitionName.escapeXml(), context = modifierElement)
                            append(": ")

                            val type = definitionTypes.first()
                            val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", type)
                            appendPsiLinkOrUnresolved(typeLink.escapeXml(), type.escapeXml())
                            for ((index, t) in definitionTypes.withIndex()) {
                                if (index == 0) continue
                                append(", ")
                                val subtypeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", type, t)
                                appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), t.escapeXml())
                            }
                        }
                        CwtDataTypes.EnumValue -> {
                            val enumValueName = snippetNode.text
                            val enumName = configExpression.value!!
                            append(PlsBundle.message("generatedFromEnumValue"))
                            append(" ")
                            if (configGroup.enums.containsKey(enumName)) {
                                val link = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "enums", enumName, enumValueName)
                                appendPsiLinkOrUnresolved(link.escapeXml(), enumName.escapeXml(), context = modifierElement)
                                append(": ")
                                val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "enums", enumName)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = modifierElement)
                            } else if (configGroup.complexEnums.containsKey(enumName)) {
                                append(enumValueName.escapeXml())
                                append(": ")
                                val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "complex_enums", enumName)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = modifierElement)
                            } else {
                                //unexpected
                                append(enumValueName.escapeXml())
                                append(": ")
                                append(enumName.escapeXml())
                            }
                        }
                        CwtDataTypes.Value -> {
                            val dynamicValueType = snippetNode.text
                            val valueName = configExpression.value!!
                            append(PlsBundle.message("generatedFromDynamicValue"))
                            if (configGroup.dynamicValueTypes.containsKey(valueName)) {
                                val link = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "values", dynamicValueType, valueName)
                                appendPsiLinkOrUnresolved(link.escapeXml(), valueName.escapeXml(), context = modifierElement)
                                append(": ")
                                val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "values", dynamicValueType)
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

    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifiers = definitionInfo.modifiers
        if (modifiers.isEmpty()) return false
        for (modifier in modifiers) {
            appendBr()
            append(PlsStringConstants.generatedModifierPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Modifier.createLink(modifier.name)
            appendPsiLink(link.escapeXml(), modifier.name.escapeXml())
            grayed {
                append(" ")
                append(PlsBundle.message("byTemplate"))
                append(" ")
                val key = modifier.config.name
                val gameType = definitionInfo.gameType
                val templateLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "modifiers", key)
                appendPsiLinkOrUnresolved(templateLink.escapeXml(), key.escapeXml())
            }
        }
        return true
    }
}

/**
 * 提供对通过经济类型（`economic_category`）生成的修正的支持。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        val project = configGroup.project
        val selector = selector(project, element).definition().contextSensitive().distinctByName()
        val economicCategories = ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.EconomicCategory, selector).findAll()
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
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val selector = selector(project, element).definition().contextSensitive().distinctByName()
        val economicCategories = ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.EconomicCategory, selector).findAll()
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

    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement!!
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        if (element !is ParadoxScriptStringExpressionElement) return

        val selector = selector(configGroup.project, element).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.EconomicCategory, selector).processQueryAsync p@{ economicCategory ->
            ProgressManager.checkCanceled()

            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            //排除不匹配modifier的supported_scopes的情况
            val modifierCategories = ParadoxModifierManager.resolveModifierCategory(economicCategoryInfo.modifierCategory, configGroup)
            val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && PlsFacade.getSettings().completion.completeOnlyScopeIsMatched) return@p true

            val tailText = " from economic category " + economicCategoryInfo.name
            val typeText = economicCategoryInfo.name
            val typeIcon = PlsIcons.Nodes.Definition(ParadoxDefinitionTypes.EconomicCategory)
            for (economicCategoryModifierInfo in economicCategoryInfo.modifiers) {
                val name = economicCategoryModifierInfo.name
                //排除重复的
                if (!modifierNames.add(name)) continue

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this@ParadoxEconomicCategoryModifierSupport)
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(modifierElement)
                    .withTypeText(typeText, typeIcon, true)
                    .withPatchableIcon(PlsIcons.Nodes.Modifier)
                    .withPatchableTailText(tailText)
                    .withModifierLocalizedNamesIfNecessary(name, element)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
            true
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ParadoxModificationTrackers.ScriptFileTracker("common/economic_categories/**/*.txt")
    }

    override fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        val economicCategoryInfo = modifierElement.economicCategoryInfo ?: return null
        val modifierCategory = economicCategoryInfo.modifierCategory //may be null
        val configGroup = PlsFacade.getConfigGroup(modifierElement.project, modifierElement.gameType)
        return ParadoxModifierManager.resolveModifierCategory(modifierCategory, configGroup)
    }

    override fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val gameType = modifierElement.gameType
        val economicCategoryInfo = modifierElement.economicCategoryInfo ?: return false
        val modifierInfo = modifierElement.economicCategoryModifierInfo ?: return false

        //加上名字
        val name = modifierElement.name
        append(PlsStringConstants.modifierPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上经济类型信息
        appendBr().appendIndent()
        append(PlsBundle.message("generatedFromEconomicCategory"))
        append(" ")
        val ecLink = ParadoxReferenceLinkType.Definition.createLink(gameType, economicCategoryInfo.name, ParadoxDefinitionTypes.EconomicCategory)
        appendPsiLinkOrUnresolved(ecLink.escapeXml(), economicCategoryInfo.name.escapeXml(), context = modifierElement)
        if (modifierInfo.resource != null) {
            appendBr().appendIndent()
            append(PlsBundle.message("generatedFromResource"))
            append(" ")
            val resourceLink = ParadoxReferenceLinkType.Definition.createLink(gameType, modifierInfo.resource, ParadoxDefinitionTypes.Resource)
            appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = modifierElement)
        } else {
            appendBr().appendIndent()
            append(PlsBundle.message("forAiBudget"))
        }

        return true
    }

    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val gameType = definitionInfo.gameType
        val configGroup = definitionInfo.configGroup
        val project = configGroup.project
        val selector = selector(project, definition).definition().contextSensitive()
        val economicCategory = ParadoxDefinitionSearch.search(definitionInfo.name, ParadoxDefinitionTypes.EconomicCategory, selector)
            .find()
            ?: return false
        val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return false
        for (modifierInfo in economicCategoryInfo.modifiers) {
            appendBr()
            append(PlsStringConstants.generatedModifierPrefix).append(" ")
            val modifierLink = ParadoxReferenceLinkType.Modifier.createLink(modifierInfo.name)
            appendPsiLink(modifierLink.escapeXml(), modifierInfo.name.escapeXml())
            if (modifierInfo.resource != null) {
                grayed {
                    append(" ")
                    append(PlsBundle.message("fromResource"))
                    append(" ")
                    val resourceLink = ParadoxReferenceLinkType.Definition.createLink(gameType, modifierInfo.resource, ParadoxDefinitionTypes.Resource)
                    appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = definition)
                }
            } else {
                grayed {
                    append(" ")
                    append(PlsBundle.message("forAiBudget"))
                }
            }
        }
        return true
    }
}

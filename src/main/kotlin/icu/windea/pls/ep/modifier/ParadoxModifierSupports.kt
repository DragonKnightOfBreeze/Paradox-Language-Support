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
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.modifier.ParadoxModifierSupport.Keys.synced
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

//region Extensions

val ParadoxModifierSupport.Keys.templateReferences by createKey<List<ParadoxTemplateSnippetExpressionReference>>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.economicCategoryInfo by createKey<ParadoxEconomicCategoryInfo>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.economicCategoryModifierInfo by createKey<ParadoxEconomicCategoryInfo.ModifierInfo>(ParadoxModifierSupport.Keys).synced()

var ParadoxModifierInfo.templateReferences by ParadoxModifierSupport.Keys.templateReferences
var ParadoxModifierInfo.economicCategoryInfo by ParadoxModifierSupport.Keys.economicCategoryInfo
var ParadoxModifierInfo.economicCategoryModifierInfo by ParadoxModifierSupport.Keys.economicCategoryModifierInfo

var ParadoxModifierElement.templateReferences by ParadoxModifierSupport.Keys.templateReferences
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
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

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
            CwtTemplateExpressionManager.matches(modifierName, element, config.template, configGroup)
        }
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        var modifierConfig: CwtModifierConfig? = null
        val templateReferences = configGroup.generatedModifiers.values.firstNotNullOfOrNull { config ->
            ProgressManager.checkCanceled()

            val resolvedReferences = CwtTemplateExpressionManager.resolveReferences(modifierName, config.template, configGroup).orNull()
            if (resolvedReferences != null) modifierConfig = config
            resolvedReferences
        }.orEmpty()
        if (modifierConfig == null) return null
        val modifierInfo = ParadoxModifierInfo(modifierName, gameType, project)
        modifierInfo.modifierConfig = modifierConfig
        modifierInfo.templateReferences = templateReferences
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
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue

            val tailText = ParadoxCompletionManager.getExpressionTailText(context, modifierConfig.config, withConfigExpression = true)
            val template = modifierConfig.template
            if (template.expressionString.isEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            //生成的modifier
            CwtTemplateExpressionManager.processResolveResult(element, template, configGroup) p@{ name ->
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
        val templateReferences = modifierElement.templateReferences ?: return false

        //加上名字
        val configGroup = modifierConfig.configGroup
        val name = modifierElement.name
        append(PlsBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上模版信息
        val templateConfigExpression = modifierConfig.template
        if (templateConfigExpression.expressionString.isNotEmpty()) {
            val gameType = modifierElement.gameType
            val templateString = templateConfigExpression.expressionString

            appendBr().appendIndent()
            append(PlsBundle.message("byTemplate")).append(" ")
            appendCwtConfigLink("${gameType.prefix}modifiers/$templateString", templateString)

            //加上生成源信息
            if (templateReferences.isNotEmpty()) {
                for (reference in templateReferences) {
                    appendBr().appendIndent()
                    val configExpression = reference.configExpression
                    when (configExpression.type) {
                        CwtDataTypes.Definition -> {
                            val definitionName = reference.name
                            val definitionType = configExpression.value!!
                            val definitionTypes = definitionType.split('.')
                            append(PlsBundle.message("generatedFromDefinition"))
                            append(" ")
                            appendDefinitionLink(gameType, definitionName, definitionType, modifierElement)
                            append(": ")

                            val type = definitionTypes.first()
                            val typeLink = "${gameType.prefix}types/${type}"
                            appendCwtConfigLink(typeLink, type)
                            for ((index, t) in definitionTypes.withIndex()) {
                                if (index == 0) continue
                                append(", ")
                                val subtypeLink = "$typeLink/${t}"
                                appendCwtConfigLink(subtypeLink, t)
                            }
                        }
                        CwtDataTypes.EnumValue -> {
                            val enumValueName = reference.name
                            val enumName = configExpression.value!!
                            append(PlsBundle.message("generatedFromEnumValue"))
                            append(" ")
                            if (configGroup.enums.containsKey(enumName)) {
                                appendCwtConfigLink("${gameType.prefix}enums/${enumName}/${enumValueName}", enumName, modifierElement)
                                append(": ")
                                appendCwtConfigLink("${gameType.prefix}enums/${enumName}", enumName, modifierElement)
                            } else if (configGroup.complexEnums.containsKey(enumName)) {
                                append(enumValueName.escapeXml())
                                append(": ")
                                appendCwtConfigLink("${gameType.prefix}complex_enums/${enumName}", enumName, modifierElement)
                            } else {
                                //unexpected
                                append(enumValueName.escapeXml())
                                append(": ")
                                append(enumName.escapeXml())
                            }
                        }
                        CwtDataTypes.Value -> {
                            val dynamicValueType = reference.name
                            val valueName = configExpression.value!!
                            append(PlsBundle.message("generatedFromDynamicValue"))
                            if (configGroup.dynamicValueTypes.containsKey(valueName)) {
                                appendCwtConfigLink("${gameType.prefix}values/${dynamicValueType}/${valueName}", valueName, modifierElement)
                                append(": ")
                                appendCwtConfigLink("${gameType.prefix}values/${dynamicValueType}", valueName, modifierElement)
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
            append(PlsBundle.message("prefix.generatedModifier")).append(" ")
            appendModifierLink(modifier.name)
            grayed {
                append(" ")
                append(PlsBundle.message("byTemplate"))
                append(" ")
                val key = modifier.config.name
                val gameType = definitionInfo.gameType
                appendCwtConfigLink("${gameType.prefix}modifiers/${key}", key)
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
        val economicCategories = ParadoxDefinitionSearch.search("economic_category", selector).findAll()
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
        val economicCategories = ParadoxDefinitionSearch.search("economic_category", selector).findAll()
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
        ParadoxDefinitionSearch.search("economic_category", selector).processQueryAsync p@{ economicCategory ->
            ProgressManager.checkCanceled()

            val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return@p true
            //排除不匹配modifier的supported_scopes的情况
            val modifierCategories = ParadoxEconomicCategoryManager.resolveModifierCategory(economicCategoryInfo.modifierCategory, configGroup)
            val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return@p true

            val tailText = " from economic category " + economicCategoryInfo.name
            val typeText = economicCategoryInfo.name
            val typeIcon = PlsIcons.Nodes.Definition("economic_category")
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
        val configGroup = getConfigGroup(modifierElement.project, modifierElement.gameType)
        return ParadoxEconomicCategoryManager.resolveModifierCategory(modifierCategory, configGroup)
    }

    override fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val gameType = modifierElement.gameType
        val economicCategoryInfo = modifierElement.economicCategoryInfo ?: return false
        val modifierInfo = modifierElement.economicCategoryModifierInfo ?: return false

        //加上名字
        val name = modifierElement.name
        append(PlsBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上经济类型信息
        appendBr().appendIndent()
        append(PlsBundle.message("generatedFromEconomicCategory"))
        append(" ")
        appendDefinitionLink(gameType, economicCategoryInfo.name, "economic_category", modifierElement)
        if (modifierInfo.resource != null) {
            appendBr().appendIndent()
            append(PlsBundle.message("generatedFromResource"))
            append(" ")
            appendDefinitionLink(gameType, modifierInfo.resource, "resource", modifierElement)
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
        val economicCategory = ParadoxDefinitionSearch.search(definitionInfo.name, "economic_category", selector)
            .find()
            ?: return false
        val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return false
        for (modifierInfo in economicCategoryInfo.modifiers) {
            appendBr()
            append(PlsBundle.message("prefix.generatedModifier")).append(" ")
            appendModifierLink(modifierInfo.name)
            if (modifierInfo.resource != null) {
                grayed {
                    append(" ")
                    append(PlsBundle.message("fromResource"))
                    append(" ")
                    appendDefinitionLink(gameType, modifierInfo.resource, "resource", definition)
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

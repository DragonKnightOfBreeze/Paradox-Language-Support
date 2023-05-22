package icu.windea.pls.lang.modifier.impl

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

/**
 * 通过模版表达式生成修正。（如：`job_<job>_add` -> `job_researcher_add`）
 */
class ParadoxTemplateModifierSupport : ParadoxModifierSupport {
    companion object {
        @JvmField
        val referencesKey = Key.create<List<ParadoxTemplateSnippetExpressionReference>>("paradox.modifierElement.references")
    }
    
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.generatedModifiers.values.any { config ->
            config.template.matches(modifierName, element, configGroup)
        }
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        val modifierName = name
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        var modifierConfig: CwtModifierConfig? = null
        val references = configGroup.generatedModifiers.values.firstNotNullOfOrNull { config ->
            ProgressManager.checkCanceled()
            val resolvedReferences = config.template.resolveReferences(modifierName, element, configGroup).takeIfNotEmpty()
            if(resolvedReferences != null) modifierConfig = config
            resolvedReferences
        }.orEmpty()
        if(modifierConfig == null) return null
        val resolved = ParadoxModifierElement(element, modifierName, gameType, project)
        resolved.putUserData(ParadoxModifierHandler.modifierConfigKey, modifierConfig)
        resolved.putUserData(referencesKey, references)
        resolved.putUserData(ParadoxModifierHandler.supportKey, this)
        return resolved
    }
    
    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>): Unit = with(context) {
        val element = contextElement
        if(element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.generatedModifiers
        if(modifiers.isEmpty()) return
        val configGroup = configGroup
        val project = configGroup.project
        for(modifierConfig in modifiers.values) {
            //排除不匹配modifier的supported_scopes的情况
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val tailText = ParadoxConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = true)
            val template = modifierConfig.template
            if(template.isEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            //生成的modifier
            template.processResolveResult(contextElement, configGroup) p@{ name ->
                //排除重复的
                if(!modifierNames.add(name)) return@p true
                
                val modifierElement = ParadoxModifierHandler.resolveModifier(name, element, configGroup, this@ParadoxTemplateModifierSupport)
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(modifierElement, name)
                    .withIcon(PlsIcons.Modifier)
                    .withTailText(tailText)
                    .withTypeText(typeFile?.name)
                    .withTypeIcon(typeFile?.icon)
                    .withScopeMatched(scopeMatched)
                    .letIf(getSettings().completion.completeByLocalizedName) {
                        //如果启用，也基于修正的本地化名字进行代码补全
                        val localizedNames = ParadoxModifierHandler.getModifierLocalizedNames(name, project, element)
                        it.withLocalizedNames(localizedNames)
                    }
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
    
    override fun getModificationTracker(resolved: ParadoxModifierElement): ModificationTracker {
        //TODO 可以进一步缩小范围
        return ParadoxPsiModificationTracker.getInstance(resolved.project).ScriptFileTracker(":txt")
    }
    
    override fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return element.getUserData(ParadoxModifierHandler.modifierConfigKey)?.categoryConfigMap
    }
    
    override fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean = with(builder) {
        val modifierConfig = element.getUserData(ParadoxModifierHandler.modifierConfigKey) ?: return false
        val references = element.getUserData(referencesKey) ?: return false
        
        //加上名字
        val configGroup = modifierConfig.info.configGroup
        val name = element.name
        append(PlsBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上模版信息
        val templateConfigExpression = modifierConfig.template
        if(templateConfigExpression.isNotEmpty()) {
            val gameType = element.gameType
            val templateString = templateConfigExpression.expressionString
            
            appendBr().appendIndent()
            append(PlsBundle.message("byTemplate")).append(" ")
            appendCwtLink("${gameType.linkToken}modifiers/$templateString", templateString)
            
            //加上生成源信息
            if(references.isNotEmpty()) {
                for(reference in references) {
                    appendBr().appendIndent()
                    val configExpression = reference.configExpression
                    when(configExpression.type) {
                        CwtDataType.Definition -> {
                            val definitionName = reference.name
                            val definitionType = configExpression.value!!
                            val definitionTypes = definitionType.split('.')
                            append(PlsBundle.message("generatedFromDefinition"))
                            append(" ")
                            appendDefinitionLink(gameType, definitionName, definitionType, element)
                            append(": ")
                            
                            val type = definitionTypes.first()
                            val typeLink = "${gameType.linkToken}types/${type}"
                            appendCwtLink(typeLink, type)
                            for((index, t) in definitionTypes.withIndex()) {
                                if(index == 0) continue
                                append(", ")
                                val subtypeLink = "$typeLink/${t}"
                                appendCwtLink(subtypeLink, t)
                            }
                        }
                        CwtDataType.EnumValue -> {
                            val enumValueName = reference.name
                            val enumName = configExpression.value!!
                            append(PlsBundle.message("generatedFromEnumValue"))
                            append(" ")
                            if(configGroup.enums.containsKey(enumName)) {
                                appendCwtLink("${gameType.linkToken}enums/${enumName}/${enumValueName}", enumName, element)
                                append(": ")
                                appendCwtLink("${gameType.linkToken}enums/${enumName}", enumName, element)
                            } else if(configGroup.complexEnums.containsKey(enumName)) {
                                append(enumValueName.escapeXml())
                                append(": ")
                                appendCwtLink("${gameType.linkToken}complex_enums/${enumName}", enumName, element)
                            } else {
                                //unexpected
                                append(enumValueName.escapeXml())
                                append(": ")
                                append(enumName.escapeXml())
                            }
                        }
                        CwtDataType.Value -> {
                            val valueSetName = reference.name
                            val valueName = configExpression.value!!
                            append(PlsBundle.message("generatedFromValueSetValue"))
                            if(configGroup.values.containsKey(valueName)) {
                                appendCwtLink("${gameType.linkToken}values/${valueSetName}/${valueName}", valueName, element)
                                append(": ")
                                appendCwtLink("${gameType.linkToken}values/${valueSetName}", valueName, element)
                            } else {
                                append(valueName.escapeXml())
                                append(": ")
                                append(valueSetName.escapeXml())
                            }
                        }
                        else -> pass()
                    }
                }
            }
        }
        
        return true
    }
    
    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean = with(builder) {
        val modifiers = definitionInfo.modifiers
        if(modifiers.isEmpty()) return false
        for(modifier in modifiers) {
            appendBr()
            append(PlsBundle.message("prefix.generatedModifier")).append(" ")
            append(modifier.name)
            grayed {
                append(" ")
                append(PlsBundle.message("byTemplate"))
                append(" ")
                val key = modifier.config.name
                val gameType = definitionInfo.gameType
                appendCwtLink("${gameType.linkToken}modifiers/${key}", key)
            }
        }
        return true
    }
}

package icu.windea.pls.lang.modifier.impl

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

/**
 * 提供对预定义的修正的支持。
 */
class ParadoxPredefinedModifierSupport: ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.predefinedModifiers[modifierName] != null
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        val modifierName = name
        val modifierConfig = configGroup.predefinedModifiers[modifierName]
        if(modifierConfig == null) return null
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val resolved = ParadoxModifierElement(element, modifierName, gameType, project)
        resolved.putUserData(ParadoxModifierHandler.modifierConfigKey, modifierConfig)
        resolved.putUserData(ParadoxModifierHandler.supportKey, this)
        return resolved
    }
    
    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)= with(context) {
        val element = context.contextElement
        if(element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.predefinedModifiers
        if(modifiers.isEmpty()) return
        val configGroup = configGroup
        val project = configGroup.project
        for(modifierConfig in modifiers.values) {
            //排除重复的
            if(!modifierNames.add(modifierConfig.name)) continue
            
            //排除不匹配modifier的supported_scopes的情况
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val tailText = ParadoxConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = false)
            val template = modifierConfig.template
            if(template.isNotEmpty()) continue
            val typeFile = modifierConfig.pointer.containingFile
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierHandler.resolveModifier(name, element, configGroup, this@ParadoxPredefinedModifierSupport)
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
        }
    }
    
    override fun getModificationTracker(resolved: ParadoxModifierElement): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }
    
    override fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return element.getUserData(ParadoxModifierHandler.modifierConfigKey)?.categoryConfigMap
    }
    
    //这里需要返回null，以便尝试适用接下来的扩展点，如果全部无法适用，会使用默认的处理逻辑
    
    override fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder) = false
}
package icu.windea.pls.lang.modifier.impl

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.model.stub.*
import icu.windea.pls.script.psi.*

/**
 * 提供对预定义的修正的支持。
 */
class ParadoxPredefinedModifierSupport: ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.predefinedModifiers[modifierName] != null
    }
    
    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierStub? {
        val modifierName = name
        val modifierConfig = configGroup.predefinedModifiers[modifierName] ?: return null
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val modifierData = ParadoxModifierStub(modifierName, gameType, project)
        modifierData.support = this
        modifierData.modifierConfig = modifierConfig
        return modifierData
    }
    
    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val element = context.contextElement!!
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        if(element !is ParadoxScriptStringExpressionElement) return
        val modifiers = configGroup.predefinedModifiers
        if(modifiers.isEmpty()) return
        
        for(modifierConfig in modifiers.values) {
            //排除重复的
            if(!modifierNames.add(modifierConfig.name)) continue
            
            //排除不匹配modifier的supported_scopes的情况
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val tailText = CwtConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = false)
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
                    val localizedNames = ParadoxModifierHandler.getModifierLocalizedNames(name, element, configGroup.project)
                    it.withLocalizedNames(localizedNames)
                }
            result.addScriptExpressionElement(context, builder)
        }
    }
    
    override fun getModificationTracker(modifierData: ParadoxModifierStub): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }
    
    override fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return modifierElement.modifierConfig?.categoryConfigMap
    }
}
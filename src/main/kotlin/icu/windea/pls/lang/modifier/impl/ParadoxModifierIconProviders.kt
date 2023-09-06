package icu.windea.pls.lang.modifier.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$
        registry += "gfx/interface/icons/modifiers/mod_${name}"
    }
}

class ParadoxDefinitionDelegateBasedModifierIconProvider: ParadoxModifierIconProvider {
    //如果修正M由定义D生成，而定义D的作为图标的图片又委托给了定义D1
    //那么修正M的作为图标的图片也可以委托给定义D1的对应修正
    
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        val modifierData = ParadoxModifierHandler.getModifierData(name, element) ?: return
        val modifierConfig = modifierData.modifierConfig ?: return
        val templateReferences = modifierData.templateReferences ?: return
        val templateReference = templateReferences.singleOrNull()?.takeIf { it.configExpression.type == CwtDataType.Definition } ?: return
        val definitionName = templateReference.name
        val definitionType = templateReference.configExpression.value ?: return
        processDelegateDefinitions(definitionName, definitionType, element, modifierConfig, registry)
    }
    
    private fun processDelegateDefinitions(definitionName: String, definitionType: String, element: PsiElement, modifierConfig: CwtModifierConfig, registry: MutableSet<String>) {
        withRecursionGuard("icu.windea.pls.lang.modifier.impl.ParadoxDefinitionDelegateBasedModifierIconProvider.processDelegateDefinitions") {
            withCheckRecursion(definitionName) {
                val configGroup = modifierConfig.config.info.configGroup
                val selector = definitionSelector(configGroup.project, element).contextSensitive()
                ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync p@{ definition ->
                    ProgressManager.checkCanceled()
                    val definitionInfo = definition.definitionInfo ?: return@p true
                    definitionInfo.primaryImages.forEach f@{ imageConfig ->
                        if(imageConfig.locationExpression.framePropertyNames.isNotNullOrEmpty()) return@f
                        val propertyName = imageConfig.locationExpression.propertyName ?: return@f
                        val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return@f
                        val propertyValue = property.propertyValue ?: return@f
                        val config = ParadoxConfigHandler.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return@f
                        if(config.expression.type != CwtDataType.Definition) return@f //must be a definition reference
                        if(config.expression.value?.substringBefore('.') != definitionInfo.type) return@f //must be same definition type
                        val resolved = ParadoxConfigHandler.resolveScriptExpression(propertyValue, null, config, config.expression, configGroup, false) ?: return@f
                        val resolvedDefinition = resolved.castOrNull<ParadoxScriptDefinitionElement>() ?: return@f
                        val resolvedDefinitionInfo = resolvedDefinition.definitionInfo ?: return@f
                        val resolvedDefinitionName = resolvedDefinitionInfo.name
                        val resolvedDefinitionType = resolvedDefinitionInfo.type
                        val delegateName = modifierConfig.template.extract(resolvedDefinitionName)
                        registry += "gfx/interface/icons/modifiers/mod_${delegateName}"
                        processDelegateDefinitions(resolvedDefinitionName, resolvedDefinitionType, element, modifierConfig, registry)
                    }
                    true
                }
            }
        }
    }
}

class ParadoxEconomicCategoryBasedModifierIconProvider: ParadoxModifierIconProvider {
    //对于由economic_category生成的那些修正，需要应用特定的图标继承逻辑
    
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //TODO
    }
}
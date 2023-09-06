package icu.windea.pls.lang.modifier.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.modifier.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$
        registry += "gfx/interface/icons/modifiers/mod_${name}"
    }
}

class ParadoxDelegateDefinitionBasedModifierIconProvider: ParadoxModifierIconProvider {
    //如果修正M由定义D生成，而定义D的作为图标的图片又委托给了定义D1
    //那么修正M的作为图标的图片也可以委托给定义D1的对应修正
    
    override fun addModifierIconPath(name: String, element: PsiElement, registry: MutableSet<String>) {
        val modifierData = ParadoxModifierHandler.getModifierData(name, element) ?: return
        val templateReferences = modifierData.templateReferences ?: return
        val templateReference = templateReferences.singleOrNull()?.takeIf { it.configExpression.type == CwtDataType.Definition } ?: return
        val configGroup = templateReference.configGroup
        val definitionName = templateReference.name
        val definitionType = templateReference.configExpression.value ?: return
        val selector = definitionSelector(configGroup.project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync p@{ definition ->
            val definitionInfo = definition.definitionInfo ?: return@p true
            definitionInfo.primaryImages
                .filter { it.locationExpression.propertyName != null && it.locationExpression.framePropertyNames.isNullOrEmpty() }
                .find { it.locationExpression.resolve(definition, definitionInfo) }
            true
        }
    }
}
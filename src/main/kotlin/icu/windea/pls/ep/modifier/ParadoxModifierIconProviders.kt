package icu.windea.pls.ep.modifier

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    override fun addModifierIconPath(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$
        registry += "gfx/interface/icons/modifiers/mod_${modifierInfo.name}"
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisJobBasedModifierIconProvider: ParadoxModifierIconProvider {
    //对于由job生成的那些修正，需要应用特殊的图标继承逻辑
    
    override fun addModifierIconPath(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        val modifierConfig = modifierInfo.modifierConfig ?: return
        val templateReferences = modifierInfo.templateReferences ?: return
        val templateReference = templateReferences.singleOrNull()?.takeIf { it.configExpression.type == CwtDataTypes.Definition } ?: return
        val definitionName = templateReference.name
        val definitionType = templateReference.configExpression.value ?: return
        if(definitionType.substringBefore('.') != "job") return
        val configGroup = modifierConfig.config.configGroup
        val selector = definitionSelector(configGroup.project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val property = definition.findProperty("icon", inline = true) ?: return@p true
            val propertyValue = property.propertyValue ?: return@p true
            if(propertyValue !is ParadoxScriptString) return@p true
            val name = modifierConfig.template.extract(propertyValue.value)
            registry += "gfx/interface/icons/modifiers/mod_${name}"
            true
        }
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEconomicCategoryBasedModifierIconProvider: ParadoxModifierIconProvider {
    //对于由economic_category生成的那些修正，需要应用特殊的图标继承逻辑
    
    override fun addModifierIconPath(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>) {
        val economicCategoryInfo = modifierInfo.economicCategoryInfo ?: return
        val economicCategoryModifierInfo = modifierInfo.economicCategoryModifierInfo ?: return
        if(economicCategoryModifierInfo.useParentIcon) {
            //去除默认的对应图标
            val economicCategoryName = economicCategoryModifierInfo.name
            registry -= "gfx/interface/icons/modifiers/mod_$economicCategoryName"
            //加入所属经济分类的对应图标
            val name = economicCategoryModifierInfo.resolveName(economicCategoryInfo.name)
            registry += "gfx/interface/icons/modifiers/mod_${name}"
        }
        //使用全局的对应图标
        val name = economicCategoryModifierInfo.resolveName(null)
        registry += "gfx/interface/icons/modifiers/mod_${name}"
    }
}
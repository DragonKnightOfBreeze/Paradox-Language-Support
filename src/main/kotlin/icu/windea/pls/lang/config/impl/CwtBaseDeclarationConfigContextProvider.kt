package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesToDistinct
        val subtypes = definitionSubtypes?.filter { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        if(!declarationConfig.propertyConfig.isBlock) return declarationConfig.propertyConfig
        
        val rootConfigs = declarationConfig.propertyConfig.configs
        val configs = rootConfigs?.flatMap { ParadoxConfigGenerator.deepCopyConfigsInDeclarationConfig(it, context) }
        return declarationConfig.propertyConfig.copy(configs = configs)
        //declarationConfig.propertyConfig.parent should be null here
    }
}
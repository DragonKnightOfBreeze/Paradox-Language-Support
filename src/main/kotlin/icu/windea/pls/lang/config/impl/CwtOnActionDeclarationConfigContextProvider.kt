package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*

private val configKey = Key.create<CwtOnActionConfig>("cwt.declarationConfigProvider.onAction.config")

class CwtOnActionDeclarationConfigContextProvider: CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "on_action") return null
        val onActionConfig = configGroup.onActions.getByTemplate(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
            .apply { putUserData(configKey, onActionConfig) }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.configGroup.gameType.id
        val definitionName = context.definitionName
        return "oa@$gameTypeId#$definitionName"
    }
    
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val config = context.getUserData(configKey)!!
        val expressions = buildSet {
            if(context.configGroup.types.get("event")?.subtypes?.containsKey("scopeless") == true) {
                add("<event.scopeless>")
            }
            add("<event.${config.eventType}>")
        }
        
        val rootConfig = declarationConfig.propertyConfig
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context) a@{ result, c1 ->
            fun asDelegated(config: CwtMemberConfig<*>): CwtMemberConfig<*> {
                return config.delegated(CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(config, context), config.parentConfig)
            }
            
            when(c1) {
                is CwtPropertyConfig -> {
                    val isKey = c1.key == "<event>"
                    val isValue = c1.stringValue == "<event>"
                    if(isKey || isValue) {
                        for(expression in expressions) {
                            val keyArg = if(isKey) expression else c1.key
                            val valueArg = if(isValue) expression else c1.stringValue.orEmpty()
                            val cc1 = c1.copy(key = keyArg, value = valueArg).also { it.parentConfig = c1.parentConfig }
                            result += asDelegated(cc1)
                        }
                        return@a
                    }
                }
                is CwtValueConfig -> {
                    if(c1.stringValue == "<event>") {
                        for(expression in expressions) {
                            val cc1 = c1.copy(pointer = emptyPointer(), value = expression).also { it.parentConfig = c1.parentConfig }
                            result += asDelegated(cc1)
                        }
                        return@a
                    }
                }
            }
            
            result += asDelegated(c1)
        }
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}
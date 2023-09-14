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
        val rootConfig = declarationConfig.propertyConfig
        return rootConfig.delegated(CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context), null)
        //parentConfig should be null here
    }
    
    //TODO
    private fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, context: CwtDeclarationConfigContext) {
        val config = context.getUserData(configKey)
        if(config == null) return
        val expressions = buildSet {
            if(context.configGroup.types.get("event")?.subtypes?.containsKey("scopeless") == true) {
                add("<event.scopeless>")
            }
            add("<event.${config.eventType}>")
        }
        declarationConfig.processDescendants p@{ c ->
            val cs = c.configs ?: return@p true
            cs as MutableList
            val ccs = mutableListOf<CwtMemberConfig<*>>()
            var i = -1
            for((index, cc) in cs.withIndex()) {
                when(cc) {
                    is CwtPropertyConfig -> {
                        val isKey = cc.key == "<event>"
                        val isValue = cc.stringValue == "<event>"
                        if(isKey || isValue) {
                            for(expression in expressions) {
                                val keyArg = if(isKey) expression else cc.key
                                val valueArg = if(isValue) expression else cc.stringValue.orEmpty()
                                val cc0 = cc.copy(key = keyArg, value = valueArg).also { it.parentConfig = cc.parentConfig }
                                ccs.add(cc0)
                                i = index
                            }
                            break
                        }
                    }
                    is CwtValueConfig -> {
                        if(cc.stringValue == "<event>") {
                            for(expression in expressions) {
                                val cc0 = cc.copy(pointer = emptyPointer(), value = expression).also { it.parentConfig = cc.parentConfig }
                                ccs.add(cc0)
                                i = index
                            }
                            break
                        }
                    }
                }
            }
            if(i != -1) {
                cs.removeAt(i)
                cs.addAll(i, ccs)
            }
            true
        }
    }
}
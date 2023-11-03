package icu.windea.pls.lang.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.util.function.*

//region Extensions

var CwtDeclarationConfigContext.gameRuleConfig: CwtGameRuleConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtOnActionConfig? by createKeyDelegate(CwtDeclarationConfigContext.Keys)

//endregion

class CwtBaseDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext {
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionSubtypes = context.definitionSubtypes
        val subtypesToDistinct = declarationConfig.subtypesToDistinct
        val subtypes = definitionSubtypes?.filter { it in subtypesToDistinct }.orEmpty()
        val typeString = subtypes.joinToString(".", context.definitionType + ".")
        return "b@$gameTypeId#$typeString"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val rootConfig = declarationConfig.propertyConfig
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context)
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}

class CwtGameRuleOverriddenDeclarationConfigContextProvider : CwtDeclarationConfigContextProvider {
    //某些game_rule的声明规则需要重载
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "game_rule") return null
        val gameRuleConfig = configGroup.gameRules.get(definitionName) ?: return null
        if(gameRuleConfig.config.configs.isNullOrEmpty()) return null
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.gameRuleConfig = gameRuleConfig }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "gr@$gameTypeId#$definitionName"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val gameRuleConfig = context.gameRuleConfig!!
        val rootConfig = gameRuleConfig.config
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context)
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}

class CwtOnActionDeclarationConfigContextProvider: CwtDeclarationConfigContextProvider {
    //如果预定义的on_action可以确定事件类型，其声明规则需要经过修改（将其中匹配"<event>"的规则，替换为此事件类型对应的规则）
    
    override fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, gameType: ParadoxGameType, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        if(definitionName == null) return null
        if(definitionType != "on_action") return null
        val onActionConfig = configGroup.onActions.getByTemplate(definitionName, element, configGroup) ?: return null
        return CwtDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, gameType, configGroup)
            .apply { this.onActionConfig = onActionConfig }
    }
    
    override fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String {
        val gameTypeId = context.gameType.id
        val definitionName = context.definitionName
        return "oa@$gameTypeId#$definitionName"
    }
    
    override fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val config = context.onActionConfig!!
        val expressions = buildSet {
            if(context.configGroup.types.get("event")?.subtypes?.containsKey("scopeless") == true) {
                add("<event.scopeless>")
            }
            add("<event.${config.eventType}>")
        }
        
        val action = object : BiConsumer<MutableList<CwtMemberConfig<*>>, CwtMemberConfig<*>> {
            override fun accept(result: MutableList<CwtMemberConfig<*>>, c1: CwtMemberConfig<*>) {
                fun asDelegated(config: CwtMemberConfig<*>): CwtMemberConfig<*> {
                    return config.delegated(CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(config, context, this), config.parentConfig)
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
                            return
                        }
                    }
                    is CwtValueConfig -> {
                        if(c1.stringValue == "<event>") {
                            for(expression in expressions) {
                                val cc1 = c1.copy(pointer = emptyPointer(), value = expression).also { it.parentConfig = c1.parentConfig }
                                result += asDelegated(cc1)
                            }
                            return
                        }
                    }
                }
                result += asDelegated(c1)
            }
        }
        
        val rootConfig = declarationConfig.propertyConfig
        val configs = CwtConfigManipulator.deepCopyConfigsInDeclarationConfig(rootConfig, context, action)
        return rootConfig.delegated(configs, null)
        //parentConfig should be null here
    }
}
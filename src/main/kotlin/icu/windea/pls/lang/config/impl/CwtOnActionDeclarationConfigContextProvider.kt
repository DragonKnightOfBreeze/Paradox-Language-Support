package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
import java.util.function.*

private val configKey = createKey<CwtOnActionConfig>("cwt.declarationConfigProvider.onAction.config")

class CwtOnActionDeclarationConfigContextProvider: CwtDeclarationConfigContextProvider {
    //如果预定义的on_action可以确定事件类型，其声明规则需要经过修改（将其中匹配"<event>"的规则，替换为此事件类型对应的规则）
    
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
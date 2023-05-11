package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

class CwtOnActionDeclarationConfigInjector : CwtDeclarationConfigInjector {
    companion object {
        val configKey = Key.create<CwtOnActionConfig?>("cwt.config.injector.onAction.config")
    }
    
    //某些预定义的on_action的声明规则需要重载（先实现，目前没有出现这种情况）
    //预定义的on_action如果指定了事件类型，声明规则中需要在"<event>"规则后加上对应的规则
    
    override fun supports(configContext: CwtConfigContext): Boolean {
        val (contextElement, name, type, _, configGroup, matchType) = configContext
        if(type == "on_action") {
            if(name == null) return false
            val config = configGroup.onActions.getByTemplate(name, contextElement, configGroup, matchType)
            configContext.putUserData(configKey, config)
            return config != null
        }
        return false
    }
    
    override fun getDeclarationMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig? {
        val config = configContext.getUserData(configKey) ?: return null
        return config.config.takeIf { it.configs.isNotNullOrEmpty() }
    }
    
    override fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtConfigContext) {
        val config = configContext.getUserData(configKey) ?: return
        val expression = "<event.${config.eventType}>"
        declarationConfig.processDescendants p@{ c ->
            when(c) {
                is CwtPropertyConfig -> {
                    val isKey = c.key == "<event>"
                    val isValue = c.stringValue == "<event>"
                    if(isKey || isValue) {
                        val cs = c.parent?.configs ?: return@p true
                        val keyArg = if(isKey) c.key else expression
                        val valueArg = if(isValue) c.stringValue.orEmpty() else expression
                        val cc = c.copy(pointer = emptyPointer(), key = keyArg, value = valueArg)
                        (cs as MutableList).add(cs.indexOf(cc), cc)
                    }
                }
                is CwtValueConfig -> {
                    if(c.stringValue == "<event>") {
                        val cs = c.parent?.configs ?: return@p true
                        val cc = c.copy(pointer = emptyPointer(), value = expression)
                        (cs as MutableList).add(cs.indexOf(cc), cc)
                    }
                }
            }
            true
        }
    }
}


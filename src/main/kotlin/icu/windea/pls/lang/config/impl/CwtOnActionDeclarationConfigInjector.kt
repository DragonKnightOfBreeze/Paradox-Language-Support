package icu.windea.pls.lang.config.impl

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

class CwtOnActionDeclarationConfigInjector : CwtDeclarationConfigInjector {
    companion object {
        val configKey = Key.create<CwtOnActionConfig>("cwt.config.injector.onAction.config")
    }
    
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
    
    override fun handleCacheKey(cacheKey: String, configContext: CwtConfigContext): String? {
        val config = configContext.getUserData(configKey)
        if(config == null) return null
        return "${configContext.definitionName}#${cacheKey}"
    }
    
    //override fun getDeclarationMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig? {
    //    val config = configContext.getUserData(configKey) ?: return null
    //    return doGetDeclarationMergedConfig(config)
    //}
    //
    //private fun doGetDeclarationMergedConfig(config: CwtGameRuleConfig): CwtPropertyConfig? {
    //    return config.config.takeIf { it.configs.isNotNullOrEmpty() }
    //}
    
    override fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtConfigContext) {
        val config = configContext.getUserData(configKey) ?: return
        val expressions = buildList {
            if(configContext.configGroup.types.get("event")?.subtypes?.containsKey("scopeless") == true) {
                add("<event.scopeless>")
            }
            add("<event.${config.eventType}>")
        }
        declarationConfig.processDescendants p@{ c ->
            val cs = c.configs ?: return@p true
            cs as MutableList
            val ccs = SmartList<CwtDataConfig<*>>()
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
                                val cc0 = cc.copy(key = keyArg, value = valueArg, stringValue = valueArg).also { it.parent = cc.parent }
                                ccs.add(cc0)
                                i = index
                            }
                            break
                        }
                    }
                    is CwtValueConfig -> {
                        if(cc.stringValue == "<event>") {
                            for(expression in expressions) {
                                val cc0 = cc.copy(pointer = emptyPointer(), value = expression, stringValue = expression).also { it.parent = cc.parent }
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

package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
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
    
    override fun getCacheKey(cacheKey0: String, configContext: CwtConfigContext): String? {
        val config = configContext.getUserData(configKey)
        if(config == null) return null
        return "${configContext.definitionName}#${cacheKey0}"
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
        ProgressManager.checkCanceled()
        val expression = "<event.${config.eventType}>"
        declarationConfig.processDescendants p@{ c ->
            ProgressManager.checkCanceled()
            when(c) {
                is CwtPropertyConfig -> {
                    val isKey = c.key == "<event>"
                    val isValue = c.stringValue == "<event>"
                    if(isKey || isValue) {
                        val cs = c.parent?.configs ?: return@p true
                        val keyArg = if(isKey) expression else c.key
                        val valueArg = if(isValue) expression else c.stringValue.orEmpty()
                        val cc = c.copy(pointer = emptyPointer(), key = keyArg, value = valueArg, stringValue = valueArg)
                        val i = cs.indexOf(c)
                        (cs as MutableList).add(i, cc) //insert before "xxx = <event>"
                    }
                }
                is CwtValueConfig -> {
                    if(c.stringValue == "<event>") {
                        val cs = c.parent?.configs ?: return@p true
                        val cc = c.copy(pointer = emptyPointer(), value = expression, stringValue = expression)
                        val i = cs.indexOf(c)
                        (cs as MutableList).add(i, cc) //insert before "<event>"
                    }
                }
            }
            true
        }
    }
}


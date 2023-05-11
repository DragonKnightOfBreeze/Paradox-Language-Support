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
        val expression = "<event.${config.eventType}>"
        declarationConfig.processDescendants p@{ c ->
            ProgressManager.checkCanceled()
            val cs = c.configs ?: return@p true
            var i = -1
            var cc: CwtDataConfig<*>? = null
            for((index, c1) in cs.withIndex()) {
                when(c1) {
                    is CwtPropertyConfig -> {
                        val isKey = c1.key == "<event>"
                        val isValue = c1.stringValue == "<event>"
                        val keyArg = if(isKey) expression else c1.key
                        val valueArg = if(isValue) expression else c1.stringValue.orEmpty()
                        if(isKey || isValue) {
                            cc = c1.copy(pointer = emptyPointer(), key = keyArg, value = valueArg, stringValue = valueArg)
                            i = index
                            break
                        }
                    }
                    is CwtValueConfig -> {
                        if(c1.stringValue == "<event>") {
                            cc = c1.copy(pointer = emptyPointer(), value = expression, stringValue = expression)
                            i = index
                            break
                        }
                    }
                }
            }
            if(cc != null) {
                (cs as MutableList).add(i, cc)
            }
            true
        }
    }
}


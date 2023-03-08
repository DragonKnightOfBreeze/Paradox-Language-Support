package icu.windea.pls.lang.config

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*

/**
 * 替换on_action声明规则中的事件引用规则表达式。
 */
class CwtOnActionConfigExpressionReplacer : CwtConfigExpressionReplacer {
    //如果definitionType是on_action且configExpression是<event>，需要基于on_actions.cwt加上具体的事件类型。
    
    override fun shouldReplace(configContext: CwtConfigContext): Boolean {
        val (contextElement, name, type, _, configGroup) = configContext
        when {
            type == "on_action" -> {
                if(name == null) return false
                val config = configGroup.onActions.getByTemplate(name, contextElement, configGroup)
                return config != null
            }
        }
        return false
    }
    
    override fun doReplace(configExpression: String, configContext: CwtConfigContext): String? {
        val (contextElement, name, type, _, configGroup) = configContext
        when {
            type == "on_action" -> {
                if(name == null) return null
                if(configExpression == "<event>") {
                    val config = configGroup.onActions.getByTemplate(name, contextElement, configGroup)
                    if(config == null) return null
                    val eventType = config.eventType
                    return "<event.${eventType}>"
                }
            }
        }
        return null
    }
}

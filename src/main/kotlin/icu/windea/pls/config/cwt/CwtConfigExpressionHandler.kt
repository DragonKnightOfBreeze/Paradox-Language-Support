@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

import com.intellij.psi.*

/**
 * 某些情况下，需要对configExpression作进一步的处理。（例如，对于`on_action`）
 */
object CwtConfigExpressionHandler {
    fun shouldHandle(element: PsiElement, name: String?, type: String, subtype: List<String>?, configGroup: CwtConfigGroup): Boolean {
        when {
            type == "on_action" -> {
                if(name == null) return false
                val config = configGroup.onActions.getByTemplate(name, element, configGroup)
                return config != null
            }
        }
        return false
    }
    
    fun handle(element: PsiElement, configExpression: String, name: String?, type: String, subtype: List<String>?, configGroup: CwtConfigGroup): String {
        when {
            //如果definitionType是on_action且configExpression是<event>，需要参照on_actions.csv进行特殊处理
            type == "on_action" -> {
                if(name == null) return configExpression
                if(configExpression == "<event>") {
                    val config = configGroup.onActions.getByTemplate(name, element, configGroup)
                    if(config == null) return configExpression
                    val eventType = config.eventType
                    return "<event.${eventType}>"
                }
            }
        }
        return configExpression
    }
}
@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

/**
 * 某些情况下，需要对configExpression作进一步的处理。（例如，对于`on_action`）
 */
object CwtConfigExpressionHandler {
	fun shouldHandle(name: String?, type: String, subtype: List<String>?, configGroup: CwtConfigGroup): Boolean {
		when{
			type == "on_action" -> {
				if(name == null) return false
				return configGroup.onActions.containsKey(name)
			}
		}
		return false
	}
	
	fun handle(configExpression: String, name: String?, type: String, subtype: List<String>?, configGroup: CwtConfigGroup): String {
		when{
			//如果definitionType是on_action且configExpression是<event>，需要参照on_actions.csv进行特殊处理
			type == "on_action" -> {
				if(name == null) return configExpression
				if(configExpression == "<event>") {
					val eventType = configGroup.onActions[name]?.event
					if(eventType != null) {
						return "<event.${eventType}>"
					}
				}
			}
		}
		return configExpression
	}
}
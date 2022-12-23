package icu.windea.pls.config

import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.model.*

/**
 * 某些情况下，需要对configExpression作进一步的处理。（例如，`on_action`）
 */
object CwtConfigExpressionResolver {
	fun resolved(configExpression: CwtDataExpression, definitionMemberInfo: ParadoxDefinitionMemberInfo?): CwtDataExpression {
		////如果definitionType是on_action且configExpression是<event>，需要参照on_actions.csv进行特殊处理
		if(definitionMemberInfo == null) return configExpression
		val definitionInfo = definitionMemberInfo.definitionInfo
		val configGroup = definitionMemberInfo.configGroup
		when{
			configExpression.type == CwtDataTypes.TypeExpression -> {
				if(definitionInfo.type == "on_action" && configExpression.expressionString == "<event>") {
					val eventType = configGroup.onActions[definitionInfo.name]?.event
					if(eventType != null) {
						when(configExpression) {
							is CwtKeyExpression -> CwtKeyExpression.resolve("<event.${eventType}>")
							is CwtValueExpression -> CwtValueExpression.resolve("<event.${eventType}>")
						}
					}
				}
			}
		}
		return configExpression
	}
}
package icu.windea.pls.core.handler

import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*

object ParadoxExpressionHandler {
	@JvmStatic
	fun isSystemScope(text: String, configGroup: CwtConfigGroup): Boolean{
		return getInternalConfig(configGroup.project).systemScopeMap.containsKey(text)
	}
	
	@JvmStatic
	fun isScopeLink(text: String, configGroup: CwtConfigGroup) : Boolean {
		return configGroup.linksAsScopeNotData.containsKey(text)
	}
}

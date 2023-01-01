package icu.windea.pls.config.cwt.config

import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

data class CwtConfigGroupInfo(
	val groupName: String
) {
	lateinit var configGroup: CwtConfigGroup
	
	val aliasNameSupportScope = mutableSetOf<String>()
	
	/**
	 * @see CwtDataType.FilePath
	 * @see CwtPathExpressionType.FilePath
	 */
	val filePathExpressions = mutableSetOf<String>()
	
	/**
	 * @see CwtDataType.Icon
	 * @see CwtPathExpressionType.Icon
	 */
	val iconPathExpressions = mutableSetOf<String>()
	
	/**
	 * @see CwtDataType.TemplateExpression
	 */
	val templateExpressionLinks = mutableListOf<LocationLink>()
	//val templateExpressionLinks = mutableMapOf<String, MutableList<LocationLink>>() //typeExpression - links
	
	fun acceptConfigExpression(configExpression: CwtDataExpression) {
		when(configExpression.type) {
			CwtDataType.FilePath -> {
				configExpression.value?.let { filePathExpressions.add(it) }
			}
			CwtDataType.Icon -> {
				configExpression.value?.let { iconPathExpressions.add(it) }
			}
			CwtDataType.TemplateExpression -> {
				val link = configExpression.extraValue?.castOrNull<Pair<String, String>>()?.let { "$" linkTo "${it.first}$${it.second}" }
				link?.let { templateExpressionLinks.add(it) }
				//val typeExpression = this.value
				//val link = this.extraValue?.castOrNull<Pair<String, String>>()?.let { "$" linkTo "${it.first}$${it.second}" }
				//if(typeExpression != null && link != null) {
				//	info.templateExpressionLinks.getOrPut(typeExpression) { SmartList() }.add(link)
				//}
			}
			else -> pass()
		}
	}
	
	fun acceptAliasConfig(aliasConfig: CwtAliasConfig) {
		if(aliasConfig.expression.type == CwtDataType.ScopeField) {
			aliasNameSupportScope.add(aliasConfig.name)
		}
	}
}
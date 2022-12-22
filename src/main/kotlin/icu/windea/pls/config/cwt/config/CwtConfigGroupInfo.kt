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
	 * @see CwtDataTypes.FilePath
	 * @see CwtPathExpressionType.FilePath
	 */
	val filePathExpressions = mutableSetOf<String>()
	
	/**
	 * @see CwtDataTypes.Icon
	 * @see CwtPathExpressionType.Icon
	 */
	val iconPathExpressions = mutableSetOf<String>()
	
	/**
	 * @see CwtDataTypes.TypeExpressionString
	 */
	val typeExpressionStringLinks = mutableListOf<LocationLink>()
	//val typeExpressionStringLinks = mutableMapOf<String, MutableList<LocationLink>>() //typeExpression - links
	
	fun acceptConfigExpression(configExpression: CwtDataExpression) {
		when(configExpression.type) {
			CwtDataTypes.TypeExpressionString -> {
				val link = configExpression.extraValue?.castOrNull<Pair<String, String>>()?.let { "$" linkTo "${it.first}$${it.second}" }
				link?.let { typeExpressionStringLinks.add(it) }
				//val typeExpression = this.value
				//val link = this.extraValue?.castOrNull<Pair<String, String>>()?.let { "$" linkTo "${it.first}$${it.second}" }
				//if(typeExpression != null && link != null) {
				//	info.typeExpressionStringLinks.getOrPut(typeExpression) { SmartList() }.add(link)
				//}
			}
			CwtDataTypes.FilePath -> {
				configExpression.value?.let { filePathExpressions.add(it) }
			}
			CwtDataTypes.Icon -> {
				configExpression.value?.let { iconPathExpressions.add(it) }
			}
			else -> pass()
		}
	}
	
	fun acceptAliasConfig(aliasConfig: CwtAliasConfig) {
		if(aliasConfig.expression.type == CwtDataTypes.ScopeField) {
			aliasNameSupportScope.add(aliasConfig.name)
		}
	}
}
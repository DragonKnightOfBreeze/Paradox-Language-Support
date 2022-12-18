package icu.windea.pls.config.cwt.config

import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.util.*

data class CwtConfigGroupInfo(
	val groupName: String
) {
	lateinit var configGroup: CwtConfigGroup
	
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
}
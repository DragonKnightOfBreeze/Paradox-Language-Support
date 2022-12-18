package icu.windea.pls.config.cwt.config

import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*

data class CwtConfigGroupInfo(
	val groupName: String
) {
	lateinit var configGroup: CwtConfigGroup
	
	/**
	 * @see CwtPathExpressionType.FilePath
	 */
	val filePathExpressions = mutableSetOf<String>()
	
	/**
	 * @see CwtPathExpressionType.Icon
	 */
	val iconPathExpressions = mutableSetOf<String>()
}
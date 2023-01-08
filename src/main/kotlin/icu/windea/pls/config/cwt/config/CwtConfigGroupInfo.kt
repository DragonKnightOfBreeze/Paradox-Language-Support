package icu.windea.pls.config.cwt.config

import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*

data class CwtConfigGroupInfo(
	val groupName: String
) {
	lateinit var configGroup: CwtConfigGroup
	
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
	 * @see CwtTemplateExpression
	 */
	val templateExpressions = mutableMapOf<CwtDataExpression, MutableList<CwtTemplateExpression>>()
	
	fun acceptConfigExpression(configExpression: CwtDataExpression) {
		when(configExpression.type) {
			CwtDataType.FilePath -> {
				configExpression.value?.let { filePathExpressions.add(it) }
			}
			CwtDataType.Icon -> {
				configExpression.value?.let { iconPathExpressions.add(it) }
			}
			CwtDataType.TemplateExpression -> {
				val templateExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
				for(referenceExpression in templateExpression.referenceExpressions) {
					templateExpressions.getOrPut(referenceExpression){ SmartList()}
						.add(templateExpression)
				}
			}
			else -> pass()
		}
	}
}
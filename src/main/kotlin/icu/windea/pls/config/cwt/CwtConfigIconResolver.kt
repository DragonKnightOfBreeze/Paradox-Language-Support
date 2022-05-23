package icu.windea.pls.config.cwt

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import javax.swing.*

/**
 * 用于根据配置上的选项、配置类型和表达式类型获取进行代码补全时需要显示的图标。
 */
object CwtConfigIconResolver {
	fun resolve(config: CwtConfig<*>, keyType: CwtKeyExpression.Type? = null, valueType: CwtValueExpression.Type? = null): Icon? {
		when {
			config is CwtKvConfig<*> -> {
				val iconOption = config.options?.find { it.key == "icon" }?.value
				if(iconOption != null) {
					when(iconOption) {
						"tag" -> return PlsIcons.tagIcon
						"property" -> return PlsIcons.propertyIcon
						"value" -> return PlsIcons.valueIcon
						//TO IMPLEMENT
					}
				}
				if(keyType != null) {
					when(keyType) {
						CwtKeyExpression.Type.Localisation -> return PlsIcons.localisationIcon
						CwtKeyExpression.Type.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtKeyExpression.Type.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtKeyExpression.Type.TypeExpression -> return PlsIcons.definitionIcon
						CwtKeyExpression.Type.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtKeyExpression.Type.Value -> return PlsIcons.valueIcon
						CwtKeyExpression.Type.Enum -> return PlsIcons.enumIcon
						CwtKeyExpression.Type.ComplexEnum -> return PlsIcons.enumIcon
						CwtKeyExpression.Type.Constant -> return PlsIcons.propertyIcon
						else -> pass()
					}
				}
				if(valueType != null) {
					when(valueType) {
						CwtValueExpression.Type.Localisation -> return PlsIcons.localisationIcon
						CwtValueExpression.Type.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtValueExpression.Type.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtValueExpression.Type.TypeExpression -> return PlsIcons.definitionIcon
						CwtValueExpression.Type.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtValueExpression.Type.Value -> return PlsIcons.valueIcon
						CwtValueExpression.Type.Enum -> return PlsIcons.enumIcon
						CwtValueExpression.Type.ComplexEnum -> return PlsIcons.enumIcon
						CwtValueExpression.Type.Constant -> return PlsIcons.valueIcon
						else -> pass()
					}
				}
			}
			config is CwtModifierConfig -> return PlsIcons.modifierIcon
			config is CwtLocalisationCommandConfig -> return PlsIcons.localisationCommandFieldIcon
		}
		return null
	}
}
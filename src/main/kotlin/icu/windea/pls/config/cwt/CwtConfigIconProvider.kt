package icu.windea.pls.config.cwt

import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import javax.swing.*

/**
 * CWT规则图标的提供器。
 *
 * 用于根据配置上的选项、配置类型和表达式类型获取进行代码补全时需要显示的图标。
 */
@Service(Service.Level.APP)
class CwtConfigIconProvider {
	fun resolve(config: CwtConfig<*>, keyType: CwtKeyExpressionType? = null, valueType: CwtValueExpressionType? = null): Icon? {
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
						CwtKvExpressionTypes.Localisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.TypeExpression -> return PlsIcons.definitionIcon
						CwtKvExpressionTypes.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtKvExpressionTypes.Value -> return PlsIcons.valueIcon
						CwtKvExpressionTypes.Enum -> return PlsIcons.enumIcon
						CwtKvExpressionTypes.ComplexEnum -> return PlsIcons.enumIcon
						CwtKvExpressionTypes.Constant -> return PlsIcons.propertyIcon
						else -> pass()
					}
				}
				if(valueType != null) {
					when(valueType) {
						CwtKvExpressionTypes.Localisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtKvExpressionTypes.TypeExpression -> return PlsIcons.definitionIcon
						CwtKvExpressionTypes.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtKvExpressionTypes.Value -> return PlsIcons.valueIcon
						CwtKvExpressionTypes.Enum -> return PlsIcons.enumIcon
						CwtKvExpressionTypes.ComplexEnum -> return PlsIcons.enumIcon
						CwtKvExpressionTypes.Constant -> return PlsIcons.valueIcon
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
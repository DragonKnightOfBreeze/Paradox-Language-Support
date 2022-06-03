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
	fun resolve(config: CwtConfig<*>, keyType: CwtKeyDataType? = null, valueType: CwtValueDataType? = null): Icon? {
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
						CwtDataTypes.Localisation -> return PlsIcons.localisationIcon
						CwtDataTypes.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtDataTypes.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtDataTypes.TypeExpression -> return PlsIcons.definitionIcon
						CwtDataTypes.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtDataTypes.Value -> return PlsIcons.valueIcon
						CwtDataTypes.Enum -> return PlsIcons.enumIcon
						CwtDataTypes.ComplexEnum -> return PlsIcons.enumIcon
						CwtDataTypes.Constant -> return PlsIcons.propertyIcon
						else -> pass()
					}
				}
				if(valueType != null) {
					when(valueType) {
						CwtDataTypes.Localisation -> return PlsIcons.localisationIcon
						CwtDataTypes.SyncedLocalisation -> return PlsIcons.localisationIcon
						CwtDataTypes.InlineLocalisation -> return PlsIcons.localisationIcon
						CwtDataTypes.TypeExpression -> return PlsIcons.definitionIcon
						CwtDataTypes.TypeExpressionString -> return PlsIcons.definitionIcon
						CwtDataTypes.Value -> return PlsIcons.valueIcon
						CwtDataTypes.Enum -> return PlsIcons.enumIcon
						CwtDataTypes.ComplexEnum -> return PlsIcons.enumIcon
						CwtDataTypes.Constant -> return PlsIcons.valueIcon
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
@file:Suppress("unused")

package icu.windea.pls.config.cwt

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

internal typealias CwtConfigMap = MutableMap<String, CwtFileConfig>
internal typealias CwtConfigMaps = MutableMap<String, CwtConfigMap>


val CwtProperty.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtProperty): CwtConfigType? {
	val name = element.name
	return when {
		name.surroundsWith("type[", "]") -> CwtConfigType.Type
		name.surroundsWith("subtype[", "]") -> CwtConfigType.Subtype
		name.surroundsWith("enum[", "]") -> CwtConfigType.Enum
		name.surroundsWith("complex_enum[", "]") -> CwtConfigType.ComplexEnum
		name.surroundsWith("value[", "]") -> CwtConfigType.Value
		name.surroundsWith("single_alias[", "]") -> CwtConfigType.SingleAlias
		name.surroundsWith("alias[", "]") -> CwtConfigType.Alias
		else -> {
			val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
			val parentName = parentProperty.name
			when {
				parentName == "links" -> CwtConfigType.Link
				parentName == "localisation_links" -> CwtConfigType.LocalisationLink
				parentName == "localisation_commands" -> CwtConfigType.LocalisationCommand
				parentName == "modifier_categories" -> CwtConfigType.ModifierCategory
				parentName == "modifiers" -> CwtConfigType.Modifier
				parentName == "scopes" -> CwtConfigType.Scope
				parentName == "scope_groups" -> CwtConfigType.ScopeGroup
				parentName == "tags" -> CwtConfigType.Tag
				//from internal config
				parentName == "locales" -> CwtConfigType.LocalisationLocale
				parentName == "colors" -> CwtConfigType.LocalisationColor
				parentName == "predefined_variables" -> CwtConfigType.LocalisationPredefinedVariable
				else -> null
			}
		}
	}
}

val CwtValue.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtValue): CwtConfigType? {
	val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
	val parentName = parentProperty.name
	return when {
		parentName.surroundsWith("enum[", "]") -> CwtConfigType.EnumValue
		parentName.surroundsWith("value[", "]") -> CwtConfigType.ValueValue
		else -> null
	}
}


fun ParadoxScriptProperty.getPropertyConfig(): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val element = this
	val definitionElementInfo = element.definitionElementInfo ?: return null
	if(definitionElementInfo.elementPath.isEmpty()) return null //不允许value直接是定义的value的情况
	return definitionElementInfo.matchedPropertyConfig
		?: definitionElementInfo.propertyConfigs.singleOrNull()
}

fun ParadoxScriptPropertyKey.getPropertyConfig(): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val element = this
	val definitionElementInfo = element.definitionElementInfo ?: return null
	if(definitionElementInfo.elementPath.isEmpty()) return null //不允许value直接是定义的value的情况
	return definitionElementInfo.matchedPropertyConfig
		?: definitionElementInfo.propertyConfigs.singleOrNull()
}

fun ParadoxScriptValue.getValueConfig(): CwtValueConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	//NOTE 如果已经匹配propertyConfig但是无法匹配valueConfig，使用唯一的那个或者null
	val element = this
	val parent = element.parent
	when(parent) {
		//如果value是property的value
		is ParadoxScriptPropertyValue -> {
			val property = parent.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			return definitionElementInfo.matchedPropertyConfig?.valueConfig
				?: definitionElementInfo.propertyConfigs.singleOrNull()?.valueConfig
		}
		//如果value是block中的value
		is ParadoxScriptBlock -> {
			val property = parent.parent?.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			val childValueConfigs = definitionElementInfo.childValueConfigs
			if(childValueConfigs.isEmpty()) return null
			val gameType = definitionElementInfo.gameType
			val configGroup = getCwtConfig(element.project).getValue(gameType)
			return childValueConfigs.find { CwtConfigHandler.matchesValue(it.valueExpression, element, configGroup) }
				?: childValueConfigs.singleOrNull()
		}
		else -> return null
	}
}
package icu.windea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

enum class CwtConfigType(
	val id: String,
	val text: String,
	val isReference: Boolean = false,
	val category: String? = null
) {
	Type("type", PlsDocBundle.message("name.cwt.type")),
	Subtype("subtype", PlsDocBundle.message("name.cwt.subtype")),
	Enum("enum", PlsDocBundle.message("name.cwt.enum")),
	ComplexEnum("complex enum", PlsDocBundle.message("name.cwt.complexEnum")),
	Value("value", PlsDocBundle.message("name.cwt.value")),
	SingleAlias("single alias", PlsDocBundle.message("name.cwt.singleAlias")),
	Alias("alias", PlsDocBundle.message("name.cwt.alias")),
	
	EnumValue("enum value", PlsDocBundle.message("name.cwt.enumValue"), true, category = "enums"),
	ValueSetValue("value set value", PlsDocBundle.message("name.cwt.valueSetValue"), true, category = "values"),
	
	Link("link", PlsDocBundle.message("name.cwt.link"), true),
	LocalisationLink("localisation link", PlsDocBundle.message("name.cwt.localisationLink"), true),
	LocalisationCommand("localisation command", PlsDocBundle.message("name.cwt.localisationCommand"), true),
	ModifierCategory("modifier category", PlsDocBundle.message("name.cwt.modifierCategory"), true),
	Modifier("modifier", PlsDocBundle.message("name.cwt.modifier"), true),
	Scope("scope", PlsDocBundle.message("name.cwt.scope"), true),
	ScopeGroup("scope group", PlsDocBundle.message("name.cwt.scopeGroup"), true),
	
	SystemScope("system scope", PlsDocBundle.message("name.cwt.systemScope"), true),
	LocalisationLocale("localisation locale", PlsDocBundle.message("name.cwt.localisationLocale"), true),
	LocalisationColor("localisation color", PlsDocBundle.message("name.cwt.localisationColor"), true),
	LocalisationPredefinedVariable("localisation predefined variable", PlsDocBundle.message("name.cwt.localisationPredefinedVariable"), true);
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		//属性名匹配+父属性名匹配，不检查属性是否在正确的位置，插件开发者应当保证CWT配置是正确的……
		
		fun resolve(element: PsiElement): CwtConfigType? {
			return when {
				element is CwtProperty -> resolve(element)
				element is CwtValue -> resolve(element)
				else -> null
			}
		}
		
		fun resolve(element: CwtProperty): CwtConfigType? {
			val name = element.name
			return when {
				name.surroundsWith("type[", "]") -> Type
				name.surroundsWith("subtype[", "]") -> Subtype
				name.surroundsWith("enum[", "]") -> Enum
				name.surroundsWith("complex_enum[", "]") -> ComplexEnum
				name.surroundsWith("value[", "]") -> Value
				name.surroundsWith("single_alias[", "]") -> SingleAlias
				name.surroundsWith("alias[", "]") -> Alias
				else -> {
					val parentProperty = element.parentOfType<CwtProperty>() ?: return null
					val parentName = parentProperty.name
					when {
						parentName == "links" -> Link
						parentName == "localisation_links" -> LocalisationLink
						parentName == "localisation_commands" -> LocalisationCommand
						parentName == "modifier_categories" -> ModifierCategory
						parentName == "modifiers" -> Modifier
						parentName == "scopes" -> Scope
						parentName == "scope_groups" -> ScopeGroup
						//from internal config
						parentName == "system_scopes" && parentProperty.containingFile.name == InternalConfigGroup.scriptConfigFileName -> SystemScope
						parentName == "locales" && parentProperty.containingFile.name == InternalConfigGroup.localisationConfigFileName -> LocalisationLocale
						parentName == "colors" && parentProperty.containingFile.name == InternalConfigGroup.localisationConfigFileName -> LocalisationColor
						parentName == "predefined_variables" && parentProperty.containingFile.name == InternalConfigGroup.localisationConfigFileName -> LocalisationPredefinedVariable
						else -> null
					}
				}
			}
		}
		
		fun resolve(element: CwtValue): CwtConfigType? {
			val parentProperty = element.parentOfType<CwtProperty>() ?: return null
			val parentName = parentProperty.name
			val parentParentProperty = parentProperty.parentOfType<CwtProperty>()
			val parentParentName = parentParentProperty?.name
			return when {
				parentName.surroundsWith("enum[", "]") -> EnumValue
				parentName.surroundsWith("value[", "]") -> ValueSetValue
				parentParentName == "scope_groups" -> Scope
				else -> null
			}
		}
	}
}
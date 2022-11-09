package icu.windea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

sealed class CwtConfigType(
	val id: String,
	val isReference: Boolean = false,
	val category: String? = null
) {
	abstract val nameText: String
	open val descriptionText: String? = null
	
	object Type : CwtConfigType("type") {
		override val nameText get() = PlsDocBundle.message("name.cwt.type")
	}
	
	object Subtype : CwtConfigType("subtype") {
		override val nameText get() = PlsDocBundle.message("name.cwt.subtype")
	}
	
	object Enum : CwtConfigType("enum") {
		override val nameText get() = PlsDocBundle.message("name.cwt.enum")
	}
	
	object ComplexEnum : CwtConfigType("complex enum") {
		override val nameText get() = PlsDocBundle.message("name.cwt.complexEnum")
	}
	
	object Value : CwtConfigType("value") {
		override val nameText get() = PlsDocBundle.message("name.cwt.value")
	}
	
	object SingleAlias : CwtConfigType("single alias") {
		override val nameText get() = PlsDocBundle.message("name.cwt.singleAlias")
	}
	
	object Alias : CwtConfigType("alias") {
		override val nameText get() = PlsDocBundle.message("name.cwt.alias")
	}
	
	object EnumValue : CwtConfigType("enum value", true, "enums") {
		override val nameText get() = PlsDocBundle.message("name.cwt.enumValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
	}
	
	object ValueSetValue : CwtConfigType("value set value", true, "values") {
		override val nameText get() = PlsDocBundle.message("name.cwt.valueSetValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.valueSetValue")
	}
	
	object Link : CwtConfigType("link", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.link")
		override val descriptionText get() = PlsBundle.message("cwt.description.link")
	}
	
	object LocalisationLink : CwtConfigType("localisation link", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
	}
	
	object LocalisationCommand : CwtConfigType("localisation command", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationCommand")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
	}
	
	object ModifierCategory : CwtConfigType("modifier category", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.modifierCategory")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
	}
	
	object Modifier : CwtConfigType("modifier", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.modifier")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
	}
	
	object Scope : CwtConfigType("scope", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.scope")
		override val descriptionText get() = PlsBundle.message("cwt.description.scope")
	}
	
	object ScopeGroup : CwtConfigType("scope group", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.scopeGroup")
		override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
	}
	
	object SystemScope : CwtConfigType("system scope", isReference = true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.systemScope")
		override val descriptionText get() = PlsBundle.message("cwt.description.systemScope")
	}
	
	object LocalisationLocale : CwtConfigType("localisation locale", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationLocale")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
	}
	
	object LocalisationPredefinedVariable : CwtConfigType("localisation predefined variable", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationPredefinedVariable")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationPredefinedVariable")
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
		
		fun values(): Array<CwtConfigType> {
			return arrayOf(Type, Subtype, Enum, ComplexEnum, Value, SingleAlias, Alias, EnumValue, ValueSetValue, Link, LocalisationLink, LocalisationCommand, ModifierCategory, Modifier, Scope, ScopeGroup, SystemScope, LocalisationLocale, LocalisationPredefinedVariable)
		}
		
		fun valueOf(value: String): CwtConfigType {
			return when(value) {
				"Type" -> Type
				"Subtype" -> Subtype
				"Enum" -> Enum
				"ComplexEnum" -> ComplexEnum
				"Value" -> Value
				"SingleAlias" -> SingleAlias
				"Alias" -> Alias
				"EnumValue" -> EnumValue
				"ValueSetValue" -> ValueSetValue
				"Link" -> Link
				"LocalisationLink" -> LocalisationLink
				"LocalisationCommand" -> LocalisationCommand
				"ModifierCategory" -> ModifierCategory
				"Modifier" -> Modifier
				"Scope" -> Scope
				"ScopeGroup" -> ScopeGroup
				"SystemScope" -> SystemScope
				"LocalisationLocale" -> LocalisationLocale
				"LocalisationPredefinedVariable" -> LocalisationPredefinedVariable
				else -> throw IllegalArgumentException("No object icu.windea.pls.cwt.CwtConfigType.$value")
			}
		}
	}
}
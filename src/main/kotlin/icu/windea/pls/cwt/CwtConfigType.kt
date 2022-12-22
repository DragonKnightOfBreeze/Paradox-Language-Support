package icu.windea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

enum class CwtConfigType(
	val id: String,
	val isReference: Boolean = false,
	val category: String? = null
) {
	Type("type") {
		override val nameText get() = PlsDocBundle.message("prefix.type")
	},
	Subtype("subtype") {
		override val nameText get() = PlsDocBundle.message("prefix.subtype")
	},
	Enum("enum") {
		override val nameText get() = PlsDocBundle.message("prefix.enum")
	},
	ComplexEnum("complex enum") {
		override val nameText get() = PlsDocBundle.message("prefix.complexEnum")
	},
	ValueSet("value") {
		override val nameText get() = PlsDocBundle.message("prefix.valueSet")
	},
	SingleAlias("single alias") {
		override val nameText get() = PlsDocBundle.message("prefix.singleAlias")
	},
	Alias("alias") {
		override val nameText get() = PlsDocBundle.message("prefix.alias")
	},
	EnumValue("enum value", true, "enums") {
		override val nameText get() = PlsDocBundle.message("prefix.enumValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
	},
	ValueSetValue("value set value", true, "values") {
		override val nameText get() = PlsDocBundle.message("prefix.valueSetValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.valueSetValue")
	},
	Link("link", true) {
		override val nameText get() = PlsDocBundle.message("prefix.link")
		override val descriptionText get() = PlsBundle.message("cwt.description.link")
	},
	LocalisationLink("localisation link", true) {
		override val nameText get() = PlsDocBundle.message("prefix.localisationLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
	},
	LocalisationCommand("localisation command", true) {
		override val nameText get() = PlsDocBundle.message("prefix.localisationCommand")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
	},
	ModifierCategory("modifier category", true) {
		override val nameText get() = PlsDocBundle.message("prefix.modifierCategory")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
	},
	Modifier("modifier", true) {
		override val nameText get() = PlsDocBundle.message("prefix.modifier")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
	},
	Scope("scope", true) {
		override val nameText get() = PlsDocBundle.message("prefix.scope")
		override val descriptionText get() = PlsBundle.message("cwt.description.scope")
	},
	ScopeGroup("scope group", true) {
		override val nameText get() = PlsDocBundle.message("prefix.scopeGroup")
		override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
	},
	SystemScope("system scope", true) {
		override val nameText get() = PlsDocBundle.message("prefix.systemScope")
		override val descriptionText get() = PlsBundle.message("cwt.description.systemScope")
	},
	LocalisationLocale("localisation locale", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationLocale")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
	},
	LocalisationPredefinedVariable("localisation predefined variable", true) {
		override val nameText get() = PlsDocBundle.message("name.cwt.localisationPredefinedVariable")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationPredefinedVariable")
	};
	
	abstract val nameText: String
	open val descriptionText: String? = null
	
	companion object {
		//属性名匹配+父属性名匹配，不检查属性是否在正确的位置，插件开发者应当保证CWT配置是正确的……
		
		fun resolve(element: CwtProperty): CwtConfigType? {
			val name = element.name
			return when {
				name.surroundsWith("type[", "]") -> Type
				name.surroundsWith("subtype[", "]") -> Subtype
				name.surroundsWith("enum[", "]") -> Enum
				name.surroundsWith("complex_enum[", "]") -> ComplexEnum
				name.surroundsWith("value[", "]") -> ValueSet
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
						parentName == "system_scopes" && getFileKey(parentProperty) == "system_scopes" -> SystemScope
						parentName == "localisation_locales" && getFileKey(parentProperty) == "localisation_locales" -> LocalisationLocale
						parentName == "localisation_predefined_parameters" && getFileKey(parentProperty) == "localisation_predefined_parameters" -> LocalisationPredefinedVariable
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
		
		private fun getFileKey(element: PsiElement): String? {
			return element.containingFile?.name?.substringBefore('.')
		}
	}
}

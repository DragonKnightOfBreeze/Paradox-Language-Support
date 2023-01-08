package icu.windea.pls.config.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

enum class CwtConfigType(
	val id: String,
	val isReference: Boolean = false,
	val category: String? = null
) {
	Type("type") {
		override val prefix get() = PlsDocBundle.message("prefix.type")
	},
	Subtype("subtype") {
		override val prefix get() = PlsDocBundle.message("prefix.subtype")
	},
	Enum("enum") {
		override val prefix get() = PlsDocBundle.message("prefix.enum")
	},
	ComplexEnum("complex enum") {
		override val prefix get() = PlsDocBundle.message("prefix.complexEnum")
	},
	ValueSet("value") {
		override val prefix get() = PlsDocBundle.message("prefix.valueSet")
	},
	SingleAlias("single alias") {
		override val prefix get() = PlsDocBundle.message("prefix.singleAlias")
		override val icon get() =  PlsIcons.Alias
	},
	Alias("alias") {
		override val prefix: String get() = PlsDocBundle.message("prefix.alias")
		override val icon get() =  PlsIcons.Alias
	},
	EnumValue("enum value", true, "enums") {
		override val prefix get() = PlsDocBundle.message("prefix.enumValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
		override val icon get() =  PlsIcons.EnumValue
	},
	ValueSetValue("value set value", true, "values") {
		override val prefix get() = PlsDocBundle.message("prefix.valueSetValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.valueSetValue")
		override val icon get() =  PlsIcons.ValueSetValue
	},
	Link("link", true) {
		override val prefix get() = PlsDocBundle.message("prefix.link")
		override val descriptionText get() = PlsBundle.message("cwt.description.link")
		override val icon get() = PlsIcons.Link
	},
	LocalisationLink("localisation link", true) {
		override val prefix get() = PlsDocBundle.message("prefix.localisationLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
		override val icon get() = PlsIcons.Link
	},
	LocalisationCommand("localisation command", true) {
		override val prefix get() = PlsDocBundle.message("prefix.localisationCommand")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
		override val icon get() = PlsIcons.LocalisationCommandField
	},
	ModifierCategory("modifier category", true) {
		override val prefix get() = PlsDocBundle.message("prefix.modifierCategory")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
	},
	Modifier("modifier", true) {
		override val prefix get() = PlsDocBundle.message("prefix.modifier")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
		override val icon get() = PlsIcons.Modifier
	},
	Scope("scope", true) {
		override val prefix get() = PlsDocBundle.message("prefix.scope")
		override val descriptionText get() = PlsBundle.message("cwt.description.scope")
		override val icon get() = PlsIcons.Scope
	},
	ScopeGroup("scope group", true) {
		override val prefix get() = PlsDocBundle.message("prefix.scopeGroup")
		override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
	},
	SystemScope("system scope", true) {
		override val prefix get() = PlsDocBundle.message("prefix.systemScope")
		override val descriptionText get() = PlsBundle.message("cwt.description.systemScope")
		override val icon get() = PlsIcons.SystemScope
	},
	LocalisationLocale("localisation locale", true) {
		override val prefix get() = PlsDocBundle.message("prefix.localisationLocale")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
		override val icon get() = PlsIcons.LocalisationLocale
	},
	LocalisationPredefinedParameter("localisation predefined parameter", true) {
		override val prefix get() = PlsDocBundle.message("prefix.localisationPredefinedParameter")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationPredefinedParameter")
		override val icon get() = PlsIcons.PredefinedParameter
	};
	
	abstract val prefix: String
	open val descriptionText: String? = null
	open val icon: Icon? = null
	
	companion object {
		@JvmStatic
		fun resolve(element: CwtProperty): CwtConfigType? {
			return CachedValuesManager.getCachedValue(element, PlsKeys.cachedCwtConfigTypeKey) {
				val file = element.containingFile
				val value =  doResolve(element, file)
				CachedValueProvider.Result.create(value, file)
			}
		}
		
		private fun doResolve(element: CwtProperty, file: PsiFile): CwtConfigType? {
			val fileKey = file.name.substringBefore('.')
			val name = element.name
			when {
				name.surroundsWith("type[", "]") -> {
					Type
				}
				name.surroundsWith("subtype[", "]") -> {
					Subtype
				}
				name.surroundsWith("enum[", "]") -> {
					Enum
				}
				name.surroundsWith("complex_enum[", "]") -> {
					ComplexEnum
				}
				name.surroundsWith("value[", "]") -> {
					ValueSet
				}
				name.surroundsWith("single_alias[", "]") -> {
					SingleAlias
				}
				name.surroundsWith("alias[", "]") -> {
					Alias
				}
				else -> {
					val parentProperty = element.parentProperty() ?: return null
					val parentName = parentProperty.name
					when {
						parentName == "links" -> Link
						parentName == "localisation_links" -> LocalisationLink
						parentName == "localisation_commands" -> LocalisationCommand
						parentName == "modifier_categories" -> ModifierCategory
						parentName == "modifiers" -> Modifier
						parentName == "scopes" -> Scope
						parentName == "scope_groups" -> ScopeGroup
						parentName == "system_scopes" && fileKey == "system_scopes" -> SystemScope
						parentName == "localisation_locales" && fileKey == "localisation_locales" -> LocalisationLocale
						parentName == "localisation_predefined_parameters" && fileKey == "localisation_predefined_parameters" -> LocalisationPredefinedParameter
						else -> null
					}
				}
			}
			return null
		}
		
		fun resolve(element: CwtValue): CwtConfigType? {
			return CachedValuesManager.getCachedValue(element, PlsKeys.cachedCwtConfigTypeKey) {
				val file = element.containingFile
				val value =  doResolve(element, file)
				CachedValueProvider.Result.create(value, file)
			}
		}
		
		private fun doResolve(element: CwtValue, file: PsiFile): CwtConfigType? {
			val fileKey = file.name.substringBefore('.')
			val parentProperty = element.parentOfType<CwtProperty>() ?: return null
			val parentName = parentProperty.name
			val parentParentProperty = parentProperty.parentProperty()
			val parentParentName = parentParentProperty?.name
			return when {
				parentName.surroundsWith("enum[", "]") -> EnumValue
				parentName.surroundsWith("value[", "]") -> ValueSetValue
				parentParentName == "scope_groups" && fileKey == "scopes" -> Scope
				else -> null
			}
		}
		
		private fun CwtProperty.parentProperty() = parentOfType<CwtProperty>()
	}
}

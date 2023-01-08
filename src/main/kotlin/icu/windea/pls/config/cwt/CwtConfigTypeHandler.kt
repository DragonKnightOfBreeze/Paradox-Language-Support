package icu.windea.pls.config.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

object CwtConfigTypeHandler {
	@JvmStatic
	fun get(element: PsiElement): CwtConfigType? {
		if(element !is CwtProperty && element !is CwtValue) return null
		return getFromCache(element)
	}
	
	private fun getFromCache(element: PsiElement): CwtConfigType? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedCwtConfigTypeKey) {
			val file = element.containingFile
			val value = when(element) {
				is CwtProperty -> resolve(element, file)
				is CwtValue -> resolve(element, file)
				else -> null
			}
			CachedValueProvider.Result.create(value, file) //invalidated on file modification
		}
	}
	
	private fun resolve(element: CwtProperty, file: PsiFile): CwtConfigType? {
		val fileKey = file.name.substringBefore('.')
		val configPath = element.configPath
		if(configPath == null || configPath.isEmpty()) return null
		val path = configPath.path
		return when {
			path.matchesAntPath("types/type[*]") -> {
				CwtConfigType.Type
			}
			path.matchesAntPath("types/type[*]/subtype[*]") -> {
				CwtConfigType.Subtype
			}
			path.matchesAntPath("types/type[*]/modifiers/**") -> {
				when {
					configPath.get(3).surroundsWith("subtype[", "]") -> {
						if(configPath.length == 5) return CwtConfigType.Modifier
					}
					else -> {
						if(configPath.length == 4) return CwtConfigType.Modifier
					}
				}
				null
			}
			path.matchesAntPath("enums/enum[*]") -> {
				CwtConfigType.Enum
			}
			path.matchesAntPath("enums/complex_enum[*]") -> {
				CwtConfigType.ComplexEnum
			}
			path.matchesAntPath("values/value[*]") -> {
				CwtConfigType.ValueSet
			}
			path.matchesAntPath("single_alias[*]") -> {
				CwtConfigType.SingleAlias
			}
			path.matchesAntPath("alias[*]") -> {
				val aliasName = configPath.get(0).substringIn('[', ']', "").substringBefore(':', "")
				when {
					aliasName == "modifier" -> return CwtConfigType.Modifier
					aliasName == "trigger" -> return CwtConfigType.Trigger
					aliasName == "effect" -> return CwtConfigType.Effect
				}
				CwtConfigType.Alias
			}
			fileKey == "links" && path.matchesAntPath("links/*") -> {
				CwtConfigType.Link
			}
			fileKey == "localisation" && path.matchesAntPath("localisation_links/*") -> {
				CwtConfigType.LocalisationLink
			}
			fileKey == "localisation" && path.matchesAntPath("localisation_commands/*") -> {
				CwtConfigType.LocalisationCommand
			}
			fileKey == "modifier_categories" && path.matchesAntPath("modifier_categories/*") -> {
				CwtConfigType.ModifierCategory
			}
			fileKey == "modifiers" && path.matchesAntPath("modifiers/*") -> {
				CwtConfigType.Modifier
			}
			fileKey == "scopes" && path.matchesAntPath("scopes/*") -> {
				CwtConfigType.Scope
			}
			fileKey == "scopes" && path.matchesAntPath("scope_groups/*") -> {
				CwtConfigType.ScopeGroup
			}
			fileKey == "system_scopes" && path.matchesAntPath("system_scopes/*") -> {
				CwtConfigType.SystemScope
			}
			fileKey == "localisation_locales" && path.matchesAntPath("localisation_locales/*") -> {
				CwtConfigType.LocalisationLocale
			}
			fileKey == "localisation_predefined_parameters" && path.matchesAntPath("localisation_predefined_parameters/*") -> {
				CwtConfigType.LocalisationPredefinedParameter
			}
			else -> null
		}
	}
	
	private fun resolve(element: CwtValue, file: PsiFile): CwtConfigType? {
		val fileKey = file.name.substringBefore('.')
		val configPath = element.configPath
		if(configPath == null || configPath.isEmpty()) return null
		val path = configPath.path
		return when {
			path.matchesAntPath("enums/enum[*]/*") -> CwtConfigType.EnumValue
			path.matchesAntPath("values/value[*]/*") -> CwtConfigType.ValueSetValue
			else -> null
		}
	}
}
package icu.windea.pls.config.util

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.path.*
import java.util.*

object CwtConfigManager {
    fun getContainingConfigGroup(element: PsiElement): CwtConfigGroup? {
        if(element.language != CwtLanguage) return null
        val file = element.containingFile ?: return null
        val vFile = file.virtualFile ?: return null
        val project = file.project
        return CwtConfigGroupFileProvider.EP_NAME.extensionList.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(vFile, project)
        }
    }
    
    fun getConfigPath(element: PsiElement): CwtConfigPath? {
        if(element is CwtFile) return CwtConfigPath.Empty
        if(element !is CwtMemberElement) return null
        return doGetConfigPathFromCache(element)
    }
    
    private fun doGetConfigPathFromCache(element: CwtMemberElement): CwtConfigPath? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigPath) {
            val value = doGetConfigPath(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun doGetConfigPath(element: CwtMemberElement): CwtConfigPath? {
        var current: PsiElement = element
        var depth = 0
        val subPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current is CwtProperty -> {
                    subPaths.addFirst(current.name)
                    depth++
                }
                current is CwtValue && current.isBlockValue() -> {
                    subPaths.addFirst("-")
                    depth++
                }
            }
            current = current.parent ?: break
        }
        if(current !is CwtFile) return null //unexpected
        return CwtConfigPath.resolve(subPaths.joinToString("/"))
    }
    
    fun getConfigType(element: PsiElement): CwtConfigType? {
        if(element !is CwtMemberElement) return null
        return doGetConfigTypeFromCache(element)
    }
    
    private fun doGetConfigTypeFromCache(element: CwtMemberElement): CwtConfigType? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigType) {
            val file = element.containingFile ?: return@getCachedValue null
            val value = when(element) {
                is CwtProperty -> doGetConfigType(element)
                is CwtValue -> doGetConfigType(element)
                else -> null
            }
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetConfigType(element: CwtMemberElement): CwtConfigType? {
        val configPath = element.configPath
        if(configPath == null || configPath.isEmpty()) return null
        val path = configPath.path
        return when {
            element is CwtProperty && path.matchesAntPath("types/type[*]") -> {
                CwtConfigType.Type
            }
            element is CwtProperty && path.matchesAntPath("types/type[*]/subtype[*]") -> {
                CwtConfigType.Subtype
            }
            element is CwtProperty && path.matchesAntPath("types/type[*]/modifiers/**") -> {
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
            element is CwtProperty && path.matchesAntPath("enums/enum[*]") -> {
                CwtConfigType.Enum
            }
            element is CwtValue && path.matchesAntPath("enums/enum[*]/*") -> {
                CwtConfigType.EnumValue
            }
            element is CwtProperty && path.matchesAntPath("enums/complex_enum[*]") -> {
                CwtConfigType.ComplexEnum
            }
            element is CwtProperty && path.matchesAntPath("values/value[*]") -> {
                CwtConfigType.DynamicValueType
            }
            element is CwtValue && path.matchesAntPath("values/value[*]/*") -> {
                CwtConfigType.DynamicValue
            }
            element is CwtProperty && path.matchesAntPath("inline[*]") -> {
                CwtConfigType.Inline
            }
            element is CwtProperty && path.matchesAntPath("single_alias[*]") -> {
                CwtConfigType.SingleAlias
            }
            element is CwtProperty && path.matchesAntPath("alias[*]") -> {
                val aliasName = configPath.get(0).substringIn('[', ']', "").substringBefore(':', "")
                when {
                    aliasName == "modifier" -> return CwtConfigType.Modifier
                    aliasName == "trigger" -> return CwtConfigType.Trigger
                    aliasName == "effect" -> return CwtConfigType.Effect
                }
                CwtConfigType.Alias
            }
            element is CwtProperty && path.matchesAntPath("links/*") -> {
                CwtConfigType.Link
            }
            element is CwtProperty && path.matchesAntPath("localisation_links/*") -> {
                CwtConfigType.LocalisationLink
            }
            element is CwtProperty && path.matchesAntPath("localisation_commands/*") -> {
                CwtConfigType.LocalisationCommand
            }
            element is CwtProperty && path.matchesAntPath("modifier_categories/*") -> {
                CwtConfigType.ModifierCategory
            }
            element is CwtProperty && path.matchesAntPath("modifiers/*") -> {
                CwtConfigType.Modifier
            }
            element is CwtProperty && path.matchesAntPath("scopes/*") -> {
                CwtConfigType.Scope
            }
            element is CwtProperty && path.matchesAntPath("scope_groups/*") -> {
                CwtConfigType.ScopeGroup
            }
            element is CwtProperty && path.matchesAntPath("system_links/*") -> {
                CwtConfigType.SystemLink
            }
            element is CwtProperty && path.matchesAntPath("localisation_locales/*") -> {
                CwtConfigType.LocalisationLocale
            }
            element is CwtProperty && path.matchesAntPath("localisation_predefined_parameters/*") -> {
                CwtConfigType.LocalisationPredefinedParameter
            }
            path.matchesAntPath("scripted_variables/*") -> {
                CwtConfigType.ExtendedScriptedVariable
            }
            path.matchesAntPath("definitions/*") -> {
                CwtConfigType.ExtendedDefinition
            }
            path.matchesAntPath("game_rules/*") -> {
                CwtConfigType.ExtendedGameRule
            }
            path.matchesAntPath("on_actions/*") -> {
                CwtConfigType.ExtendedOnAction
            }
            path.matchesAntPath("inline_scripts/*") -> {
                CwtConfigType.ExtendedInlineScript
            }
            path.matchesAntPath("parameters/*") -> {
                CwtConfigType.ExtendedParameter
            }
            path.matchesAntPath("complex_enum_values/*/*") -> {
                CwtConfigType.ExtendedComplexEnumValue
            }
            path.matchesAntPath("dynamic_values/*/*") -> {
                CwtConfigType.ExtendedDynamicValue
            }
            else -> null
        }
    }
    
    fun getConfigByPathExpression(configGroup: CwtConfigGroup, pathExpression: String): CwtMemberConfig<*>? {
        TODO()
    }
}
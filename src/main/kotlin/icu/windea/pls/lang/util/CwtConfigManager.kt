package icu.windea.pls.lang.util

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
import icu.windea.pls.model.*
import java.util.*

object CwtConfigManager {
    fun getContainingConfigGroup(element: PsiElement): CwtConfigGroup? {
        if(element.language != CwtLanguage) return null
        val file = element.containingFile ?: return null
        val vFile = file.virtualFile ?: return null
        val project = file.project
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        return fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(vFile, project)
        }
    }
    
    fun getConfigPath(element: PsiElement): CwtConfigPath? {
        if(element is CwtFile || element is CwtRootBlock) return CwtConfigPath.Empty
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
        //NOTE 1.3.18+ 通常情况下，规则路径不应当包含"/" "-"等必须考虑转义的特殊字符，目前也不支持这类情况
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
            element is CwtProperty && path.matchesAntPattern("types/type[*]") -> {
                CwtConfigType.Type
            }
            element is CwtProperty && path.matchesAntPattern("types/type[*]/subtype[*]") -> {
                CwtConfigType.Subtype
            }
            element is CwtProperty && path.matchesAntPattern("types/type[*]/modifiers/**") -> {
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
            element is CwtProperty && path.matchesAntPattern("enums/enum[*]") -> {
                CwtConfigType.Enum
            }
            element is CwtValue && path.matchesAntPattern("enums/enum[*]/*") -> {
                CwtConfigType.EnumValue
            }
            element is CwtProperty && path.matchesAntPattern("enums/complex_enum[*]") -> {
                CwtConfigType.ComplexEnum
            }
            element is CwtProperty && path.matchesAntPattern("values/value[*]") -> {
                CwtConfigType.DynamicValueType
            }
            element is CwtValue && path.matchesAntPattern("values/value[*]/*") -> {
                CwtConfigType.DynamicValue
            }
            element is CwtProperty && path.matchesAntPattern("inline[*]") -> {
                CwtConfigType.Inline
            }
            element is CwtProperty && path.matchesAntPattern("single_alias[*]") -> {
                CwtConfigType.SingleAlias
            }
            element is CwtProperty && path.matchesAntPattern("alias[*]") -> {
                val aliasName = configPath.get(0).substringIn('[', ']', "").substringBefore(':', "")
                when {
                    aliasName == "modifier" -> return CwtConfigType.Modifier
                    aliasName == "trigger" -> return CwtConfigType.Trigger
                    aliasName == "effect" -> return CwtConfigType.Effect
                }
                CwtConfigType.Alias
            }
            element is CwtProperty && path.matchesAntPattern("links/*") -> {
                CwtConfigType.Link
            }
            element is CwtProperty && path.matchesAntPattern("localisation_links/*") -> {
                CwtConfigType.LocalisationLink
            }
            element is CwtProperty && path.matchesAntPattern("localisation_commands/*") -> {
                CwtConfigType.LocalisationCommand
            }
            element is CwtProperty && path.matchesAntPattern("modifier_categories/*") -> {
                CwtConfigType.ModifierCategory
            }
            element is CwtProperty && path.matchesAntPattern("modifiers/*") -> {
                CwtConfigType.Modifier
            }
            element is CwtProperty && path.matchesAntPattern("scopes/*") -> {
                CwtConfigType.Scope
            }
            element is CwtProperty && path.matchesAntPattern("scope_groups/*") -> {
                CwtConfigType.ScopeGroup
            }
            element is CwtProperty && path.matchesAntPattern("database_object_types/*") -> {
                CwtConfigType.DatabaseObjectType
            }
            element is CwtProperty && path.matchesAntPattern("system_scopes/*") -> {
                CwtConfigType.SystemScope
            }
            element is CwtProperty && path.matchesAntPattern("localisation_locales/*") -> {
                CwtConfigType.LocalisationLocale
            }
            element is CwtProperty && path.matchesAntPattern("localisation_predefined_parameters/*") -> {
                CwtConfigType.LocalisationPredefinedParameter
            }
            path.matchesAntPattern("scripted_variables/*") -> {
                CwtConfigType.ExtendedScriptedVariable
            }
            path.matchesAntPattern("definitions/*") -> {
                CwtConfigType.ExtendedDefinition
            }
            path.matchesAntPattern("game_rules/*") -> {
                CwtConfigType.ExtendedGameRule
            }
            path.matchesAntPattern("on_actions/*") -> {
                CwtConfigType.ExtendedOnAction
            }
            path.matchesAntPattern("inline_scripts/*") -> {
                CwtConfigType.ExtendedInlineScript
            }
            path.matchesAntPattern("parameters/*") -> {
                CwtConfigType.ExtendedParameter
            }
            path.matchesAntPattern("complex_enum_values/*/*") -> {
                CwtConfigType.ExtendedComplexEnumValue
            }
            path.matchesAntPattern("dynamic_values/*/*") -> {
                CwtConfigType.ExtendedDynamicValue
            }
            else -> null
        }
    }
    
    fun getConfigByPathExpression(configGroup: CwtConfigGroup, pathExpression: String): List<CwtMemberConfig<*>> {
        val separatorIndex = pathExpression.indexOf('#')
        if(separatorIndex == -1) return emptyList()
        val filePath = pathExpression.substring(0, separatorIndex)
        if(filePath.isEmpty()) return emptyList()
        val fileConfig = configGroup.files[filePath] ?: return emptyList()
        val configPath = pathExpression.substring(separatorIndex + 1)
        if(configPath.isEmpty()) return emptyList()
        val pathList = configPath.split('/')
        var r: List<CwtMemberConfig<*>> = emptyList()
        pathList.forEach { p ->
            if(p == "-") {
                if(r.isEmpty()) {
                    r = fileConfig.values
                } else {
                    r = buildList {
                        r.forEach { c1 ->
                            c1.configs?.forEach { c2 ->
                                if(c2 is CwtValueConfig) this += c2
                            }
                        }
                    }
                }
            } else {
                if(r.isEmpty()) {
                    r = fileConfig.properties.filter { c -> c.key == p }
                } else {
                    r = buildList {
                        r.forEach { c1 ->
                            c1.configs?.forEach { c2 ->
                                if(c2 is CwtPropertyConfig && c2.key == p) this += c2
                            }
                        }
                    }
                }
            }
            if(r.isEmpty()) return emptyList()
        }
        return r
    }
}

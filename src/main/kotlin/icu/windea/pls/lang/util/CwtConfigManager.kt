package icu.windea.pls.lang.util

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.internal.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.util.*

object CwtConfigManager {
    /**
     * @param forRepo 是否兼容插件或者规则仓库中的CWT文件（此时将其视为规则文件）。
     */
    fun getContainingConfigGroup(element: PsiElement, forRepo: Boolean = false): CwtConfigGroup? {
        if(element.language != CwtLanguage) return null
        val file = element.containingFile ?: return null
        val vFile = file.virtualFile ?: return null
        val project = file.project
        
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroup = fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(vFile, project)
        }
        if(configGroup != null) return configGroup
        
        runCatchingCancelable r@{
            if(!forRepo) return@r
            val workDirectory = vFile.toNioPath().toFile().parentFile ?: return@r
            val command = "git remote -v"
            val commandResult = executeCommand(command, CommandType.POWER_SHELL, null, workDirectory)
            val gameTypeId = commandResult.lines()
                .mapNotNull { it.splitByBlank(3).getOrNull(1) }
                .firstNotNullOfOrNull t@{
                    if(it.contains("Paradox-Language-Support")) return@t "core"
                    val s = it.substringInLast("cwtools-", "-config", "")
                    if(s.isNotEmpty()) return@t s
                    null
                } ?: return@r
            val gameType = ParadoxGameType.resolve(gameTypeId)
            return getConfigGroup(project, gameType)
        }
        
        return null
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
        return CwtConfigPath.resolve(subPaths)
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
    
    fun getContextConfigs(element: PsiElement, containerElement: PsiElement, schema: CwtSchemaConfig): List<CwtMemberConfig<*>> {
        val configPath = getConfigPath(containerElement) ?: return emptyList()
        
        var contextConfigs = mutableListOf<CwtMemberConfig<*>>()
        contextConfigs += schema.properties
        configPath.forEachIndexed f1@{ i, path ->
            val flatten = i != configPath.length - 1 || !(element is CwtString && element.isPropertyValue())
            val nextContextConfigs = mutableListOf<CwtMemberConfig<*>>()
            contextConfigs.forEach f2@{ config ->
                when(config) {
                    is CwtPropertyConfig -> {
                        val schemaExpression = CwtSchemaExpression.resolve(config.key)
                        if(!matchesSchemaExpression(path, schemaExpression, schema)) return@f2
                        nextContextConfigs += config
                    }
                    is CwtValueConfig -> {
                        if(path != "-") return@f2
                        nextContextConfigs += config
                    }
                }
            }
            contextConfigs = nextContextConfigs
            if(flatten) contextConfigs = contextConfigs.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
        }
        return contextConfigs
    }
    
    fun matchesSchemaExpression(value: String, schemaExpression: CwtSchemaExpression, schema: CwtSchemaConfig): Boolean {
        return when(schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                schemaExpression.expressionString == value
            }
            is CwtSchemaExpression.Enum -> {
                schema.enums[schemaExpression.name]?.values?.any { it.stringValue == value } ?: false
            }
            is CwtSchemaExpression.Template -> {
                value.matchesPattern(schemaExpression.pattern)
            }
            is CwtSchemaExpression.Type -> {
                true //fast check
            }
            is CwtSchemaExpression.Constraint -> {
                false //fast check 
            }
        }
    }
}

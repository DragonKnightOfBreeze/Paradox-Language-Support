package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import java.util.*

object CwtConfigHandler {
    //region Config Path Methods
    val cachedCwtConfigPathKey = Key.create<CachedValue<CwtConfigPath>>("paradox.cached.cwtConfigPath")
    
    fun get(element: PsiElement): CwtConfigPath? {
        if(element is CwtFile) return EmptyCwtConfigPath
        if(element !is CwtProperty && element !is CwtValue) return null
        return getFromCache(element)
    }
    
    private fun getFromCache(element: PsiElement): CwtConfigPath? {
        return CachedValuesManager.getCachedValue(element, cachedCwtConfigPathKey) {
            val value = resolve(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun resolve(element: PsiElement): CwtConfigPath? {
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
    //endregion
    
    //region Config Type Methods
    val cachedCwtConfigTypeKey = Key.create<CachedValue<CwtConfigType>>("paradox.cached.cwtConfigType")
    
    fun getConfigType(element: PsiElement): CwtConfigType? {
        if(element !is CwtProperty && element !is CwtValue) return null
        return getConfigTypeFromCache(element)
    }
    
    private fun getConfigTypeFromCache(element: PsiElement): CwtConfigType? {
        return CachedValuesManager.getCachedValue(element, cachedCwtConfigTypeKey) {
            val file = element.containingFile
            val value = when(element) {
                is CwtProperty -> doGetConfigType(element, file)
                is CwtValue -> resolve(element, file)
                else -> null
            }
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetConfigType(element: CwtProperty, file: PsiFile): CwtConfigType? {
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
            fileKey == "on_actions" && path.matchesAntPath("on_actions/*") -> {
                CwtConfigType.OnAction
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
            fileKey == "system_links" && path.matchesAntPath("system_links/*") -> {
                CwtConfigType.SystemLink
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
        //val fileKey = file.name.substringBefore('.')
        val configPath = element.configPath
        if(configPath == null || configPath.isEmpty()) return null
        val path = configPath.path
        return when {
            path.matchesAntPath("enums/enum[*]/*") -> CwtConfigType.EnumValue
            path.matchesAntPath("values/value[*]/*") -> CwtConfigType.ValueSetValue
            else -> null
        }
    }
    //endregion
    
    //region Common Methods
    fun mergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        if(config.expression == otherConfig.expression) return config
        return doMergeValueConfig(config, otherConfig) ?: doMergeValueConfig(otherConfig, config)
    }
    
    fun doMergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        val e1 = config.expression
        val e2 = otherConfig.expression
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            CwtDataType.Int -> {
                when(t2) {
                    CwtDataType.Int, CwtDataType.Float, CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> CwtValueConfig.resolve(config.pointer, config.info, "int")
                    else -> null
                }
            }
            CwtDataType.Float -> {
                when(t2) {
                    CwtDataType.Float, CwtDataType.ValueField, CwtDataType.VariableField -> CwtValueConfig.resolve(config.pointer, config.info, "float")
                    else -> null
                }
            }
            CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
                when {
                    t2 == CwtDataType.ScopeField -> CwtValueConfig.resolve(config.pointer, config.info, config.value)
                    t2 == CwtDataType.Scope && e2.value == "any" -> CwtValueConfig.resolve(config.pointer, config.info, config.value)
                    else -> null
                }
            }
            CwtDataType.Value -> {
                if(t2 == CwtDataType.ValueSet && e1.value == e2.value) return CwtValueConfig.resolve(config.pointer, config.info, "value_set[${e1.value}]")
                null
            }
            CwtDataType.ValueSet -> {
                if(t2 == CwtDataType.Value && e1.value == e2.value) return CwtValueConfig.resolve(config.pointer, config.info, "value_set[${e1.value}]")
                null
            }
            CwtDataType.VariableField -> {
                when(t2) {
                    CwtDataType.VariableField, CwtDataType.ValueField -> CwtValueConfig.resolve(config.pointer, config.info, "variable_field")
                    else -> null
                }
            }
            CwtDataType.IntVariableField -> {
                when(t2) {
                    CwtDataType.IntVariableField, CwtDataType.ValueField, CwtDataType.IntValueField -> CwtValueConfig.resolve(config.pointer, config.info, "int_variable_field")
                    else -> null
                }
            }
            CwtDataType.IntValueField -> {
                when(t2) {
                    CwtDataType.ValueField, CwtDataType.IntValueField -> CwtValueConfig.resolve(config.pointer, config.info, "int_value_field")
                    else -> null
                }
            }
            else -> null
        }
    }
    //endregion
}
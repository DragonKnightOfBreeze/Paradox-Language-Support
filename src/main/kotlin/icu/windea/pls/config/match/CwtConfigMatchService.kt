package icu.windea.pls.config.match

import com.intellij.util.Processor
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.extended.CwtExtendedParameterConfig
import icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processValue
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier

object CwtConfigMatchService {
    inline fun <reified T : CwtIdMatchableConfig<*>> processMatchedConfigsById(id: String?, configGroup: CwtConfigGroup, processor: Processor<T>): Boolean {
        return processMatchedConfigsById(id, configGroup, T::class.java, processor)
    }

    fun <T : CwtIdMatchableConfig<*>> processMatchedConfigsById(id: String?, configGroup: CwtConfigGroup, type: Class<T>, processor: Processor<T>): Boolean {
        val id = id?.trim()?.orNull()
        return when (type) {
            CwtAliasConfig::class.java -> {
                val source = configGroup.aliasGroups
                val aliasName = id?.substringBefore(':')?.trim()?.orNull()
                val aliasSubName = id?.substringAfter(':')?.trim()?.orNull()
                val aliasSubNameMayBeConst = aliasSubName == null || aliasSubName.isIdentifier()
                source.processValue(aliasName) { v1 ->
                    if (aliasSubNameMayBeConst) {
                        // may-be-const alias sub name should ignore case here (but such collection is not a case-insensitive collection)
                        v1.values.process { v2 ->
                            v2.process { if (aliasSubName == null || aliasSubName.equals(it.subName, true)) processor.process(it.cast()) else true }
                        }
                    } else {
                        // non-const alias sub name do not ignore case
                        v1.processValue(aliasSubName) { v2 ->
                            v2.process { processor.process(it.cast()) }
                        }
                    }
                }
            }
            CwtComplexEnumConfig::class.java -> {
                val source = configGroup.complexEnums
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtDatabaseObjectTypeConfig::class.java -> {
                val source = configGroup.databaseObjectTypes
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtDeclarationConfig::class.java -> {
                val source = configGroup.declarations
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtDirectiveConfig::class.java -> {
                val source = configGroup.directives.orNull() ?: return true
                source.process { if (it.name == id) processor.process(it.cast()) else true }
            }
            CwtDynamicValueTypeConfig::class.java -> {
                val source = configGroup.dynamicValueTypes
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtEnumConfig::class.java -> {
                val source = configGroup.enums
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtLinkConfig::class.java -> {
                val source = configGroup.links
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtLocaleConfig::class.java -> {
                val source = configGroup.localisationLocalesById
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtLocalisationCommandConfig -> {
                val source = configGroup.localisationCommands
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtLocalisationPromotionConfig::class.java -> {
                val source = configGroup.localisationPromotions
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtModifierCategoryConfig::class.java -> {
                val source = configGroup.modifierCategories
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtModifierConfig::class.java -> {
                val source = configGroup.modifiers
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtRowConfig::class.java -> {
                val source = configGroup.rows
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtScopeConfig::class.java -> {
                val source = configGroup.scopes
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtScopeGroupConfig::class.java -> {
                val source = configGroup.scopeGroups
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtSingleAliasConfig::class.java -> {
                val source = configGroup.singleAliases
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtSystemScopeConfig::class.java -> {
                val source = configGroup.systemScopes
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtTypeConfig::class.java -> {
                val source = configGroup.types
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtExtendedComplexEnumValueConfig::class.java -> {
                val source = configGroup.extendedComplexEnumValues
                val enumName = id?.substringBefore(':')?.trim()?.orNull()
                val name = id?.substringAfter(':')?.trim()?.orNull()
                source.processValue(enumName) { v1 ->
                    v1.processValue(name) {
                        processor.process(it.cast())
                    }
                }
            }
            CwtExtendedDefinitionConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedDefinitions
                source.processValue(id) { v -> v.process { processor.process(it.cast()) } }
            }
            CwtExtendedDynamicValueConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedDynamicValues
                val dynamicValueType = id?.substringBefore(':')?.trim()?.orNull()
                val name = id?.substringAfter(':')?.trim()?.orNull()
                source.processValue(dynamicValueType) { v1 ->
                    v1.processValue(name) {
                        processor.process(it.cast())
                    }
                }
            }
            CwtExtendedGameRuleConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedGameRules
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtExtendedInlineScriptConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedInlineScripts
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtExtendedOnActionConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedOnActions
                source.processValue(id) { processor.process(it.cast()) }
            }
            CwtExtendedParameterConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedParameters
                source.processValue(id) { v -> v.process { processor.process(it.cast()) } }
            }
            CwtExtendedScriptedVariableConfig::class.java -> {
                // TODO 2.1.6+ pattern match
                val source = configGroup.extendedScriptedVariables
                source.processValue(id) { processor.process(it.cast()) }
            }
            else -> throw UnsupportedOperationException()
        }
    }


    inline fun <reified T : CwtFilePathMatchableConfig<*>> processMatchedConfigsByFilePath(filePath: String?, configGroup: CwtConfigGroup, processor: Processor<T>): Boolean {
        return processMatchedConfigsByFilePath(filePath, configGroup, T::class.java, processor)
    }

    fun <T : CwtFilePathMatchableConfig<*>> processMatchedConfigsByFilePath(filePath: String?, configGroup: CwtConfigGroup, type: Class<T>, processor: Processor<T>): Boolean {
        return when (type) {
            CwtComplexEnumConfig::class.java -> {
                val source = configGroup.complexEnums
                source.values.process { if (matchesByFilePath(it, filePath)) processor.process(it.cast()) else true }
                true
            }
            CwtRowConfig::class.java -> {
                val source = configGroup.rows
                source.values.process { if (matchesByFilePath(it, filePath)) processor.process(it.cast()) else true }
            }
            CwtTypeConfig::class.java -> {
                val source = configGroup.types
                source.values.process { if (matchesByFilePath(it, filePath)) processor.process(it.cast()) else true }
            }
            else -> throw UnsupportedOperationException()
        }
    }

    fun matchesByFilePath(config: CwtFilePathMatchableConfig<*>, filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return true
        return config.filePathPatterns.any { pattern -> filePath.matchesAntPattern(pattern) }
    }
}

package icu.windea.pls.ep.configGroup

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
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
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * 用于初始化规则分组中基于文件内容的那些数据。
 */
class CwtFileBasedConfigGroupDataProvider : CwtConfigGroupDataProvider {
    private val logger = thisLogger()

    override suspend fun process(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup) {
        val currentCoroutineContext = currentCoroutineContext()

        // 按照文件路径（相对于规则分组的根目录）正序读取所有规则文件
        // 后加入的规则文件会覆盖先加入的同路径的规则文件
        // 后加入的数据项会覆盖先加入的同名同类型的数据项

        currentCoroutineContext.ensureActive()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvidersAndRootDirectories = mutableMapOf<CwtConfigGroupFileProvider, VirtualFile>()
        readAction {
            fileProviders.forEach f@{ fileProvider ->
                currentCoroutineContext.ensureActive()
                val rootDirectory = fileProvider.getRootDirectory(configGroup.project) ?: return@f
                fileProvidersAndRootDirectories[fileProvider] = rootDirectory
            }
        }

        currentCoroutineContext.ensureActive()
        val allInternalFiles = mutableMapOf<String, VirtualFile>()
        val allFiles = mutableMapOf<String, VirtualFile>()
        readAction {
            fileProvidersAndRootDirectories.all { (fileProvider, rootDirectory) ->
                currentCoroutineContext.ensureActive()
                fileProvider.processFiles(configGroup, rootDirectory) p@{ filePath, file ->
                    if (filePath.startsWith("internal/")) {
                        if (fileProvider.type != CwtConfigGroupFileProvider.Type.BuiltIn) return@p true // 不允许覆盖内部规则文件
                        allInternalFiles.putIfAbsent(filePath, file)
                        return@p true
                    }
                    allFiles[filePath] = file
                    true
                }
            }
        }
        readAction {
            try {
                allInternalFiles.forEach f@{ (filePath, file) ->
                    currentCoroutineContext.ensureActive()
                    CwtConfigResolverUtil.setLocation(filePath, configGroup)
                    resolveAndProcessInternalFile(configGroup, file, filePath)
                }
                allFiles.forEach f@{ (filePath, file) ->
                    currentCoroutineContext.ensureActive()
                    CwtConfigResolverUtil.setLocation(filePath, configGroup)
                    resolveAndProcessFile(configGroup, file, filePath)
                }
            } finally {
                CwtConfigResolverUtil.resetLocation()
            }
        }
    }

    private fun resolveAndProcessInternalFile(configGroup: CwtConfigGroup, file: VirtualFile, filePath: String) {
        val psiFile = runCatchingCancelable { file.toPsiFile(configGroup.project) }
            .onFailure { logger.warn(it) }
            .getOrNull()
        if (psiFile !is CwtFile) return
        val fileConfig = CwtFileConfig.resolve(psiFile, configGroup, filePath)
        processInternalFile(fileConfig, filePath)
    }

    private fun resolveAndProcessFile(configGroup: CwtConfigGroup, file: VirtualFile, filePath: String) {
        val psiFile = runCatchingCancelable { file.toPsiFile(configGroup.project) }
            .onFailure { logger.warn(it) }
            .getOrNull()
        if (psiFile !is CwtFile) return
        val fileConfig = CwtFileConfig.resolve(psiFile, configGroup, filePath)
        processFile(fileConfig)
    }

    private fun processInternalFile(fileConfig: CwtFileConfig, filePath: String) {
        when (filePath) {
            "internal/schema.cwt" -> CwtSchemaConfig.resolveInFile(fileConfig)
            "internal/folding_settings.cwt" -> CwtFoldingSettingsConfig.resolveInFile(fileConfig)
            "internal/postfix_template_settings.cwt" -> CwtPostfixTemplateSettingsConfig.resolveInFile(fileConfig)
        }
    }

    private fun processFile(fileConfig: CwtFileConfig) {
        val initializer = fileConfig.configGroup.initializer
        for (property in fileConfig.properties) {
            val key = property.key
            when {
                key == "priorities" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val k = config.key.optimizedPath().orNull() ?: continue
                        val v = config.stringValue?.orNull()?.let { ParadoxOverrideStrategy.get(it.uppercase()) } ?: continue
                        initializer.priorities[k] = v
                    }
                }
                key == "system_scopes" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val systemScopeConfig = CwtSystemScopeConfig.resolve(config)
                        initializer.systemScopes[systemScopeConfig.id] = systemScopeConfig
                    }
                }
                key == "locales" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localeConfig = CwtLocaleConfig.resolve(config)
                        initializer.localisationLocalesById[localeConfig.id] = localeConfig
                        localeConfig.codes.forEach { code ->
                            initializer.localisationLocalesByCode[code] = localeConfig
                        }
                    }
                }
                key == "types" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val typeConfig = CwtTypeConfig.resolve(config) ?: continue
                        initializer.types[typeConfig.name] = typeConfig
                    }
                }
                key == "rows" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val rowConfig = CwtRowConfig.resolve(config) ?: continue
                        initializer.rows[rowConfig.name] = rowConfig
                    }
                }
                key == "enums" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        run {
                            val enumConfig = CwtEnumConfig.resolve(config) ?: return@run
                            initializer.enums[enumConfig.name] = enumConfig
                        }
                        run {
                            val complexEnumConfig = CwtComplexEnumConfig.resolve(config) ?: return@run
                            initializer.complexEnums[complexEnumConfig.name] = complexEnumConfig
                        }
                    }
                }
                key == "values" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val dynamicValueTypeConfig = CwtDynamicValueTypeConfig.resolve(config) ?: continue
                        initializer.dynamicValueTypes[dynamicValueTypeConfig.name] = dynamicValueTypeConfig
                    }
                }
                key == "links" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val linkConfig = CwtLinkConfig.resolve(config) ?: continue
                        initializer.links[linkConfig.name] = linkConfig
                    }
                }
                key == "localisation_links" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val linkConfig = CwtLinkConfig.resolveForLocalisation(config) ?: continue
                        initializer.localisationLinks[linkConfig.name] = linkConfig
                    }
                }
                key == "localisation_promotions" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localisationPromotionConfig = CwtLocalisationPromotionConfig.resolve(config)
                        initializer.localisationPromotions[localisationPromotionConfig.name] = localisationPromotionConfig
                    }
                }
                key == "localisation_commands" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localisationCommandConfig = CwtLocalisationCommandConfig.resolve(config)
                        initializer.localisationCommands[localisationCommandConfig.name] = localisationCommandConfig
                    }
                }
                key == "modifier_categories" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val modifierCategoryConfig = CwtModifierCategoryConfig.resolve(config) ?: continue
                        initializer.modifierCategories[modifierCategoryConfig.name] = modifierCategoryConfig
                    }
                }
                key == "modifiers" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val modifierName = config.key
                        val modifierConfig = CwtModifierConfig.resolve(config, modifierName) ?: continue
                        initializer.modifiers[modifierName] = modifierConfig
                    }
                }
                key == "scopes" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val scopeConfig = CwtScopeConfig.resolve(config) ?: continue
                        initializer.scopes[scopeConfig.name] = scopeConfig
                        for (alias in scopeConfig.aliases) {
                            initializer.scopeAliasMap[alias] = scopeConfig
                        }
                    }
                }
                key == "scope_groups" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val scopeGroupConfig = CwtScopeGroupConfig.resolve(config) ?: continue
                        initializer.scopeGroups[scopeGroupConfig.name] = scopeGroupConfig
                    }
                }
                key == "database_object_types" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val databaseObjectTypeConfig = CwtDatabaseObjectTypeConfig.resolve(config) ?: continue
                        initializer.databaseObjectTypes[databaseObjectTypeConfig.name] = databaseObjectTypeConfig
                    }
                }
                key == "scripted_variables" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val scriptedVariableConfig = CwtExtendedScriptedVariableConfig.resolve(config) ?: continue
                        initializer.extendedScriptedVariables[scriptedVariableConfig.name] = scriptedVariableConfig
                    }
                }
                key == "definitions" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val definitionConfig = CwtExtendedDefinitionConfig.resolve(config) ?: continue
                        // 使用 fast 列表作为默认值
                        initializer.extendedDefinitions.computeIfAbsent(definitionConfig.name) { FastList() } += definitionConfig
                    }
                }
                key == "game_rules" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val gameRuleConfig = CwtExtendedGameRuleConfig.resolve(config)
                        initializer.extendedGameRules[gameRuleConfig.name] = gameRuleConfig
                    }
                }
                key == "on_actions" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val onActionConfig = CwtExtendedOnActionConfig.resolve(config) ?: continue
                        initializer.extendedOnActions[onActionConfig.name] = onActionConfig
                    }
                }
                key == "inline_scripts" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val inlineScriptConfig = CwtExtendedInlineScriptConfig.resolve(config)
                        initializer.extendedInlineScripts[inlineScriptConfig.name] = inlineScriptConfig
                    }
                }
                key == "parameters" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val parameterConfig = CwtExtendedParameterConfig.resolve(config) ?: continue
                        initializer.extendedParameters.computeIfAbsent(parameterConfig.name) { FastList() } += parameterConfig
                    }
                }
                key == "complex_enum_values" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        if (config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for (config1 in configs1) {
                            val complexEnumValueConfig = CwtExtendedComplexEnumValueConfig.resolve(config1, type)
                            initializer.extendedComplexEnumValues.computeIfAbsent(type) { FastMap() }[complexEnumValueConfig.name] = complexEnumValueConfig
                        }
                    }
                }
                key == "dynamic_values" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        if (config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for (config1 in configs1) {
                            val dynamicValueConfig = CwtExtendedDynamicValueConfig.resolve(config1, type)
                            initializer.extendedDynamicValues.computeIfAbsent(type) { FastMap() }[dynamicValueConfig.name] = dynamicValueConfig
                        }
                    }
                }
                else -> {
                    run {
                        val singleAliasConfig = CwtSingleAliasConfig.resolve(property) ?: return@run
                        if (CwtConfigManager.isRemoved(singleAliasConfig)) return@run
                        initializer.singleAliases[singleAliasConfig.name] = singleAliasConfig
                    }
                    run {
                        val aliasConfig = CwtAliasConfig.resolve(property) ?: return@run
                        if (CwtConfigManager.isRemoved(aliasConfig)) return@run
                        CwtAliasConfig.postProcess(aliasConfig)
                        initializer.aliasGroups.computeIfAbsent(aliasConfig.name) { FastMap() }.computeIfAbsent(aliasConfig.subName) { FastList() } += aliasConfig
                    }
                    run {
                        val inlineConfig = CwtInlineConfig.resolve(property) ?: return@run
                        initializer.inlineConfigGroup.computeIfAbsent(inlineConfig.name) { FastList() } += inlineConfig
                    }
                    run {
                        val declarationConfig = CwtDeclarationConfig.resolve(property) ?: return@run
                        initializer.declarations[key] = declarationConfig
                    }
                }
            }
        }
    }

    override suspend fun postOptimize(configGroup: CwtConfigGroup) {
        // 2.0.7 nothing now (since it's not very necessary)
    }
}

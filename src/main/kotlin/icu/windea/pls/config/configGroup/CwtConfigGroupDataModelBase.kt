package icu.windea.pls.config.configGroup

import icu.windea.pls.config.attributes.CwtConfigGroupAttributesBase
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributes
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributesEvaluator
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.extended.CwtExtendedParameterConfig
import icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

open class CwtConfigGroupDataModelBase : CwtConfigGroupDataModel {
    final override val fileConfigs: Object2ObjectLinkedOpenHashMap<String, CwtFileConfig> = Object2ObjectLinkedOpenHashMap()
    final override val configPostProcessActions: ObjectArrayList<Runnable> = ObjectArrayList()
    final override val schemas: ObjectArrayList<CwtSchemaConfig> = ObjectArrayList()
    final override val foldingSettings: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtFoldingSettingsConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val postfixTemplateSettings: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val priorities: Object2ObjectLinkedOpenHashMap<String, ParadoxOverrideStrategy> = Object2ObjectLinkedOpenHashMap()
    final override val systemScopes: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtSystemScopeConfig> = CaseInsensitiveStringKeyMap()
    final override val locales: Object2ObjectLinkedOpenHashMap<String, CwtLocaleConfig> = Object2ObjectLinkedOpenHashMap()
    final override val types: Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig> = Object2ObjectLinkedOpenHashMap()
    final override val swappedTypes: Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig> = Object2ObjectLinkedOpenHashMap()
    final override val type2ModifiersMap: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtModifierConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val declarations: Object2ObjectLinkedOpenHashMap<String, CwtDeclarationConfig> = Object2ObjectLinkedOpenHashMap()
    final override val rows: Object2ObjectLinkedOpenHashMap<String, CwtRowConfig> = Object2ObjectLinkedOpenHashMap()
    final override val defineNamespaces: Object2ObjectLinkedOpenHashMap<String, CwtDefineNamespaceConfig> = Object2ObjectLinkedOpenHashMap()
    final override val enums: Object2ObjectLinkedOpenHashMap<String, CwtEnumConfig> = Object2ObjectLinkedOpenHashMap()
    final override val complexEnums: Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig> = Object2ObjectLinkedOpenHashMap()
    final override val complexEnumsFromColumns: Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig> = Object2ObjectLinkedOpenHashMap()
    final override val unions: Object2ObjectLinkedOpenHashMap<String, CwtUnionConfig> = Object2ObjectLinkedOpenHashMap()
    final override val dynamicValueTypes: Object2ObjectLinkedOpenHashMap<String, CwtDynamicValueTypeConfig> = Object2ObjectLinkedOpenHashMap()
    final override val links: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig> = CaseInsensitiveStringKeyMap()
    final override val localisationLinks: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig> = CaseInsensitiveStringKeyMap()
    final override val localisationCommands: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationCommandConfig> = CaseInsensitiveStringKeyMap()
    final override val localisationPromotions: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationPromotionConfig> = CaseInsensitiveStringKeyMap()
    final override val scopes: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig> = CaseInsensitiveStringKeyMap()
    final override val scopeAliasMap: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig> = CaseInsensitiveStringKeyMap()
    final override val scopeGroups: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeGroupConfig> = CaseInsensitiveStringKeyMap()
    final override val aliasGroups: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtAliasConfig>>> = Object2ObjectLinkedOpenHashMap()
    final override val singleAliases: Object2ObjectLinkedOpenHashMap<String, CwtSingleAliasConfig> = Object2ObjectLinkedOpenHashMap()
    final override val modifierCategories: Object2ObjectLinkedOpenHashMap<String, CwtModifierCategoryConfig> = Object2ObjectLinkedOpenHashMap()
    final override val modifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> = CaseInsensitiveStringKeyMap()
    final override val databaseObjectTypes: Object2ObjectLinkedOpenHashMap<String, CwtDatabaseObjectTypeConfig> = Object2ObjectLinkedOpenHashMap()
    final override val macros: ObjectArrayList<CwtMacroConfig> = ObjectArrayList()
    final override val extendedScriptedVariables: Object2ObjectLinkedOpenHashMap<String, CwtExtendedScriptedVariableConfig> = Object2ObjectLinkedOpenHashMap()
    final override val extendedDefinitions: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedDefinitionConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val extendedGameRules: Object2ObjectLinkedOpenHashMap<String, CwtExtendedGameRuleConfig> = Object2ObjectLinkedOpenHashMap()
    final override val extendedOnActions: Object2ObjectLinkedOpenHashMap<String, CwtExtendedOnActionConfig> = Object2ObjectLinkedOpenHashMap()
    final override val extendedParameters: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedParameterConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val extendedComplexEnumValues: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedComplexEnumValueConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val extendedDynamicValues: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedDynamicValueConfig>> = Object2ObjectLinkedOpenHashMap()
    final override val extendedInlineScripts: Object2ObjectLinkedOpenHashMap<String, CwtExtendedInlineScriptConfig> = Object2ObjectLinkedOpenHashMap()
    final override val globalLocales: ObjectArrayList<CwtLocaleConfig> = ObjectArrayList()
    final override val supportedLocales: ObjectArrayList<CwtLocaleConfig> = ObjectArrayList()
    final override val predefinedModifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> = CaseInsensitiveStringKeyMap()
    final override val generatedModifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> = CaseInsensitiveStringKeyMap()
    final override val aliasKeysGroupConst: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, String>> = CaseInsensitiveStringKeyMap()
    final override val aliasKeysGroupNoConst: Object2ObjectLinkedOpenHashMap<String, ObjectLinkedOpenHashSet<String>> = Object2ObjectLinkedOpenHashMap()
    final override val aliasNamesSupportScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    final override val relatedLocalisationPatterns: ObjectLinkedOpenHashSet<Tuple2<String, String>> = ObjectLinkedOpenHashSet()
    final override val typesModel: CwtTypesModelBase = CwtTypesModelBase()
    final override val linksModel: CwtLinksModelBase = CwtLinksModelBase()
    final override val localisationLinksModel: CwtLinksModelBase = CwtLinksModelBase()
    final override val macrosModel: CwtMacrosModelBase = CwtMacrosModelBase()
    final override val attribute: CwtConfigGroupAttributesBase = CwtConfigGroupAttributesBase()

    val unionAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> = Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>()
    val aliasAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> = Object2ObjectLinkedOpenHashMap()
    val singleAliasAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> = Object2ObjectLinkedOpenHashMap()

    override fun getUnionAttribute(name: String): CwtExpandableConfigAttributes {
        return unionAttributes.getOrPut(name) {
            val config = unions[name] ?: return CwtExpandableConfigAttributes.EMPTY
            val configGroup = config.configGroup
            CwtExpandableConfigAttributesEvaluator().evaluate(name, config, configGroup)
        }
    }

    override fun getAliasAttribute(name: String): CwtExpandableConfigAttributes {
        return aliasAttributes.getOrPut(name) {
            val configs = aliasGroups[name]?.values ?: return CwtExpandableConfigAttributes.EMPTY
            if (configs.isEmpty()) return CwtExpandableConfigAttributes.EMPTY
            val configGroup = configs.firstOrNull()?.firstOrNull()?.configGroup ?: return CwtExpandableConfigAttributes.EMPTY
            CwtExpandableConfigAttributesEvaluator().evaluate(name, configs, configGroup)
        }
    }

    override fun getSingleAliasAttribute(name: String): CwtExpandableConfigAttributes {
        return singleAliasAttributes.getOrPut(name) {
            val config = singleAliases[name] ?: return CwtExpandableConfigAttributes.EMPTY
            val configGroup = config.configGroup
            CwtExpandableConfigAttributesEvaluator().evaluate(name, config, configGroup)
        }
    }

    fun trim() {
        schemas.trim()
        foldingSettings.trim()
        foldingSettings.values.forEach { it.trim() }
        postfixTemplateSettings.trim()
        postfixTemplateSettings.values.forEach { it.trim() }
        priorities.trim()
        systemScopes.trim()
        locales.trim()
        types.trim()
        swappedTypes.trim()
        type2ModifiersMap.trim()
        type2ModifiersMap.values.forEach { it.trim() }
        declarations.trim()
        rows.trim()
        defineNamespaces.trim()
        enums.trim()
        complexEnums.trim()
        complexEnumsFromColumns.trim()
        unions.trim()
        dynamicValueTypes.trim()
        links.trim()
        localisationLinks.trim()
        localisationCommands.trim()
        localisationPromotions.trim()
        scopes.trim()
        scopeAliasMap.trim()
        scopeGroups.trim()
        aliasGroups.trim()
        aliasGroups.values.forEach { it.trim() }
        singleAliases.trim()
        macros.trim()
        modifierCategories.trim()
        modifiers.trim()
        databaseObjectTypes.trim()
        extendedScriptedVariables.trim()
        extendedDefinitions.trim()
        extendedDefinitions.values.forEach { it.trim() }
        extendedGameRules.trim()
        extendedOnActions.trim()
        extendedParameters.trim()
        extendedParameters.values.forEach { it.trim() }
        extendedComplexEnumValues.trim()
        extendedComplexEnumValues.values.forEach { it.trim() }
        extendedDynamicValues.trim()
        extendedDynamicValues.values.forEach { it.trim() }
        extendedInlineScripts.trim()
        globalLocales.trim()
        supportedLocales.trim()
        predefinedModifiers.trim()
        generatedModifiers.trim()
        aliasKeysGroupConst.trim()
        aliasKeysGroupConst.values.forEach { it.trim() }
        aliasKeysGroupNoConst.trim()
        aliasNamesSupportScope.trim()
        relatedLocalisationPatterns.trim()
        linksModel.trim()
        localisationLinksModel.trim()
        macrosModel.trim()
        typesModel.trim()
        attribute.trim()
    }
}

class CwtTypesModelBase : CwtTypesModel {
    override val base2Swapped: Object2ObjectLinkedOpenHashMap<String, String> = Object2ObjectLinkedOpenHashMap()
    override val swapped2Base: Object2ObjectLinkedOpenHashMap<String, String> = Object2ObjectLinkedOpenHashMap()
    override val supportScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val indirectSupportScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val skipCheckSystemScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val supportParameters: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val supportScopeContextInference: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val typeKeyPrefixAware: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()

    fun trim() {
        base2Swapped.trim()
        swapped2Base.trim()
        supportScope.trim()
        indirectSupportScope.trim()
        skipCheckSystemScope.trim()
        supportParameters.trim()
        supportScopeContextInference.trim()
        typeKeyPrefixAware.trim()
    }
}

class CwtLinksModelBase : CwtLinksModel {
    override val variable: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forScopeStatic: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forScopeNoPrefixSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forScopeFromDataSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forScopeFromArgumentSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forScopeFromArgumentSortedByPrefix: Object2ObjectLinkedOpenCustomHashMap<String, ObjectArrayList<CwtLinkConfig>> = CaseInsensitiveStringKeyMap()
    override val forValueStatic: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueNoPrefixSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromDataSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromArgumentSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromArgumentSortedByPrefix: Object2ObjectLinkedOpenCustomHashMap<String, ObjectArrayList<CwtLinkConfig>> = CaseInsensitiveStringKeyMap()

    fun trim() {
        variable.trim()
        forScopeStatic.trim()
        forScopeNoPrefixSorted.trim()
        forScopeFromDataSorted.trim()
        forScopeFromArgumentSorted.trim()
        forScopeFromArgumentSortedByPrefix.trim()
        forScopeFromArgumentSortedByPrefix.values.forEach { it.trim() }
        forValueStatic.trim()
        forValueNoPrefixSorted.trim()
        forValueFromDataSorted.trim()
        forValueFromArgumentSorted.trim()
        forValueFromArgumentSortedByPrefix.trim()
        forValueFromArgumentSortedByPrefix.values.forEach { it.trim() }
    }
}

class CwtMacrosModelBase : CwtMacrosModel {
    override val forInlineScripts: ObjectArrayList<CwtMacroConfig.InlineScript> = ObjectArrayList()
    override var forDefinitionInjections: CwtMacroConfig.DefinitionInjection? = null

    fun trim() {
        forInlineScripts.trim()
    }
}

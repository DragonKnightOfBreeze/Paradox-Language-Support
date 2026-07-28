package icu.windea.pls.config.configGroup

import icu.windea.pls.config.attributes.CwtExpandableConfigAttributes
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
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
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.get
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.metadata.MetadataMapBase
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

abstract class CwtConfigGroupDataHolderBase : MetadataMapBase(), CwtConfigGroupDataHolder {
    // 3.0.1 use explicit code with folding, instead of delegate properties with addon code injector, to make things simple

    final override val fileConfigs: Object2ObjectLinkedOpenHashMap<String, CwtFileConfig> // region
        get() = this[Keys.fileConfigs] // endregion
    final override val schemas: ObjectArrayList<CwtSchemaConfig> // region
        get() = this[Keys.schemas] // endregion
    final override val foldingSettings: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtFoldingSettingsConfig>> // region
        get() = this[Keys.foldingSettings] // endregion
    final override val postfixTemplateSettings: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>> // region
        get() = this[Keys.postfixTemplateSettings] // endregion
    final override val priorities: Object2ObjectLinkedOpenHashMap<String, ParadoxOverrideStrategy> // region
        get() = this[Keys.priorities] // endregion
    final override val systemScopes: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtSystemScopeConfig> // region
        get() = this[Keys.systemScopes] // endregion
    final override val locales: Object2ObjectLinkedOpenHashMap<String, CwtLocaleConfig> // region
        get() = this[Keys.localesById] // endregion
    final override val types: Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig> // region
        get() = this[Keys.types] // endregion
    final override val swappedTypes: Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig> // region
        get() = this[Keys.swappedTypes] // endregion
    final override val type2ModifiersMap: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtModifierConfig>> // region
        get() = this[Keys.type2ModifiersMap] // endregion
    final override val declarations: Object2ObjectLinkedOpenHashMap<String, CwtDeclarationConfig> // region
        get() = this[Keys.declarations] // endregion
    final override val rows: Object2ObjectLinkedOpenHashMap<String, CwtRowConfig> // region
        get() = this[Keys.rows] // endregion
    final override val defineNamespaces: Object2ObjectLinkedOpenHashMap<String, CwtDefineNamespaceConfig> // region
        get() = this[Keys.defineNamespaces] // endregion
    final override val enums: Object2ObjectLinkedOpenHashMap<String, CwtEnumConfig> // region
        get() = this[Keys.enums] // endregion
    final override val complexEnums: Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig> // region
        get() = this[Keys.complexEnums] // endregion
    final override val complexEnumsFromColumns: Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig> // region
        get() = this[Keys.complexEnumsFromColumns] // endregion
    final override val unions: Object2ObjectLinkedOpenHashMap<String, CwtUnionConfig> // region
        get() = this[Keys.unions] // endregion
    final override val dynamicValueTypes: Object2ObjectLinkedOpenHashMap<String, CwtDynamicValueTypeConfig> // region
        get() = this[Keys.dynamicValueTypes] // endregion
    final override val links: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig> // region
        get() = this[Keys.links] // endregion
    final override val localisationLinks: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig> // region
        get() = this[Keys.localisationLinks] // endregion
    final override val localisationCommands: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationCommandConfig> // region
        get() = this[Keys.localisationCommands] // endregion
    final override val localisationPromotions: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationPromotionConfig> // region
        get() = this[Keys.localisationPromotions] // endregion
    final override val scopes: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig> // region
        get() = this[Keys.scopes] // endregion
    final override val scopeAliasMap: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig> // region
        get() = this[Keys.scopeAliasMap] // endregion
    final override val scopeGroups: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeGroupConfig> // region
        get() = this[Keys.scopeGroups] // endregion
    final override val aliasGroups: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtAliasConfig>>> // region
        get() = this[Keys.aliasGroups] // endregion
    final override val singleAliases: Object2ObjectLinkedOpenHashMap<String, CwtSingleAliasConfig> // region
        get() = this[Keys.singleAliases] // endregion
    final override val modifierCategories: Object2ObjectLinkedOpenHashMap<String, CwtModifierCategoryConfig> // region
        get() = this[Keys.modifierCategories] // endregion
    final override val modifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> // region
        get() = this[Keys.modifiers] // endregion
    final override val databaseObjectTypes: Object2ObjectLinkedOpenHashMap<String, CwtDatabaseObjectTypeConfig> // region
        get() = this[Keys.databaseObjectTypes] // endregion
    final override val macros: ObjectArrayList<CwtMacroConfig> // region
        get() = this[Keys.macros] // endregion
    final override val extendedScriptedVariables: Object2ObjectLinkedOpenHashMap<String, CwtExtendedScriptedVariableConfig> // region
        get() = this[Keys.extendedScriptedVariables] // endregion
    final override val extendedDefinitions: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedDefinitionConfig>> // region
        get() = this[Keys.extendedDefinitions] // endregion
    final override val extendedGameRules: Object2ObjectLinkedOpenHashMap<String, CwtExtendedGameRuleConfig> // region
        get() = this[Keys.extendedGameRules] // endregion
    final override val extendedOnActions: Object2ObjectLinkedOpenHashMap<String, CwtExtendedOnActionConfig> // region
        get() = this[Keys.extendedOnActions] // endregion
    final override val extendedParameters: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedParameterConfig>> // region
        get() = this[Keys.extendedParameters] // endregion
    final override val extendedComplexEnumValues: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedComplexEnumValueConfig>> // region
        get() = this[Keys.extendedComplexEnumValues] // endregion
    final override val extendedDynamicValues: Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedDynamicValueConfig>> // region
        get() = this[Keys.extendedDynamicValues] // endregion
    final override val extendedInlineScripts: Object2ObjectLinkedOpenHashMap<String, CwtExtendedInlineScriptConfig> // region
        get() = this[Keys.extendedInlineScripts] // endregion
    final override val globalLocales: ObjectArrayList<CwtLocaleConfig> // region
        get() = this[Keys.globalLocales] // endregion
    final override val supportedLocales: ObjectArrayList<CwtLocaleConfig> // region
        get() = this[Keys.supportedLocales] // endregion
    final override val predefinedModifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> // region
        get() = this[Keys.predefinedModifiers] // endregion
    final override val generatedModifiers: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig> // region
        get() = this[Keys.generatedModifiers] // endregion
    final override val aliasKeysGroupConst: Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, String>> // region
        get() = this[Keys.aliasKeysGroupConst] // endregion
    final override val aliasKeysGroupNoConst: Object2ObjectLinkedOpenHashMap<String, ObjectLinkedOpenHashSet<String>> // region
        get() = this[Keys.aliasKeysGroupNoConst] // endregion
    final override val aliasNamesSupportScope: ObjectLinkedOpenHashSet<String> // region
        get() = this[Keys.aliasNamesSupportScope] // endregion
    final override val relatedLocalisationPatterns: ObjectLinkedOpenHashSet<Tuple2<String, String>> // region
        get() = this[Keys.relatedLocalisationPatterns] // endregion
    final override val typesModel: CwtTypesModelBase // region
        get() = this[Keys.typesModel] // endregion
    final override val linksModel: CwtLinksModelBase // region
        get() = this[Keys.linksModel] // endregion
    final override val localisationLinksModel: CwtLinksModelBase // region
        get() = this[Keys.localisationLinksModel] // endregion
    final override val macrosModel: CwtMacrosModelBase // region
        get() = this[Keys.macrosModel] // endregion
    final override val filePathExpressions: ObjectLinkedOpenHashSet<CwtDataExpression> // region
        get() = this[Keys.filePathExpressions] // endregion
    final override val parameterConfigs: ObjectLinkedOpenHashSet<CwtMemberConfig<*>> // region
        get() = this[Keys.parameterConfigs] // endregion
    final override val unionAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> // region
        get() = this[Keys.unionAttributes] // endregion
    final override val aliasAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> // region
        get() = this[Keys.aliasAttributes] // endregion
    final override val singleAliasAttributes: Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes> // region
        get() = this[Keys.singleAliasAttributes] // endregion

    final override fun trim() {
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
        filePathExpressions.trim()
        parameterConfigs.trim()
    }

    final override fun clear() {
        clearUserData()
    }
}

object CwtConfigGroupDataHolderKeys : KeyRegistry() {
    val fileConfigs by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtFileConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val schemas by registerKey<ObjectArrayList<CwtSchemaConfig>>(this) { ObjectArrayList() }
    val foldingSettings by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val postfixTemplateSettings by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val priorities by registerKey<Object2ObjectLinkedOpenHashMap<String, ParadoxOverrideStrategy>>(this) { Object2ObjectLinkedOpenHashMap() }
    val systemScopes by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtSystemScopeConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val localesById by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtLocaleConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val types by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val swappedTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val type2ModifiersMap by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtModifierConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val declarations by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDeclarationConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val rows by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtRowConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val defineNamespaces by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDefineNamespaceConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val enums by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtEnumConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val complexEnums by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val complexEnumsFromColumns by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val unions by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtUnionConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val dynamicValueTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDynamicValueTypeConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val links by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val localisationLinks by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val localisationCommands by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationCommandConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val localisationPromotions by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val scopes by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val scopeAliasMap by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val scopeGroups by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeGroupConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val aliasGroups by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtAliasConfig>>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val singleAliases by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtSingleAliasConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val modifierCategories by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtModifierCategoryConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val modifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val databaseObjectTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDatabaseObjectTypeConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val macros by registerKey<ObjectArrayList<CwtMacroConfig>>(this) { ObjectArrayList() }
    val extendedScriptedVariables by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedScriptedVariableConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedDefinitions by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedDefinitionConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedGameRules by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedGameRuleConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedOnActions by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedOnActionConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedParameters by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedParameterConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedComplexEnumValues by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedComplexEnumValueConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedDynamicValues by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedDynamicValueConfig>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val extendedInlineScripts by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedInlineScriptConfig>>(this) { Object2ObjectLinkedOpenHashMap() }
    val globalLocales by registerKey<ObjectArrayList<CwtLocaleConfig>>(this) { ObjectArrayList() }
    val supportedLocales by registerKey<ObjectArrayList<CwtLocaleConfig>>(this) { ObjectArrayList() }
    val predefinedModifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val generatedModifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>>(this) { CaseInsensitiveStringKeyMap() }
    val aliasKeysGroupConst by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, String>>>(this) { CaseInsensitiveStringKeyMap() }
    val aliasKeysGroupNoConst by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectLinkedOpenHashSet<String>>>(this) { Object2ObjectLinkedOpenHashMap() }
    val aliasNamesSupportScope by registerKey<ObjectLinkedOpenHashSet<String>>(this) { ObjectLinkedOpenHashSet() }
    val relatedLocalisationPatterns by registerKey<ObjectLinkedOpenHashSet<Tuple2<String, String>>>(this) { ObjectLinkedOpenHashSet() }
    val typesModel by registerKey(this) { CwtTypesModelBase() }
    val linksModel by registerKey(this) { CwtLinksModelBase() }
    val localisationLinksModel by registerKey(this) { CwtLinksModelBase() }
    val macrosModel by registerKey(this) { CwtMacrosModelBase() }
    val filePathExpressions by registerKey<ObjectLinkedOpenHashSet<CwtDataExpression>>(this) { ObjectLinkedOpenHashSet() }
    val parameterConfigs by registerKey<ObjectLinkedOpenHashSet<CwtMemberConfig<*>>>(this) { ObjectLinkedOpenHashSet() }
    val unionAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>>(this) { Object2ObjectLinkedOpenHashMap() }
    val aliasAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>>(this) { Object2ObjectLinkedOpenHashMap() }
    val singleAliasAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>>(this) { Object2ObjectLinkedOpenHashMap() }
}

private typealias Keys = CwtConfigGroupDataHolderKeys

class CwtTypesModelBase : CwtTypesModel {
    override val base2Swapped: Object2ObjectLinkedOpenHashMap<String, String> = Object2ObjectLinkedOpenHashMap()
    override val swapped2Base: Object2ObjectLinkedOpenHashMap<String, String> = Object2ObjectLinkedOpenHashMap()
    override val supportScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val indirectSupportScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val skipCheckSystemScope: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val supportParameters: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val supportScopeContextInference: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()
    override val typeKeyPrefixAware: ObjectLinkedOpenHashSet<String> = ObjectLinkedOpenHashSet()

    override fun trim() {
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
    override val forScopeFromArgumentSortedByPrefix: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtLinkConfig>> = Object2ObjectLinkedOpenHashMap()
    override val forValueStatic: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueNoPrefixSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromDataSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromArgumentSorted: ObjectArrayList<CwtLinkConfig> = ObjectArrayList()
    override val forValueFromArgumentSortedByPrefix: Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtLinkConfig>> = Object2ObjectLinkedOpenHashMap()

    override fun trim() {
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

    override fun trim() {
        forInlineScripts.trim()
    }
}

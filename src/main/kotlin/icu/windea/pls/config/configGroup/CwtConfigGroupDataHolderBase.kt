package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
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
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

abstract class CwtConfigGroupDataHolderBase : UserDataHolderBase(), CwtConfigGroupDataHolder {
    object Keys : KeyRegistry() {
        val fileConfigs by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtFileConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val schemas by registerKey<ObjectArrayList<CwtSchemaConfig>, UserDataHolder>(this) { ObjectArrayList() }
        val foldingSettings by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val postfixTemplateSettings by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val priorities by registerKey<Object2ObjectLinkedOpenHashMap<String, ParadoxOverrideStrategy>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val systemScopes by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtSystemScopeConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val localesById by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtLocaleConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val types by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val swappedTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val type2ModifiersMap by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtModifierConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val declarations by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDeclarationConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val rows by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtRowConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val defineNamespaces by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDefineNamespaceConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val enums by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtEnumConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val complexEnums by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val complexEnumsFromColumns by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val unions by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtUnionConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val dynamicValueTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDynamicValueTypeConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val links by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val localisationLinks by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLinkConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val localisationCommands by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationCommandConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val localisationPromotions by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val scopes by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val scopeAliasMap by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val scopeGroups by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtScopeGroupConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val aliasGroups by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtAliasConfig>>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val singleAliases by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtSingleAliasConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val modifierCategories by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtModifierCategoryConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val modifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val databaseObjectTypes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtDatabaseObjectTypeConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val macros by registerKey<ObjectArrayList<CwtMacroConfig>, UserDataHolder>(this) { ObjectArrayList() }
        val extendedScriptedVariables by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedScriptedVariableConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedDefinitions by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedDefinitionConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedGameRules by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedGameRuleConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedOnActions by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedOnActionConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedParameters by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedParameterConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedComplexEnumValues by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedComplexEnumValueConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedDynamicValues by registerKey<Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedDynamicValueConfig>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val extendedInlineScripts by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExtendedInlineScriptConfig>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val globalLocales by registerKey<ObjectArrayList<CwtLocaleConfig>, UserDataHolder>(this) { ObjectArrayList() }
        val supportedLocales by registerKey<ObjectArrayList<CwtLocaleConfig>, UserDataHolder>(this) { ObjectArrayList() }
        val predefinedModifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val generatedModifiers by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val aliasKeysGroupConst by registerKey<Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, String>>, UserDataHolder>(this) { CaseInsensitiveStringKeyMap() }
        val aliasKeysGroupNoConst by registerKey<Object2ObjectLinkedOpenHashMap<String, ObjectLinkedOpenHashSet<String>>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val aliasNamesSupportScope by registerKey<ObjectLinkedOpenHashSet<String>, UserDataHolder>(this) { ObjectLinkedOpenHashSet() }
        val relatedLocalisationPatterns by registerKey<ObjectLinkedOpenHashSet<Tuple2<String, String>>, UserDataHolder>(this) { ObjectLinkedOpenHashSet() }
        val typesModel by registerKey<CwtTypesModelBase, UserDataHolder>(this) { CwtTypesModelBase() }
        val linksModel by registerKey<CwtLinksModelBase, UserDataHolder>(this) { CwtLinksModelBase() }
        val localisationLinksModel by registerKey<CwtLinksModelBase, UserDataHolder>(this) { CwtLinksModelBase() }
        val macrosModel by registerKey<CwtMacrosModelBase, UserDataHolder>(this) { CwtMacrosModelBase() }
        val filePathExpressions by registerKey<ObjectLinkedOpenHashSet<CwtDataExpression>, UserDataHolder>(this) { ObjectLinkedOpenHashSet() }
        val parameterConfigs by registerKey<ObjectLinkedOpenHashSet<CwtMemberConfig<*>>, UserDataHolder>(this) { ObjectLinkedOpenHashSet() }
        val unionAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val aliasAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
        val singleAliasAttributes by registerKey<Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>, UserDataHolder>(this) { Object2ObjectLinkedOpenHashMap() }
    }

    // region Accessors

    final override val fileConfigs by Keys.fileConfigs
    final override val schemas by Keys.schemas
    final override val foldingSettings by Keys.foldingSettings
    final override val postfixTemplateSettings by Keys.postfixTemplateSettings
    final override val priorities by Keys.priorities
    final override val systemScopes by Keys.systemScopes
    final override val locales by Keys.localesById
    final override val types by Keys.types
    final override val swappedTypes by Keys.swappedTypes
    final override val type2ModifiersMap by Keys.type2ModifiersMap
    final override val declarations by Keys.declarations
    final override val rows by Keys.rows
    final override val defineNamespaces by Keys.defineNamespaces
    final override val enums by Keys.enums
    final override val complexEnums by Keys.complexEnums
    final override val complexEnumsFromColumns by Keys.complexEnumsFromColumns
    final override val unions by Keys.unions
    final override val dynamicValueTypes by Keys.dynamicValueTypes
    final override val links by Keys.links
    final override val localisationLinks by Keys.localisationLinks
    final override val localisationCommands by Keys.localisationCommands
    final override val localisationPromotions by Keys.localisationPromotions
    final override val scopes by Keys.scopes
    final override val scopeAliasMap by Keys.scopeAliasMap
    final override val scopeGroups by Keys.scopeGroups
    final override val aliasGroups by Keys.aliasGroups
    final override val singleAliases by Keys.singleAliases
    final override val modifierCategories by Keys.modifierCategories
    final override val modifiers by Keys.modifiers
    final override val databaseObjectTypes by Keys.databaseObjectTypes
    final override val macros by Keys.macros
    final override val extendedScriptedVariables by Keys.extendedScriptedVariables
    final override val extendedDefinitions by Keys.extendedDefinitions
    final override val extendedGameRules by Keys.extendedGameRules
    final override val extendedOnActions by Keys.extendedOnActions
    final override val extendedParameters by Keys.extendedParameters
    final override val extendedComplexEnumValues by Keys.extendedComplexEnumValues
    final override val extendedDynamicValues by Keys.extendedDynamicValues
    final override val extendedInlineScripts by Keys.extendedInlineScripts
    final override val globalLocales by Keys.globalLocales
    final override val supportedLocales by Keys.supportedLocales
    final override val predefinedModifiers by Keys.predefinedModifiers
    final override val generatedModifiers by Keys.generatedModifiers
    final override val aliasKeysGroupConst by Keys.aliasKeysGroupConst
    final override val aliasKeysGroupNoConst by Keys.aliasKeysGroupNoConst
    final override val aliasNamesSupportScope by Keys.aliasNamesSupportScope
    final override val relatedLocalisationPatterns by Keys.relatedLocalisationPatterns
    final override val typesModel by Keys.typesModel
    final override val linksModel by Keys.linksModel
    final override val localisationLinksModel by Keys.localisationLinksModel
    final override val macrosModel by Keys.macrosModel
    final override val filePathExpressions by Keys.filePathExpressions
    final override val parameterConfigs by Keys.parameterConfigs
    final override val unionAttributes by Keys.unionAttributes
    final override val aliasAttributes by Keys.aliasAttributes
    final override val singleAliasAttributes by Keys.singleAliasAttributes

    // endregion

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

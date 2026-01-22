package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
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
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.FastCustomMap
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy

abstract class CwtConfigGroupDataHolderBase : UserDataHolderBase(), CwtConfigGroupDataHolder {
    object Keys : KeyRegistry() {
        val schemas by registerKey<FastList<CwtSchemaConfig>, CwtConfigGroupDataHolder>(this) { FastList() }
        val foldingSettings by registerKey<FastMap<String, FastCustomMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>, UserDataHolder>(this) { FastMap() }
        val postfixTemplateSettings by registerKey<FastMap<String, FastCustomMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>, UserDataHolder>(this) { FastMap() }
        val priorities by registerKey<FastMap<String, ParadoxOverrideStrategy>, UserDataHolder>(this) { FastMap() }
        val systemScopes by registerKey<FastCustomMap<@CaseInsensitive String, CwtSystemScopeConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val localisationLocalesById by registerKey<FastMap<String, CwtLocaleConfig>, UserDataHolder>(this) { FastMap() }
        val localisationLocalesByCode by registerKey<FastMap<String, CwtLocaleConfig>, UserDataHolder>(this) { FastMap() }
        val types by registerKey<FastMap<String, CwtTypeConfig>, UserDataHolder>(this) { FastMap() }
        val swappedTypes by registerKey<FastMap<String, CwtTypeConfig>, UserDataHolder>(this) { FastMap() }
        val type2ModifiersMap by registerKey<FastMap<String, FastMap<String, CwtModifierConfig>>, UserDataHolder>(this) { FastMap() }
        val declarations by registerKey<FastMap<String, CwtDeclarationConfig>, UserDataHolder>(this) { FastMap() }
        val rows by registerKey<FastMap<String, CwtRowConfig>, UserDataHolder>(this) { FastMap() }
        val enums by registerKey<FastMap<String, CwtEnumConfig>, UserDataHolder>(this) { FastMap() }
        val complexEnums by registerKey<FastMap<String, CwtComplexEnumConfig>, UserDataHolder>(this) { FastMap() }
        val dynamicValueTypes by registerKey<FastMap<String, CwtDynamicValueTypeConfig>, UserDataHolder>(this) { FastMap() }
        val links by registerKey<FastCustomMap<@CaseInsensitive String, CwtLinkConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val localisationLinks by registerKey<FastCustomMap<@CaseInsensitive String, CwtLinkConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val localisationCommands by registerKey<FastCustomMap<@CaseInsensitive String, CwtLocalisationCommandConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val localisationPromotions by registerKey<FastCustomMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val scopes by registerKey<FastCustomMap<@CaseInsensitive String, CwtScopeConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val scopeAliasMap by registerKey<FastCustomMap<@CaseInsensitive String, CwtScopeConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val scopeGroups by registerKey<FastCustomMap<@CaseInsensitive String, CwtScopeGroupConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val singleAliases by registerKey<FastMap<String, CwtSingleAliasConfig>, UserDataHolder>(this) { FastMap() }
        val aliasGroups by registerKey<FastMap<String, FastMap<String, FastList<CwtAliasConfig>>>, UserDataHolder>(this) { FastMap() }
        val directives by registerKey<FastList<CwtDirectiveConfig>, UserDataHolder>(this) { FastList() }
        val modifierCategories by registerKey<FastMap<String, CwtModifierCategoryConfig>, UserDataHolder>(this) { FastMap() }
        val modifiers by registerKey<FastCustomMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val databaseObjectTypes by registerKey<FastMap<String, CwtDatabaseObjectTypeConfig>, UserDataHolder>(this) { FastMap() }
        val extendedScriptedVariables by registerKey<FastMap<String, CwtExtendedScriptedVariableConfig>, UserDataHolder>(this) { FastMap() }
        val extendedDefinitions by registerKey<FastMap<String, FastList<CwtExtendedDefinitionConfig>>, UserDataHolder>(this) { FastMap() }
        val extendedGameRules by registerKey<FastMap<String, CwtExtendedGameRuleConfig>, UserDataHolder>(this) { FastMap() }
        val extendedOnActions by registerKey<FastMap<String, CwtExtendedOnActionConfig>, UserDataHolder>(this) { FastMap() }
        val extendedComplexEnumValues by registerKey<FastMap<String, FastMap<String, CwtExtendedComplexEnumValueConfig>>, UserDataHolder>(this) { FastMap() }
        val extendedDynamicValues by registerKey<FastMap<String, FastMap<String, CwtExtendedDynamicValueConfig>>, UserDataHolder>(this) { FastMap() }
        val extendedInlineScripts by registerKey<FastMap<String, CwtExtendedInlineScriptConfig>, UserDataHolder>(this) { FastMap() }
        val extendedParameters by registerKey<FastMap<String, FastList<CwtExtendedParameterConfig>>, UserDataHolder>(this) { FastMap() }
        val predefinedModifiers by registerKey<FastCustomMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val generatedModifiers by registerKey<FastCustomMap<@CaseInsensitive String, CwtModifierConfig>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val aliasKeysGroupConst by registerKey<FastCustomMap<@CaseInsensitive String, FastCustomMap<@CaseInsensitive String, String>>, UserDataHolder>(this) { caseInsensitiveStringKeyMap() }
        val aliasKeysGroupNoConst by registerKey<FastMap<String, FastSet<String>>, UserDataHolder>(this) { FastMap() }
        val aliasNamesSupportScope by registerKey<FastSet<String>, UserDataHolder>(this) { FastSet() }
        val relatedLocalisationPatterns by registerKey<FastSet<Tuple2<String, String>>, UserDataHolder>(this) { FastSet() }
        val linksModel by registerKey<CwtLinksModelBase, UserDataHolder>(this) { CwtLinksModelBase() }
        val localisationLinksModel by registerKey<CwtLinksModelBase, UserDataHolder>(this) { CwtLinksModelBase() }
        val directivesModel by registerKey<CwtDirectivesModelBase, UserDataHolder>(this) { CwtDirectivesModelBase() }
        val definitionTypesModel by registerKey<CwtDefinitionTypesModelBase, UserDataHolder>(this) { CwtDefinitionTypesModelBase() }
        val filePathExpressions by registerKey<FastSet<CwtDataExpression>, UserDataHolder>(this) { FastSet() }
        val parameterConfigs by registerKey<FastSet<CwtMemberConfig<*>>, UserDataHolder>(this) { FastSet() }
    }

    // region Accessors

    // see: icu.windea.pls.inject.injectors.addon.InlinedDelegateFieldCodeInjectors.CwtConfigGroupDataHolderBase

    final override val schemas by Keys.schemas
    final override val foldingSettings by Keys.foldingSettings
    final override val postfixTemplateSettings by Keys.postfixTemplateSettings
    final override val priorities by Keys.priorities
    final override val systemScopes by Keys.systemScopes
    final override val localisationLocalesById by Keys.localisationLocalesById
    final override val localisationLocalesByCode by Keys.localisationLocalesByCode
    final override val types by Keys.types
    final override val swappedTypes by Keys.swappedTypes
    final override val type2ModifiersMap by Keys.type2ModifiersMap
    final override val declarations by Keys.declarations
    final override val rows by Keys.rows
    final override val enums by Keys.enums
    final override val complexEnums by Keys.complexEnums
    final override val dynamicValueTypes by Keys.dynamicValueTypes
    final override val links by Keys.links
    final override val localisationLinks by Keys.localisationLinks
    final override val localisationCommands by Keys.localisationCommands
    final override val localisationPromotions by Keys.localisationPromotions
    final override val scopes by Keys.scopes
    final override val scopeAliasMap by Keys.scopeAliasMap
    final override val scopeGroups by Keys.scopeGroups
    final override val singleAliases by Keys.singleAliases
    final override val aliasGroups by Keys.aliasGroups
    final override val directives by Keys.directives
    final override val modifierCategories by Keys.modifierCategories
    final override val modifiers by Keys.modifiers
    final override val databaseObjectTypes by Keys.databaseObjectTypes
    final override val extendedScriptedVariables by Keys.extendedScriptedVariables
    final override val extendedDefinitions by Keys.extendedDefinitions
    final override val extendedGameRules by Keys.extendedGameRules
    final override val extendedOnActions by Keys.extendedOnActions
    final override val extendedComplexEnumValues by Keys.extendedComplexEnumValues
    final override val extendedDynamicValues by Keys.extendedDynamicValues
    final override val extendedInlineScripts by Keys.extendedInlineScripts
    final override val extendedParameters by Keys.extendedParameters
    final override val predefinedModifiers by Keys.predefinedModifiers
    final override val generatedModifiers by Keys.generatedModifiers
    final override val aliasKeysGroupConst by Keys.aliasKeysGroupConst
    final override val aliasKeysGroupNoConst by Keys.aliasKeysGroupNoConst
    final override val aliasNamesSupportScope by Keys.aliasNamesSupportScope
    final override val relatedLocalisationPatterns by Keys.relatedLocalisationPatterns
    final override val linksModel by Keys.linksModel
    final override val localisationLinksModel by Keys.localisationLinksModel
    final override val directivesModel by Keys.directivesModel
    final override val definitionTypesModel by Keys.definitionTypesModel
    final override val filePathExpressions by Keys.filePathExpressions
    final override val parameterConfigs by Keys.parameterConfigs

    // endregion

    final override fun clear() {
        clearUserData()
    }
}

class CwtLinksModelBase : CwtLinksModel {
    override val variable: FastList<CwtLinkConfig> = FastList()
    override val forScopeStatic: FastList<CwtLinkConfig> = FastList()
    override val forScopeNoPrefixSorted: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromDataSorted: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromArgumentSorted: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromArgumentSortedByPrefix: FastMap<String, FastList<CwtLinkConfig>> = FastMap()
    override val forValueStatic: FastList<CwtLinkConfig> = FastList()
    override val forValueNoPrefixSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueFromDataSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueFromArgumentSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueFromArgumentSortedByPrefix: FastMap<String, FastList<CwtLinkConfig>> = FastMap()
}

class CwtDirectivesModelBase : CwtDirectivesModel {
    override val inlineScript: FastList<CwtDirectiveConfig> = FastList()
    override var definitionInjection: CwtDirectiveConfig? = null
}

class CwtDefinitionTypesModelBase : CwtDefinitionTypesModel {
    override val supportScope: FastSet<String> = FastSet()
    override val indirectSupportScope: FastSet<String> = FastSet()
    override val skipCheckSystemScope: FastSet<String> = FastSet()
    override val supportParameters: FastSet<String> = FastSet()
    override val typeKeyPrefixAware: FastSet<String> = FastSet()
}

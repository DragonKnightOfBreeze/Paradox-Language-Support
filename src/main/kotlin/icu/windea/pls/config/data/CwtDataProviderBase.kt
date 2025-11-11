package icu.windea.pls.config.data

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
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
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy

/**
 * 用于在初始化规则分组时修改规则数据。
 *
 * @see CwtConfigGroup
 * @see CwtConfigGroupInitializer
 */
abstract class CwtDataProviderBase : CwtDataProvider, UserDataHolderBase() {
    // region Accessors

    override val schemas get() = from.schemas
    override val foldingSettings get() = from.foldingSettings
    override val postfixTemplateSettings get() = from.postfixTemplateSettings
    override val priorities get() = from.priorities
    override val systemScopes get() = from.systemScopes
    override val localisationLocalesById get() = from.localisationLocalesById
    override val localisationLocalesByCode get() = from.localisationLocalesByCode
    override val types get() = from.types
    override val swappedTypes get() = from.swappedTypes
    override val type2ModifiersMap get() = from.type2ModifiersMap
    override val declarations get() = from.declarations
    override val rows get() = from.rows
    override val enums get() = from.enums
    override val complexEnums get() = from.complexEnums
    override val dynamicValueTypes get() = from.dynamicValueTypes
    override val links get() = from.links
    override val localisationLinks get() = from.localisationLinks
    override val localisationCommands get() = from.localisationCommands
    override val localisationPromotions get() = from.localisationPromotions
    override val scopes get() = from.scopes
    override val scopeAliasMap get() = from.scopeAliasMap
    override val scopeGroups get() = from.scopeGroups
    override val singleAliases get() = from.singleAliases
    override val aliasGroups get() = from.aliasGroups
    override val inlineConfigGroup get() = from.inlineConfigGroup
    override val modifierCategories get() = from.modifierCategories
    override val modifiers get() = from.modifiers
    override val databaseObjectTypes get() = from.databaseObjectTypes
    override val extendedScriptedVariables get() = from.extendedScriptedVariables
    override val extendedDefinitions get() = from.extendedDefinitions
    override val extendedGameRules get() = from.extendedGameRules
    override val extendedOnActions get() = from.extendedOnActions
    override val extendedComplexEnumValues get() = from.extendedComplexEnumValues
    override val extendedDynamicValues get() = from.extendedDynamicValues
    override val extendedInlineScripts get() = from.extendedInlineScripts
    override val extendedParameters get() = from.extendedParameters
    override val predefinedModifiers get() = from.predefinedModifiers
    override val generatedModifiers get() = from.generatedModifiers
    override val aliasKeysGroupConst get() = from.aliasKeysGroupConst
    override val aliasKeysGroupNoConst get() = from.aliasKeysGroupNoConst
    override val aliasNamesSupportScope get() = from.aliasNamesSupportScope
    override val relatedLocalisationPatterns get() = from.relatedLocalisationPatterns
    override val linksModel get() = from.linksModel
    override val localisationLinksModel get() = from.localisationLinksModel
    override val definitionTypesModel get() = from.definitionTypesModel
    override val filePathExpressions get() = from.filePathExpressions
    override val parameterConfigs get() = from.parameterConfigs

    // endregion

    override fun clear() {
        clearUserData()
    }
}

private inline val CwtDataProviderBase.from get() = this as UserDataHolder

// region Accessor Implementations

private val UserDataHolder.schemas: MutableList<CwtSchemaConfig>
    by createKey(CwtConfigGroup.Keys) { mutableListOf() }
private val UserDataHolder.foldingSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.postfixTemplateSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.priorities: MutableMap<String, ParadoxOverrideStrategy>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLocalesById: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.localisationLocalesByCode: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.types: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.swappedTypes: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.type2ModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.declarations: MutableMap<String, CwtDeclarationConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.rows: MutableMap<String, CwtRowConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.enums: MutableMap<String, CwtEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.complexEnums: MutableMap<String, CwtComplexEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.dynamicValueTypes: MutableMap<String, CwtDynamicValueTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.links: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLinks: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationPromotions: MutableMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeGroups: MutableMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.singleAliases: MutableMap<String, CwtSingleAliasConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.modifierCategories: MutableMap<String, CwtModifierCategoryConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.databaseObjectTypes: MutableMap<String, CwtDatabaseObjectTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedScriptedVariables: MutableMap<String, CwtExtendedScriptedVariableConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedDefinitions: MutableMap<String, MutableList<CwtExtendedDefinitionConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedGameRules: MutableMap<String, CwtExtendedGameRuleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedOnActions: MutableMap<String, CwtExtendedOnActionConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedComplexEnumValues: MutableMap<String, MutableMap<String, CwtExtendedComplexEnumValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedDynamicValues: MutableMap<String, MutableMap<String, CwtExtendedDynamicValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedInlineScripts: MutableMap<String, CwtExtendedInlineScriptConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.extendedParameters: MutableMap<String, MutableList<CwtExtendedParameterConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.predefinedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.generatedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupConst: MutableMap<@CaseInsensitive String, MutableMap<String, String>>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupNoConst: MutableMap<String, MutableSet<String>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
private val UserDataHolder.aliasNamesSupportScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
private val UserDataHolder.relatedLocalisationPatterns: MutableSet<Tuple2<String, String>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
private val UserDataHolder.linksModel: CwtLinksModelBase
    by createKey(CwtConfigGroup.Keys) { CwtLinksModelBase() }
private val UserDataHolder.localisationLinksModel: CwtLinksModelBase
    by createKey(CwtConfigGroup.Keys) { CwtLinksModelBase() }
private val UserDataHolder.definitionTypesModel: CwtDefinitionTypesModelBase
    by createKey(CwtConfigGroup.Keys) { CwtDefinitionTypesModelBase() }
private val UserDataHolder.filePathExpressions: MutableSet<CwtDataExpression>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
private val UserDataHolder.parameterConfigs: MutableSet<CwtMemberConfig<*>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

// endregion

class CwtLinksModelBase : CwtLinksModel {
    override val variable: MutableList<CwtLinkConfig> = mutableListOf()
    override val forScopeStatic: MutableList<CwtLinkConfig> = mutableListOf()
    override val forScopeFromArgumentSorted: MutableList<CwtLinkConfig> = mutableListOf()
    override val forScopeFromDataSorted: MutableList<CwtLinkConfig> = mutableListOf()
    override val forScopeFromDataNoPrefixSorted: MutableList<CwtLinkConfig> = mutableListOf()
    override val forValueStatic: MutableList<CwtLinkConfig> = mutableListOf()
    override val forValueFromArgumentSorted: MutableList<CwtLinkConfig> = mutableListOf()
    override val forValueFromDataSorted: MutableList<CwtLinkConfig> = mutableListOf()
    override val forValueFromDataNoPrefixSorted: MutableList<CwtLinkConfig> = mutableListOf()
}

class CwtDefinitionTypesModelBase : CwtDefinitionTypesModel {
    override val supportScope: MutableSet<String> = mutableSetOf()
    override val indirectSupportScope: MutableSet<String> = mutableSetOf()
    override val skipCheckSystemScope: MutableSet<String> = mutableSetOf()
    override val supportParameters: MutableSet<String> = mutableSetOf()
    override val mayWithTypeKeyPrefix: MutableSet<String> = mutableSetOf()
}

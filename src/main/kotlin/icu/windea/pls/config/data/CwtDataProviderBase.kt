package icu.windea.pls.config.data

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
import icu.windea.pls.config.data.CwtDataProvider.DefinitionTypesModel
import icu.windea.pls.config.data.CwtDataProvider.LinksModel
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
    // region Internal

    override val schemas: MutableList<CwtSchemaConfig>
        by createKey(CwtConfigGroup.Keys) { mutableListOf() }
    override val foldingSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val postfixTemplateSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

    // endregion

    // region Core

    override val priorities: MutableMap<String, ParadoxOverrideStrategy>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val localisationLocalesById: MutableMap<String, CwtLocaleConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val localisationLocalesByCode: MutableMap<String, CwtLocaleConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val types: MutableMap<String, CwtTypeConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val swappedTypes: MutableMap<String, CwtTypeConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val type2ModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val declarations: MutableMap<String, CwtDeclarationConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val rows: MutableMap<String, CwtRowConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val enums: MutableMap<String, CwtEnumConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val complexEnums: MutableMap<String, CwtComplexEnumConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val dynamicValueTypes: MutableMap<String, CwtDynamicValueTypeConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val links: MutableMap<@CaseInsensitive String, CwtLinkConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val localisationLinks: MutableMap<@CaseInsensitive String, CwtLinkConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val localisationPromotions: MutableMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

    override val scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val scopeGroups: MutableMap<@CaseInsensitive String, CwtScopeGroupConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

    override val singleAliases: MutableMap<String, CwtSingleAliasConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    override val modifierCategories: MutableMap<String, CwtModifierCategoryConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

    override val databaseObjectTypes: MutableMap<String, CwtDatabaseObjectTypeConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    // endregion

    // region Extended

    override val extendedScriptedVariables: MutableMap<String, CwtExtendedScriptedVariableConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedDefinitions: MutableMap<String, MutableList<CwtExtendedDefinitionConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedGameRules: MutableMap<String, CwtExtendedGameRuleConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedOnActions: MutableMap<String, CwtExtendedOnActionConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedComplexEnumValues: MutableMap<String, MutableMap<String, CwtExtendedComplexEnumValueConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedDynamicValues: MutableMap<String, MutableMap<String, CwtExtendedDynamicValueConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedInlineScripts: MutableMap<String, CwtExtendedInlineScriptConfig>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val extendedParameters: MutableMap<String, MutableList<CwtExtendedParameterConfig>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

    // endregion

    // region Computed

    override val predefinedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val generatedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

    override val aliasKeysGroupConst: MutableMap<@CaseInsensitive String, MutableMap<String, String>>
        by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
    override val aliasKeysGroupNoConst: MutableMap<String, MutableSet<String>>
        by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
    override val aliasNamesSupportScope: MutableSet<String>
        by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

    override val relatedLocalisationPatterns: MutableSet<Tuple2<String, String>>
        by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

    override val linksModel: LinksModelImpl
        by createKey(CwtConfigGroup.Keys) { LinksModelImpl() }
    override val localisationLinksModel: LinksModelImpl
        by createKey(CwtConfigGroup.Keys) { LinksModelImpl() }
    override val definitionTypesModel: DefinitionTypesModelImpl
        by createKey(CwtConfigGroup.Keys) { DefinitionTypesModelImpl() }

    // endregion

    // region Collected

    override val filePathExpressions: MutableSet<CwtDataExpression>
        by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
    override val parameterConfigs: MutableSet<CwtMemberConfig<*>>
        by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

    // endregion

    class LinksModelImpl : LinksModel {
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

    class DefinitionTypesModelImpl : DefinitionTypesModel {
        override val supportScope: MutableSet<String> = mutableSetOf()
        override val indirectSupportScope: MutableSet<String> = mutableSetOf()
        override val skipCheckSystemScope: MutableSet<String> = mutableSetOf()
        override val supportParameters: MutableSet<String> = mutableSetOf()
        override val mayWithTypeKeyPrefix: MutableSet<String> = mutableSetOf()
    }

    override fun clear() {
        clearUserData()
    }
}

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
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy

abstract class CwtConfigGroupDataHolderBase : CwtConfigGroupDataHolder, UserDataHolderBase() {
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
    override val directives get() = from.directives
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
    override val directivesModel get() = from.directivesModel
    override val definitionTypesModel get() = from.definitionTypesModel
    override val filePathExpressions get() = from.filePathExpressions
    override val parameterConfigs get() = from.parameterConfigs

    // endregion

    override fun clear() {
        clearUserData()
    }
}

// region Accessor Implementations

private inline val CwtConfigGroupDataHolderBase.from get() = this as UserDataHolder

private val UserDataHolder.schemas: FastList<CwtSchemaConfig>
    by registerKey(CwtConfigGroup.Keys) { FastList() }
private val UserDataHolder.foldingSettings: FastMap<String, FastCustomMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.postfixTemplateSettings: FastMap<String, FastCustomMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.priorities: FastMap<String, ParadoxOverrideStrategy>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.systemScopes: FastCustomMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLocalesById: FastMap<String, CwtLocaleConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.localisationLocalesByCode: FastMap<String, CwtLocaleConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.types: FastMap<String, CwtTypeConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.swappedTypes: FastMap<String, CwtTypeConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.type2ModifiersMap: FastMap<String, FastMap<String, CwtModifierConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.declarations: FastMap<String, CwtDeclarationConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.rows: FastMap<String, CwtRowConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.enums: FastMap<String, CwtEnumConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.complexEnums: FastMap<String, CwtComplexEnumConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.dynamicValueTypes: FastMap<String, CwtDynamicValueTypeConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.links: FastCustomMap<@CaseInsensitive String, CwtLinkConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLinks: FastCustomMap<@CaseInsensitive String, CwtLinkConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationCommands: FastCustomMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationPromotions: FastCustomMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopes: FastCustomMap<@CaseInsensitive String, CwtScopeConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeAliasMap: FastCustomMap<@CaseInsensitive String, CwtScopeConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeGroups: FastCustomMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.singleAliases: FastMap<String, CwtSingleAliasConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.aliasGroups: FastMap<String, FastMap<String, FastList<CwtAliasConfig>>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.directives: FastList<CwtDirectiveConfig>
    by registerKey(CwtConfigGroup.Keys) { FastList() }
private val UserDataHolder.modifierCategories: FastMap<String, CwtModifierCategoryConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.modifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.databaseObjectTypes: FastMap<String, CwtDatabaseObjectTypeConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedScriptedVariables: FastMap<String, CwtExtendedScriptedVariableConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedDefinitions: FastMap<String, FastList<CwtExtendedDefinitionConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedGameRules: FastMap<String, CwtExtendedGameRuleConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedOnActions: FastMap<String, CwtExtendedOnActionConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedComplexEnumValues: FastMap<String, FastMap<String, CwtExtendedComplexEnumValueConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedDynamicValues: FastMap<String, FastMap<String, CwtExtendedDynamicValueConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedInlineScripts: FastMap<String, CwtExtendedInlineScriptConfig>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.extendedParameters: FastMap<String, FastList<CwtExtendedParameterConfig>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.predefinedModifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.generatedModifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupConst: FastCustomMap<@CaseInsensitive String, MutableMap<String, String>>
    by registerKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupNoConst: FastMap<String, FastSet<String>>
    by registerKey(CwtConfigGroup.Keys) { FastMap() }
private val UserDataHolder.aliasNamesSupportScope: FastSet<String>
    by registerKey(CwtConfigGroup.Keys) { FastSet() }
private val UserDataHolder.relatedLocalisationPatterns: FastSet<Tuple2<String, String>>
    by registerKey(CwtConfigGroup.Keys) { FastSet() }
private val UserDataHolder.linksModel: CwtLinksModelBase
    by registerKey(CwtConfigGroup.Keys) { CwtLinksModelBase() }
private val UserDataHolder.localisationLinksModel: CwtLinksModelBase
    by registerKey(CwtConfigGroup.Keys) { CwtLinksModelBase() }
private val UserDataHolder.directivesModel: CwtDirectivesModelBase
    by registerKey(CwtConfigGroup.Keys) { CwtDirectivesModelBase() }
private val UserDataHolder.definitionTypesModel: CwtDefinitionTypesModelBase
    by registerKey(CwtConfigGroup.Keys) { CwtDefinitionTypesModelBase() }
private val UserDataHolder.filePathExpressions: FastSet<CwtDataExpression>
    by registerKey(CwtConfigGroup.Keys) { FastSet() }
private val UserDataHolder.parameterConfigs: FastSet<CwtMemberConfig<*>>
    by registerKey(CwtConfigGroup.Keys) { FastSet() }

// endregion

class CwtLinksModelBase : CwtLinksModel {
    override val variable: FastList<CwtLinkConfig> = FastList()
    override val forScopeStatic: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromArgumentSorted: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromDataSorted: FastList<CwtLinkConfig> = FastList()
    override val forScopeFromDataNoPrefixSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueStatic: FastList<CwtLinkConfig> = FastList()
    override val forValueFromArgumentSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueFromDataSorted: FastList<CwtLinkConfig> = FastList()
    override val forValueFromDataNoPrefixSorted: FastList<CwtLinkConfig> = FastList()
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
    override val mayWithTypeKeyPrefix: FastSet<String> = FastSet()
}

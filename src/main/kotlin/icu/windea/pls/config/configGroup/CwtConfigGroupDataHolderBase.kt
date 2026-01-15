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

// region Accessor Implementations

private inline val CwtConfigGroupDataHolderBase.from get() = this as UserDataHolder

private object CwtConfigGroupDataKeys : KeyRegistry()

private val UserDataHolder.schemas: FastList<CwtSchemaConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastList() }
private val UserDataHolder.foldingSettings: FastMap<String, FastCustomMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.postfixTemplateSettings: FastMap<String, FastCustomMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.priorities: FastMap<String, ParadoxOverrideStrategy>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.systemScopes: FastCustomMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLocalesById: FastMap<String, CwtLocaleConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.localisationLocalesByCode: FastMap<String, CwtLocaleConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.types: FastMap<String, CwtTypeConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.swappedTypes: FastMap<String, CwtTypeConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.type2ModifiersMap: FastMap<String, FastMap<String, CwtModifierConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.declarations: FastMap<String, CwtDeclarationConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.rows: FastMap<String, CwtRowConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.enums: FastMap<String, CwtEnumConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.complexEnums: FastMap<String, CwtComplexEnumConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.dynamicValueTypes: FastMap<String, CwtDynamicValueTypeConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.links: FastCustomMap<@CaseInsensitive String, CwtLinkConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationLinks: FastCustomMap<@CaseInsensitive String, CwtLinkConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationCommands: FastCustomMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.localisationPromotions: FastCustomMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopes: FastCustomMap<@CaseInsensitive String, CwtScopeConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeAliasMap: FastCustomMap<@CaseInsensitive String, CwtScopeConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.scopeGroups: FastCustomMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.singleAliases: FastMap<String, CwtSingleAliasConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.aliasGroups: FastMap<String, FastMap<String, FastList<CwtAliasConfig>>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.directives: FastList<CwtDirectiveConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastList() }
private val UserDataHolder.modifierCategories: FastMap<String, CwtModifierCategoryConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.modifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.databaseObjectTypes: FastMap<String, CwtDatabaseObjectTypeConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedScriptedVariables: FastMap<String, CwtExtendedScriptedVariableConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedDefinitions: FastMap<String, FastList<CwtExtendedDefinitionConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedGameRules: FastMap<String, CwtExtendedGameRuleConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedOnActions: FastMap<String, CwtExtendedOnActionConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedComplexEnumValues: FastMap<String, FastMap<String, CwtExtendedComplexEnumValueConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedDynamicValues: FastMap<String, FastMap<String, CwtExtendedDynamicValueConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedInlineScripts: FastMap<String, CwtExtendedInlineScriptConfig>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.extendedParameters: FastMap<String, FastList<CwtExtendedParameterConfig>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.predefinedModifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.generatedModifiers: FastCustomMap<@CaseInsensitive String, CwtModifierConfig>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupConst: FastCustomMap<@CaseInsensitive String, FastCustomMap<@CaseInsensitive String, String>>
    by registerKey(CwtConfigGroupDataKeys) { caseInsensitiveStringKeyMap() }
private val UserDataHolder.aliasKeysGroupNoConst: FastMap<String, FastSet<String>>
    by registerKey(CwtConfigGroupDataKeys) { FastMap() }
private val UserDataHolder.aliasNamesSupportScope: FastSet<String>
    by registerKey(CwtConfigGroupDataKeys) { FastSet() }
private val UserDataHolder.relatedLocalisationPatterns: FastSet<Tuple2<String, String>>
    by registerKey(CwtConfigGroupDataKeys) { FastSet() }
private val UserDataHolder.linksModel: CwtLinksModelBase
    by registerKey(CwtConfigGroupDataKeys) { CwtLinksModelBase() }
private val UserDataHolder.localisationLinksModel: CwtLinksModelBase
    by registerKey(CwtConfigGroupDataKeys) { CwtLinksModelBase() }
private val UserDataHolder.directivesModel: CwtDirectivesModelBase
    by registerKey(CwtConfigGroupDataKeys) { CwtDirectivesModelBase() }
private val UserDataHolder.definitionTypesModel: CwtDefinitionTypesModelBase
    by registerKey(CwtConfigGroupDataKeys) { CwtDefinitionTypesModelBase() }
private val UserDataHolder.filePathExpressions: FastSet<CwtDataExpression>
    by registerKey(CwtConfigGroupDataKeys) { FastSet() }
private val UserDataHolder.parameterConfigs: FastSet<CwtMemberConfig<*>>
    by registerKey(CwtConfigGroupDataKeys) { FastSet() }

// endregion

abstract class CwtConfigGroupDataHolderBase : UserDataHolderBase(), CwtConfigGroupDataHolder {
    final override fun clear() {
        clearUserData()
    }

    final override val schemas get() = from.schemas
    final override val foldingSettings get() = from.foldingSettings
    final override val postfixTemplateSettings get() = from.postfixTemplateSettings
    final override val priorities get() = from.priorities
    final override val systemScopes get() = from.systemScopes
    final override val localisationLocalesById get() = from.localisationLocalesById
    final override val localisationLocalesByCode get() = from.localisationLocalesByCode
    final override val types get() = from.types
    final override val swappedTypes get() = from.swappedTypes
    final override val type2ModifiersMap get() = from.type2ModifiersMap
    final override val declarations get() = from.declarations
    final override val rows get() = from.rows
    final override val enums get() = from.enums
    final override val complexEnums get() = from.complexEnums
    final override val dynamicValueTypes get() = from.dynamicValueTypes
    final override val links get() = from.links
    final override val localisationLinks get() = from.localisationLinks
    final override val localisationCommands get() = from.localisationCommands
    final override val localisationPromotions get() = from.localisationPromotions
    final override val scopes get() = from.scopes
    final override val scopeAliasMap get() = from.scopeAliasMap
    final override val scopeGroups get() = from.scopeGroups
    final override val singleAliases get() = from.singleAliases
    final override val aliasGroups get() = from.aliasGroups
    final override val directives get() = from.directives
    final override val modifierCategories get() = from.modifierCategories
    final override val modifiers get() = from.modifiers
    final override val databaseObjectTypes get() = from.databaseObjectTypes
    final override val extendedScriptedVariables get() = from.extendedScriptedVariables
    final override val extendedDefinitions get() = from.extendedDefinitions
    final override val extendedGameRules get() = from.extendedGameRules
    final override val extendedOnActions get() = from.extendedOnActions
    final override val extendedComplexEnumValues get() = from.extendedComplexEnumValues
    final override val extendedDynamicValues get() = from.extendedDynamicValues
    final override val extendedInlineScripts get() = from.extendedInlineScripts
    final override val extendedParameters get() = from.extendedParameters
    final override val predefinedModifiers get() = from.predefinedModifiers
    final override val generatedModifiers get() = from.generatedModifiers
    final override val aliasKeysGroupConst get() = from.aliasKeysGroupConst
    final override val aliasKeysGroupNoConst get() = from.aliasKeysGroupNoConst
    final override val aliasNamesSupportScope get() = from.aliasNamesSupportScope
    final override val relatedLocalisationPatterns get() = from.relatedLocalisationPatterns
    final override val linksModel get() = from.linksModel
    final override val localisationLinksModel get() = from.localisationLinksModel
    final override val directivesModel get() = from.directivesModel
    final override val definitionTypesModel get() = from.definitionTypesModel
    final override val filePathExpressions get() = from.filePathExpressions
    final override val parameterConfigs get() = from.parameterConfigs
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

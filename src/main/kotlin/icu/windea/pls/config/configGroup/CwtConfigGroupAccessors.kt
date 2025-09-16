package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
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
import icu.windea.pls.config.configGroup.Tags.Tag
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.ep.priority.ParadoxPriority
import icu.windea.pls.ep.scope.ParadoxBaseDefinitionInferredScopeContextProvider
import icu.windea.pls.lang.ParadoxModificationTrackers

// region Annotations

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Tags(vararg val value: Tag) {
    enum class Tag {
        Internal, Extended, Computed, Collected
    }
}

// endregion

// region Internal Accessors

@Tags(Tag.Internal)
val CwtConfigGroup.schemas: MutableList<CwtSchemaConfig>
    by createKey(CwtConfigGroup.Keys) { mutableListOf() }
@Tags(Tag.Internal)
val CwtConfigGroup.foldingSettings: MutableMap<String, MutableMap<String, CwtFoldingSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
@Tags(Tag.Internal)
val CwtConfigGroup.postfixTemplateSettings: MutableMap<String, MutableMap<String, CwtPostfixTemplateSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// endregion

// region Core Accessors

// val CwtConfigGroup.files: MutableMap<String, CwtFileConfig>
//     by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.priorities: MutableMap<String, ParadoxPriority>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
val CwtConfigGroup.systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationLocalesById: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
val CwtConfigGroup.localisationLocalesByCode: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// type - typeConfig
val CwtConfigGroup.types: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// type - typeConfig
val CwtConfigGroup.swappedTypes: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.type2ModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// type - declarationConfig
val CwtConfigGroup.declarations: MutableMap<String, CwtDeclarationConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.rows: MutableMap<String, CwtRowConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// enumValue可以是int、float、bool类型，统一用字符串表示
val CwtConfigGroup.enums: MutableMap<String, CwtEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// 基于enum_name进行定位，对应的可能是key/value
val CwtConfigGroup.complexEnums: MutableMap<String, CwtComplexEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.dynamicValueTypes: MutableMap<String, CwtDynamicValueTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.links: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationLinks: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationPromotions: MutableMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeGroups: MutableMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.singleAliases: MutableMap<String, CwtSingleAliasConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// name - config
val CwtConfigGroup.modifierCategories: MutableMap<String, CwtModifierCategoryConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - config
val CwtConfigGroup.modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

// name - config
val CwtConfigGroup.databaseObjectTypes: MutableMap<String, CwtDatabaseObjectTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedScriptedVariables: MutableMap<String, CwtExtendedScriptedVariableConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedDefinitions: MutableMap<String, MutableList<CwtExtendedDefinitionConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedGameRules: MutableMap<String, CwtExtendedGameRuleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedOnActions: MutableMap<String, CwtExtendedOnActionConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// complex_enum_name - template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedComplexEnumValues: MutableMap<String, MutableMap<String, CwtExtendedComplexEnumValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// dynamic_value_type - template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedDynamicValues: MutableMap<String, MutableMap<String, CwtExtendedDynamicValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedInlineScripts: MutableMap<String, CwtExtendedInlineScriptConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
// template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedParameters: MutableMap<String, MutableList<CwtExtendedParameterConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

@Tags(Tag.Computed)
val CwtConfigGroup.predefinedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
@Tags(Tag.Computed)
val CwtConfigGroup.generatedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

// 常量字符串的别名的组名的映射
@Tags(Tag.Computed)
val CwtConfigGroup.aliasKeysGroupConst: MutableMap<@CaseInsensitive String, MutableMap<String, String>>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
// 非常量字符串的别名的组名的映射
@Tags(Tag.Computed)
val CwtConfigGroup.aliasKeysGroupNoConst: MutableMap<String, MutableSet<String>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// 变量对应的连接规则
@Tags(Tag.Computed)
val CwtConfigGroup.linksOfVariable: MutableList<CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { mutableListOf() }

// 必定支持作用域的CWT别名规则
@Tags(Tag.Computed)
val CwtConfigGroup.aliasNamesSupportScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 必定支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 必定间接支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesIndirectSupportScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSkipCheckSystemScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 支持参数的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportParameters: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 可能有类型键前缀（type_key_prefix）的定义类型 - 按文件路径计算
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesMayWithTypeKeyPrefix: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
// 相关本地化的模式，用于从本地化导航到相关定义
@Tags(Tag.Computed)
val CwtConfigGroup.relatedLocalisationPatterns: MutableSet<Tuple2<String, String>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

@Tags(Tag.Collected)
val CwtConfigGroup.filePathExpressions: MutableSet<CwtDataExpression>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }
@Tags(Tag.Collected)
val CwtConfigGroup.parameterConfigs: MutableSet<CwtMemberConfig<*>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

// endregion

// region Mock Configs

@Tags(Tag.Computed)
val CwtConfigGroup.mockVariableConfig: CwtValueConfig
    by createKey(CwtConfigGroup.Keys) {
        CwtValueConfig.resolve(emptyPointer(), this, "value[variable]")
    }

// endregion

// region Modification Trackers

@Tags(Tag.Computed)
val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by createKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesSupportParameters
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
        ParadoxModificationTrackers.ScriptFileTracker(patterns.joinToString(";"))
    }

@Tags(Tag.Computed)
val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by createKey(CwtConfigGroup.Keys) {
        val definitionTypes = ParadoxBaseDefinitionInferredScopeContextProvider.Constants.DEFINITION_TYPES
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
        ParadoxModificationTrackers.ScriptFileTracker(patterns.joinToString(";"))
    }

// endregion

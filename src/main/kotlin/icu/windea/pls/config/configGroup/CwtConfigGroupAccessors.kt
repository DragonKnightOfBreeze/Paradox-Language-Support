package icu.windea.pls.config.configGroup

import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.extended.*
import icu.windea.pls.config.config.internal.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.priority.*

@Tags(Tag.Internal)
val CwtConfigGroup.schemas: MutableList<CwtSchemaConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableListOf() }
@Tags(Tag.Internal)
val CwtConfigGroup.foldingSettings: MutableMap<String, MutableMap<String, CwtFoldingSettingsConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
@Tags(Tag.Internal)
val CwtConfigGroup.postfixTemplateSettings: MutableMap<String, MutableMap<String, CwtPostfixTemplateSettingsConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.priorities: MutableMap<String, ParadoxPriority>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationLocalesById: MutableMap<String, CwtLocalisationLocaleConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
val CwtConfigGroup.localisationLocalesByCode: MutableMap<String, CwtLocalisationLocaleConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//type - typeConfig
val CwtConfigGroup.types: MutableMap<String, CwtTypeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//type - typeConfig
val CwtConfigGroup.swappedTypes: MutableMap<String, CwtTypeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.type2ModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//type - declarationConfig
val CwtConfigGroup.declarations: MutableMap<String, CwtDeclarationConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//enumValue可以是int、float、bool类型，统一用字符串表示
val CwtConfigGroup.enums: MutableMap<String, CwtEnumConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//基于enum_name进行定位，对应的可能是key/value
val CwtConfigGroup.complexEnums: MutableMap<String, CwtComplexEnumConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.dynamicValueTypes: MutableMap<String, CwtDynamicValueTypeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.links: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationLinks: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationPromotions: MutableMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeGroups: MutableMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.singleAliases: MutableMap<String, CwtSingleAliasConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//name - config
val CwtConfigGroup.modifierCategories: MutableMap<String, CwtModifierCategoryConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//template_expression - config
val CwtConfigGroup.modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

//name - config
val CwtConfigGroup.databaseObjectTypes: MutableMap<String, CwtDatabaseObjectTypeConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedScriptedVariables: MutableMap<String, CwtExtendedScriptedVariableConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedDefinitions: MutableMap<String, MutableList<CwtExtendedDefinitionConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedGameRules: MutableMap<String, CwtExtendedGameRuleConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedOnActions: MutableMap<String, CwtExtendedOnActionConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedInlineScripts: MutableMap<String, CwtExtendedInlineScriptConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//template_expression - configs
@Tags(Tag.Extended)
val CwtConfigGroup.extendedParameters: MutableMap<String, MutableList<CwtExtendedParameterConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//complex_enum_name - template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedComplexEnumValues: MutableMap<String, MutableMap<String, CwtExtendedComplexEnumValueConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }
//dynamic_value_type - template_expression - config
@Tags(Tag.Extended)
val CwtConfigGroup.extendedDynamicValues: MutableMap<String, MutableMap<String, CwtExtendedDynamicValueConfig>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

@Tags(Tag.Computed)
val CwtConfigGroup.predefinedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
@Tags(Tag.Computed)
val CwtConfigGroup.generatedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

//常量字符串的别名的组名的映射
@Tags(Tag.Computed)
val CwtConfigGroup.aliasKeysGroupConst: MutableMap<@CaseInsensitive String, MutableMap<String, String>>
    by createKeyDelegate(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }
//非常量字符串的别名的组名的映射
@Tags(Tag.Computed)
val CwtConfigGroup.aliasKeysGroupNoConst: MutableMap<String, MutableSet<String>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableMapOf() }

//变量对应的连接规则
@Tags(Tag.Computed)
val CwtConfigGroup.linksOfVariable: MutableList<CwtLinkConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableListOf() }
//事件目标对应的本地化连接规则
@Tags(Tag.Computed)
val CwtConfigGroup.localisationLinksOfEventTarget: MutableList<CwtLinkConfig>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableListOf() }

//必定支持作用域的CWT别名规则
@Tags(Tag.Computed)
val CwtConfigGroup.aliasNamesSupportScope: MutableSet<String>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }
//必定支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportScope: MutableSet<String>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }
//必定间接支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesIndirectSupportScope: MutableSet<String>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }
//不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSkipCheckSystemScope: MutableSet<String>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }
//支持参数的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportParameters: MutableSet<String>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }

@Tags(Tag.Collected)
val CwtConfigGroup.filePathExpressions: MutableSet<CwtDataExpression>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }
@Tags(Tag.Collected)
val CwtConfigGroup.parameterConfigs: MutableSet<CwtMemberConfig<*>>
    by createKeyDelegate(CwtConfigGroup.Keys) { mutableSetOf() }


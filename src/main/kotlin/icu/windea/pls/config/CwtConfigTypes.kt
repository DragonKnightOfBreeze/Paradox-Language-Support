package icu.windea.pls.config

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.model.constants.PlsStrings

/**
 * 所有预定义的规则类型。
 *
 * 每个规则类型对应规则文件中一种特定的声明结构，由其在规则文件中的路径模式决定。
 *
 * ### 标准规则类型
 *
 * 从标准 CWT 规则文件中解析：
 *
 * - [Type][CwtConfigTypes.Type] - 类型规则，路径：`types / type[*]`
 * - [Subtype][CwtConfigTypes.Subtype] - 子类型规则，路径：`types / type[*] / subtype[*]`
 * - [Row][CwtConfigTypes.Row] - 行规则（用于CSV），路径：`rows / row[*]`
 * - [Enum][CwtConfigTypes.Enum] - 枚举规则，路径：`enums / enum[*]`
 * - [ComplexEnum][CwtConfigTypes.ComplexEnum] - 复杂枚举规则，路径：`enums / complex_enum[*]`
 * - [EnumValue][CwtConfigTypes.EnumValue] - 枚举值，路径：`enums / enum[*] / -`（值，非属性）
 * - [DynamicValueType][CwtConfigTypes.DynamicValueType] - 动态值类型规则，路径：`values / value[*]`
 * - [DynamicValue][CwtConfigTypes.DynamicValue] - 动态值，路径：`values / value[*] / -`（值，非属性）
 * - [SingleAlias][CwtConfigTypes.SingleAlias] - 单别名规则，路径：`single_alias[*]`
 * - [Alias][CwtConfigTypes.Alias] - 别名规则，路径：`alias[*]`
 * - [Directive][CwtConfigTypes.Directive] - 指令规则，路径：`directive[*]`
 * - [Link][CwtConfigTypes.Link] - 链接规则，路径：`links / *`
 * - [LocalisationLink][CwtConfigTypes.LocalisationLink] - 本地化链接规则，路径：`localisation_links / *`
 * - [LocalisationPromotion][CwtConfigTypes.LocalisationPromotion] - 本地化提升规则，路径：`localisation_promotions / *`
 * - [LocalisationCommand][CwtConfigTypes.LocalisationCommand] - 本地化命令规则，路径：`localisation_commands / *`
 * - [ModifierCategory][CwtConfigTypes.ModifierCategory] - 修正分类规则，路径：`modifier_categories / *`
 * - [Modifier][CwtConfigTypes.Modifier] - 修正规则，路径：`modifiers / *` 或 `types / type[*] / modifiers / *` 或 `alias[modifier:*]`
 * - [Trigger][CwtConfigTypes.Trigger] - 触发器规则，路径：`alias[trigger:*]`
 * - [Effect][CwtConfigTypes.Effect] - 效果规则，路径：`alias[effect:*]`
 * - [Scope][CwtConfigTypes.Scope] - 作用域规则，路径：`scopes / *`
 * - [ScopeGroup][CwtConfigTypes.ScopeGroup] - 作用域组规则，路径：`scope_groups / *`
 * - [DatabaseObjectType][CwtConfigTypes.DatabaseObjectType] - 数据库对象类型规则，路径：`database_object_types / *`
 * - [SystemScope][CwtConfigTypes.SystemScope] - 系统作用域规则，路径：`system_scopes / *`
 * - [Locale][CwtConfigTypes.Locale] - 语言区域规则，路径：`locales / *`
 *
 * ### 扩展规则类型
 *
 * 从插件扩展的规则文件中解析（不检查元素是属性还是值）：
 *
 * - [ExtendedScriptedVariable][CwtConfigTypes.ExtendedScriptedVariable] - 封装变量扩展规则，路径：`scripted_variables / *`
 * - [ExtendedDefinition][CwtConfigTypes.ExtendedDefinition] - 定义扩展规则，路径：`definitions / *`
 * - [ExtendedGameRule][CwtConfigTypes.ExtendedGameRule] - 游戏规则扩展规则，路径：`game_rules / *`
 * - [ExtendedOnAction][CwtConfigTypes.ExtendedOnAction] - 动作触发扩展规则，路径：`on_actions / *`
 * - [ExtendedInlineScript][CwtConfigTypes.ExtendedInlineScript] - 内联脚本扩展规则，路径：`inline_scripts / *`
 * - [ExtendedParameter][CwtConfigTypes.ExtendedParameter] - 参数扩展规则，路径：`parameters / *`
 * - [ExtendedDynamicValue][CwtConfigTypes.ExtendedDynamicValue] - 动态值扩展规则，路径：`dynamic_values / * / *`
 * - [ExtendedComplexEnumValue][CwtConfigTypes.ExtendedComplexEnumValue] - 复杂枚举值扩展规则，路径：`complex_enum_values / * / *`
 *
 * @see CwtConfigType
 */
@Suppress("unused")
object CwtConfigTypes {
    /** 类型规则。路径：`types / type[*]`。 */
    val Type = CwtConfigType.builder("type")
        .icon(PlsIcons.Nodes.Type)
        .prefix(PlsStrings.typePrefix)
        .description(PlsBundle.message("cwt.config.description.type"))
        .build()
    /** 子类型规则。路径：`types / type[*] / subtype[*]`。 */
    val Subtype = CwtConfigType.builder("subtype")
        .icon(PlsIcons.Nodes.Type)
        .prefix(PlsStrings.subtypePrefix)
        .description(PlsBundle.message("cwt.config.description.subtype"))
        .build()
    /** 行规则（用于CSV）。路径：`rows / row[*]`。 */
    val Row = CwtConfigType.builder("row")
        .icon(PlsIcons.Nodes.Row)
        .prefix(PlsStrings.rowPrefix)
        .description(PlsBundle.message("cwt.config.description.row"))
        .build()
    /** 枚举规则。路径：`enums / enum[*]`。 */
    val Enum = CwtConfigType.builder("enum")
        .icon(PlsIcons.Nodes.Enum)
        .prefix(PlsStrings.enumPrefix)
        .description(PlsBundle.message("cwt.config.description.enum"))
        .build()
    /** 复杂枚举规则。路径：`enums / complex_enum[*]`。 */
    val ComplexEnum = CwtConfigType.builder("complex enum")
        .icon(PlsIcons.Nodes.Enum)
        .prefix(PlsStrings.complexEnumPrefix)
        .description(PlsBundle.message("cwt.config.description.complexEnum"))
        .build()
    /** 枚举值。路径：`enums / enum[*] / -`（值，非属性）。 */
    val EnumValue = CwtConfigType.builder("enum value", "enums").reference()
        .icon(PlsIcons.Nodes.EnumValue)
        .prefix(PlsStrings.enumValuePrefix)
        .description(PlsBundle.message("cwt.config.description.enumValue"))
        .build()
    /** 动态值类型规则。路径：`values / value[*]`。 */
    val DynamicValueType = CwtConfigType.builder("dynamic value type")
        .icon(PlsIcons.Nodes.DynamicValueType)
        .prefix(PlsStrings.dynamicValueTypePrefix)
        .description(PlsBundle.message("cwt.config.description.dynamicValueType"))
        .build()
    /** 动态值。路径：`values / value[*] / -`（值，非属性）。 */
    val DynamicValue = CwtConfigType.builder("dynamic value", "values").reference()
        .icon(PlsIcons.Nodes.DynamicValue)
        .prefix(PlsStrings.dynamicValuePrefix)
        .description(PlsBundle.message("cwt.config.description.dynamicValue"))
        .build()
    /** 单别名规则。路径：`single_alias[*]`。 */
    val SingleAlias = CwtConfigType.builder("single alias")
        .icon(PlsIcons.Nodes.Alias)
        .prefix(PlsStrings.singleAliasPrefix)
        .description(PlsBundle.message("cwt.config.description.singleAlias"))
        .build()
    /** 别名规则。路径：`alias[*]`（排除`modifier`、`trigger`、`effect`子类型）。 */
    val Alias = CwtConfigType.builder("alias")
        .icon(PlsIcons.Nodes.Alias)
        .prefix(PlsStrings.aliasPrefix)
        .description(PlsBundle.message("cwt.config.description.alias"))
        .build()
    /** 指令规则。路径：`directive[*]`。 */
    val Directive = CwtConfigType.builder("directive")
        .icon(PlsIcons.Nodes.Directive)
        .prefix(PlsStrings.directivePrefix)
        .description(PlsBundle.message("cwt.config.description.directive"))
        .build()
    /** 链接规则。路径：`links / *`。 */
    val Link = CwtConfigType.builder("link").reference()
        .icon(PlsIcons.Nodes.Link)
        .prefix(PlsStrings.linkPrefix)
        .description(PlsBundle.message("cwt.config.description.link"))
        .build()
    /** 本地化链接规则。路径：`localisation_links / *`。 */
    val LocalisationLink = CwtConfigType.builder("localisation link").reference()
        .icon(PlsIcons.Nodes.Link)
        .prefix(PlsStrings.localisationLinkPrefix)
        .description(PlsBundle.message("cwt.config.description.localisationLink"))
        .build()
    /** 本地化提升规则。路径：`localisation_promotions / *`。 */
    val LocalisationPromotion = CwtConfigType.builder("localisation promotion").reference()
        .icon(PlsIcons.Nodes.Link)
        .prefix(PlsStrings.localisationPromotionPrefix)
        .description(PlsBundle.message("cwt.config.description.localisationPromotion"))
        .build()
    /** 本地化命令规则。路径：`localisation_commands / *`。 */
    val LocalisationCommand = CwtConfigType.builder("localisation command").reference()
        .icon(PlsIcons.Nodes.LocalisationCommandField)
        .prefix(PlsStrings.localisationCommandPrefix)
        .description(PlsBundle.message("cwt.config.description.localisationCommand"))
        .build()
    /** 修正分类规则。路径：`modifier_categories / *`。 */
    val ModifierCategory = CwtConfigType.builder("modifier category").reference()
        .icon(PlsIcons.Nodes.ModifierCategory)
        .prefix(PlsStrings.modifierCategoryPrefix)
        .description(PlsBundle.message("cwt.config.description.modifierCategory"))
        .build()
    /** 修正规则。路径：`modifiers / *` 或 `types / type[*] / modifiers / *` 或 `alias[modifier:*]`。 */
    val Modifier = CwtConfigType.builder("modifier").reference()
        .icon(PlsIcons.Nodes.Modifier)
        .prefix(PlsStrings.modifierPrefix)
        .description(PlsBundle.message("cwt.config.description.modifier"))
        .build()
    /** 触发器规则。路径：`alias[trigger:*]`。 */
    val Trigger = CwtConfigType.builder("trigger").reference()
        .icon(PlsIcons.Nodes.Trigger)
        .prefix(PlsStrings.triggerPrefix)
        .description(PlsBundle.message("cwt.config.description.trigger"))
        .build()
    /** 效果规则。路径：`alias[effect:*]`。 */
    val Effect = CwtConfigType.builder("effect").reference()
        .icon(PlsIcons.Nodes.Effect)
        .prefix(PlsStrings.effectPrefix)
        .description(PlsBundle.message("cwt.config.description.effect"))
        .build()
    /** 作用域规则。路径：`scopes / *`。 */
    val Scope = CwtConfigType.builder("scope").reference()
        .icon(PlsIcons.Nodes.Scope)
        .prefix(PlsStrings.scopePrefix)
        .description(PlsBundle.message("cwt.config.description.scope"))
        .build()
    /** 作用域组规则。路径：`scope_groups / *`。 */
    val ScopeGroup = CwtConfigType.builder("scope group").reference()
        .icon(PlsIcons.Nodes.Scope)
        .prefix(PlsStrings.scopeGroupPrefix)
        .description(PlsBundle.message("cwt.config.description.scopeGroup"))
        .build()
    /** 数据库对象类型规则。路径：`database_object_types / *`。 */
    val DatabaseObjectType = CwtConfigType.builder("database object type").reference()
        .icon(PlsIcons.Nodes.DatabaseObjectType)
        .prefix(PlsStrings.databaseObjectTypePrefix)
        .description(PlsBundle.message("cwt.config.description.databaseObjectType"))
        .build()
    /** 系统作用域规则。路径：`system_scopes / *`。 */
    val SystemScope = CwtConfigType.builder("system scope").reference()
        .icon(PlsIcons.Nodes.SystemScope)
        .prefix(PlsStrings.systemScopePrefix)
        .description(PlsBundle.message("cwt.config.description.systemScope"))
        .build()
    /** 语言区域规则。路径：`locales / *`。 */
    val Locale = CwtConfigType.builder("locale").reference()
        .icon(PlsIcons.Nodes.LocalisationLocale)
        .prefix(PlsStrings.localePrefix)
        .description(PlsBundle.message("cwt.config.description.locale"))
        .build()

    /** 封装变量扩展规则。路径：`scripted_variables / *`。 */
    val ExtendedScriptedVariable = CwtConfigType.builder("extended scripted variable")
        .icon(PlsIcons.Nodes.ScriptedVariableConfig)
        .prefix(PlsStrings.scriptedVariablePrefix)
        .build()
    /** 定义扩展规则。路径：`definitions / *`。 */
    val ExtendedDefinition = CwtConfigType.builder("extended definition")
        .icon(PlsIcons.Nodes.DefinitionConfig)
        .prefix(PlsStrings.definitionPrefix)
        .build()
    /** 游戏规则扩展规则。路径：`game_rules / *`。 */
    val ExtendedGameRule = CwtConfigType.builder("extended game rule")
        .icon(PlsIcons.Nodes.DefinitionConfig)
        .prefix(PlsStrings.gameRulePrefix)
        .build()
    /** 动作触发扩展规则。路径：`on_actions / *`。 */
    val ExtendedOnAction = CwtConfigType.builder("extended on action")
        .icon(PlsIcons.Nodes.DefinitionConfig)
        .prefix(PlsStrings.onActionPrefix)
        .build()
    /** 内联脚本扩展规则。路径：`inline_scripts / *`。 */
    val ExtendedInlineScript = CwtConfigType.builder("extended inline script")
        .icon(PlsIcons.Nodes.InlineScriptConfig)
        .prefix(PlsStrings.inlineScriptPrefix)
        .build()
    /** 参数扩展规则。路径：`parameters / *`。 */
    val ExtendedParameter = CwtConfigType.builder("extended parameter")
        .icon(PlsIcons.Nodes.ParameterConfig)
        .prefix(PlsStrings.parameterPrefix)
        .build()
    /** 动态值扩展规则。路径：`dynamic_values / * / *`。 */
    val ExtendedDynamicValue = CwtConfigType.builder("extended dynamic value")
        .icon(PlsIcons.Nodes.DynamicValueConfig)
        .prefix(PlsStrings.dynamicValuePrefix)
        .build()
    /** 复杂枚举值扩展规则。路径：`complex_enum_values / * / *`。 */
    val ExtendedComplexEnumValue = CwtConfigType.builder("extended complex enum value")
        .icon(PlsIcons.Nodes.EnumValueConfig)
        .prefix(PlsStrings.complexEnumValuePrefix)
        .build()
}

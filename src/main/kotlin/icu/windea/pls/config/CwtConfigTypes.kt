package icu.windea.pls.config

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons

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
 * - [EnumValue][CwtConfigTypes.EnumValue] - 枚举值，路径：`enums / enum[*] / -`（值，非属性）
 * - [ComplexEnum][CwtConfigTypes.ComplexEnum] - 复杂枚举规则，路径：`enums / complex_enum[*]`
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
 * - [ExtendedParameter][CwtConfigTypes.ExtendedParameter] - 参数扩展规则，路径：`parameters / *`
 * - [ExtendedComplexEnumValue][CwtConfigTypes.ExtendedComplexEnumValue] - 复杂枚举值扩展规则，路径：`complex_enum_values / * / *`
 * - [ExtendedDynamicValue][CwtConfigTypes.ExtendedDynamicValue] - 动态值扩展规则，路径：`dynamic_values / * / *`
 * - [ExtendedInlineScript][CwtConfigTypes.ExtendedInlineScript] - 内联脚本扩展规则，路径：`inline_scripts / *`
 *
 * @see CwtConfigType
 */
@Suppress("unused")
object CwtConfigTypes {
    /** 类型规则。路径：`types / type[*]`。 */
    val Type = CwtConfigType.builder("Type")
        .icon(PlsIcons.Configs.Type)
        .prefix("(type)")
        .description(PlsBundle.message("config.description.type"))
        .build()
    /** 子类型规则。路径：`types / type[*] / subtype[*]`。 */
    val Subtype = CwtConfigType.builder("Subtype")
        .icon(PlsIcons.Configs.Type)
        .prefix("(subtype)")
        .description(PlsBundle.message("config.description.subtype"))
        .build()
    /** 行规则（用于CSV）。路径：`rows / row[*]`。 */
    val Row = CwtConfigType.builder("Row")
        .icon(PlsIcons.Configs.Row)
        .prefix("(row)")
        .description(PlsBundle.message("config.description.row"))
        .build()
    /** 定值命名空间规则。路径：`defines / *`。 */
    val DefineNamespace = CwtConfigType.builder("DefineNamespace")
        .icon(PlsIcons.Configs.DefineNamespace)
        .prefix("(define namespace)")
        .description(PlsBundle.message("config.description.defineNamespace"))
        .build()
    /** 定值变量规则。路径：`defines / * / *`。 */
    val DefineVariable = CwtConfigType.builder("DefineVariable")
        .icon(PlsIcons.Configs.DefineVariable)
        .prefix("(define variable)")
        .description(PlsBundle.message("config.description.defineVariable"))
        .build()
    /** 枚举规则。路径：`enums / enum[*]`。 */
    val Enum = CwtConfigType.builder("Enum")
        .icon(PlsIcons.Configs.Enum)
        .prefix("(enum)")
        .description(PlsBundle.message("config.description.enum"))
        .build()
    /** 枚举值。路径：`enums / enum[*] / -`（值，非属性）。 */
    val EnumValue = CwtConfigType.builder("EnumValue", "enums").reference()
        .icon(PlsIcons.Configs.EnumValue)
        .prefix("(enum value)")
        .description(PlsBundle.message("config.description.enumValue"))
        .build()
    /** 复杂枚举规则。路径：`enums / complex_enum[*]`。 */
    val ComplexEnum = CwtConfigType.builder("ComplexEnum")
        .icon(PlsIcons.Configs.ComplexEnum)
        .prefix("(complex enum)")
        .description(PlsBundle.message("config.description.complexEnum"))
        .build()
    /** 动态值类型规则。路径：`values / value[*]`。 */
    val DynamicValueType = CwtConfigType.builder("DynamicValueType")
        .icon(PlsIcons.Configs.DynamicValueType)
        .prefix("(dynamic value type)")
        .description(PlsBundle.message("config.description.dynamicValueType"))
        .build()
    /** 动态值。路径：`values / value[*] / -`（值，非属性）。 */
    val DynamicValue = CwtConfigType.builder("DynamicValue", "values").reference()
        .icon(PlsIcons.Configs.DynamicValue)
        .prefix("(dynamic value)")
        .description(PlsBundle.message("config.description.dynamicValue"))
        .build()
    /** 单别名规则。路径：`single_alias[*]`。 */
    val SingleAlias = CwtConfigType.builder("SingleAlias")
        .icon(PlsIcons.Configs.Alias)
        .prefix("(single alias)")
        .description(PlsBundle.message("config.description.singleAlias"))
        .build()
    /** 别名规则。路径：`alias[*]`（排除`modifier`、`trigger`、`effect`子类型）。 */
    val Alias = CwtConfigType.builder("Alias")
        .icon(PlsIcons.Configs.Alias)
        .prefix("(alias)")
        .description(PlsBundle.message("config.description.alias"))
        .build()
    /** 指令规则。路径：`directive[*]`。 */
    val Directive = CwtConfigType.builder("Directive")
        .icon(PlsIcons.Configs.Directive)
        .prefix("(directive)")
        .description(PlsBundle.message("config.description.directive"))
        .build()
    /** 链接规则。路径：`links / *`。 */
    val Link = CwtConfigType.builder("Link").reference()
        .icon(PlsIcons.Configs.Link)
        .prefix("(link)")
        .description(PlsBundle.message("config.description.link"))
        .build()
    /** 本地化链接规则。路径：`localisation_links / *`。 */
    val LocalisationLink = CwtConfigType.builder("LocalisationLink").reference()
        .icon(PlsIcons.Configs.Link)
        .prefix("(localisation link)")
        .description(PlsBundle.message("config.description.localisationLink"))
        .build()
    /** 本地化提升规则。路径：`localisation_promotions / *`。 */
    val LocalisationPromotion = CwtConfigType.builder("LocalisationPromotion").reference()
        .icon(PlsIcons.Configs.LocalisationPromotion)
        .prefix("(localisation promotion)")
        .description(PlsBundle.message("config.description.localisationPromotion"))
        .build()
    /** 本地化命令规则。路径：`localisation_commands / *`。 */
    val LocalisationCommand = CwtConfigType.builder("LocalisationCommand").reference()
        .icon(PlsIcons.Configs.LocalisationCommand)
        .prefix("(localisation command)")
        .description(PlsBundle.message("config.description.localisationCommand"))
        .build()
    /** 修正分类规则。路径：`modifier_categories / *`。 */
    val ModifierCategory = CwtConfigType.builder("ModifierCategory").reference()
        .icon(PlsIcons.Configs.ModifierCategory)
        .prefix("(modifier category)")
        .description(PlsBundle.message("config.description.modifierCategory"))
        .build()
    /** 修正规则。路径：`modifiers / *` 或 `types / type[*] / modifiers / *` 或 `alias[modifier:*]`。 */
    val Modifier = CwtConfigType.builder("Modifier").reference()
        .icon(PlsIcons.Configs.Modifier)
        .prefix("(modifier)")
        .description(PlsBundle.message("config.description.modifier"))
        .build()
    /** 触发器规则。路径：`alias[trigger:*]`。 */
    val Trigger = CwtConfigType.builder("Trigger").reference()
        .icon(PlsIcons.Configs.Trigger)
        .prefix("(trigger)")
        .description(PlsBundle.message("config.description.trigger"))
        .build()
    /** 效果规则。路径：`alias[effect:*]`。 */
    val Effect = CwtConfigType.builder("Effect").reference()
        .icon(PlsIcons.Configs.Effect)
        .prefix("(effect)")
        .description(PlsBundle.message("config.description.effect"))
        .build()
    /** 作用域规则。路径：`scopes / *`。 */
    val Scope = CwtConfigType.builder("Scope").reference()
        .icon(PlsIcons.Configs.Scope)
        .prefix("(scope)")
        .description(PlsBundle.message("config.description.scope"))
        .build()
    /** 作用域组规则。路径：`scope_groups / *`。 */
    val ScopeGroup = CwtConfigType.builder("ScopeGroup").reference()
        .icon(PlsIcons.Configs.ScopeGroup)
        .prefix("(scope group)")
        .description(PlsBundle.message("config.description.scopeGroup"))
        .build()
    /** 数据库对象类型规则。路径：`database_object_types / *`。 */
    val DatabaseObjectType = CwtConfigType.builder("DatabaseObjectType").reference()
        .icon(PlsIcons.Configs.DatabaseObjectType)
        .prefix("(database object type)")
        .description(PlsBundle.message("config.description.databaseObjectType"))
        .build()
    /** 系统作用域规则。路径：`system_scopes / *`。 */
    val SystemScope = CwtConfigType.builder("SystemScope").reference()
        .icon(PlsIcons.Configs.SystemScope)
        .prefix("(system scope)")
        .description(PlsBundle.message("config.description.systemScope"))
        .build()
    /** 语言区域规则。路径：`locales / *`。 */
    val Locale = CwtConfigType.builder("Locale").reference()
        .icon(PlsIcons.Configs.Locale)
        .prefix("(locale)")
        .description(PlsBundle.message("config.description.locale"))
        .build()

    /** 封装变量扩展规则。路径：`scripted_variables / *`。 */
    val ExtendedScriptedVariable = CwtConfigType.builder("ExtendedScriptedVariable")
        .icon(PlsIcons.Configs.ExtendedScriptedVariable)
        .prefix("(scripted variable config)")
        .build()
    /** 定义扩展规则。路径：`definitions / *`。 */
    val ExtendedDefinition = CwtConfigType.builder("ExtendedDefinition")
        .icon(PlsIcons.Configs.ExtendedDefinition)
        .prefix("(definition config)")
        .build()
    /** 游戏规则扩展规则。路径：`game_rules / *`。 */
    val ExtendedGameRule = CwtConfigType.builder("ExtendedGameRule")
        .icon(PlsIcons.Configs.ExtendedGameRule)
        .prefix("(game rule config)")
        .build()
    /** 动作触发扩展规则。路径：`on_actions / *`。 */
    val ExtendedOnAction = CwtConfigType.builder("ExtendedOnAction")
        .icon(PlsIcons.Configs.ExtendedOnAction)
        .prefix("(on action config)")
        .build()
    /** 参数扩展规则。路径：`parameters / *`。 */
    val ExtendedParameter = CwtConfigType.builder("ExtendedParameter")
        .icon(PlsIcons.Configs.ExtendedParameter)
        .prefix("(parameter config)")
        .build()
    /** 复杂枚举值扩展规则。路径：`complex_enum_values / * / *`。 */
    val ExtendedComplexEnumValue = CwtConfigType.builder("ExtendedComplexEnumValue")
        .icon(PlsIcons.Configs.ExtendedComplexEnumValue)
        .prefix("(complex enum value config)")
        .build()
    /** 动态值扩展规则。路径：`dynamic_values / * / *`。 */
    val ExtendedDynamicValue = CwtConfigType.builder("ExtendedDynamicValue")
        .icon(PlsIcons.Configs.ExtendedDynamicValue)
        .prefix("(dynamic value config)")
        .build()
    /** 内联脚本扩展规则。路径：`inline_scripts / *`。 */
    val ExtendedInlineScript = CwtConfigType.builder("ExtendedInlineScript")
        .icon(PlsIcons.Configs.ExtendedInlineScript)
        .prefix("(inline script config)")
        .build()
}

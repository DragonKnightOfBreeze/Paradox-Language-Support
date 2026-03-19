package icu.windea.pls.config

import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.model.ParadoxGameType

/**
 * 所有预定义的数据类型。
 *
 * 每个数据类型对应规则表达式中一种特定的取值形态，决定了规则表达式如何从字符串解析，以及如何与脚本表达式匹配。
 *
 * ### 基本类型
 *
 * - [Any][CwtDataTypes.Any] - 匹配任意表达式（后备）
 * - [Bool][CwtDataTypes.Bool] - 布尔值（`yes` / `no`）
 * - [Int][CwtDataTypes.Int] - 整数，可带范围约束
 * - [Float][CwtDataTypes.Float] - 浮点数，可带范围约束
 * - [Scalar][CwtDataTypes.Scalar] - 标量（字符串、数字、布尔值均可）
 * - [ColorField][CwtDataTypes.ColorField] - 颜色字段
 * - [Block][CwtDataTypes.Block] - 子句（花括号块）
 *
 * ### 扩展基本类型
 *
 * - [PercentageField][CwtDataTypes.PercentageField] - 百分比字段
 * - [DateField][CwtDataTypes.DateField] - 日期字段，可带格式约束
 *
 * ### 引用类型
 *
 * - [Definition][CwtDataTypes.Definition] - 定义引用
 * - [Localisation][CwtDataTypes.Localisation]
 *   / [SyncedLocalisation][CwtDataTypes.SyncedLocalisation]
 *   / [InlineLocalisation][CwtDataTypes.InlineLocalisation] - 本地化引用
 * - [Modifier][CwtDataTypes.Modifier] - 修正引用
 * - [AbsoluteFilePath][CwtDataTypes.AbsoluteFilePath]
 *   / [FilePath][CwtDataTypes.FilePath]
 *   / [FileName][CwtDataTypes.FileName]
 *   / [Icon][CwtDataTypes.Icon] - 文件路径引用
 * - [EnumValue][CwtDataTypes.EnumValue] - 枚举值引用（简单枚举或复杂枚举）
 * - [Value][CwtDataTypes.Value]
 *   / [ValueSet][CwtDataTypes.ValueSet]
 *   / [DynamicValue][CwtDataTypes.DynamicValue] - 动态值表达式
 * - [ScopeField][CwtDataTypes.ScopeField]
 *   / [Scope][CwtDataTypes.Scope]
 *   / [ScopeGroup][CwtDataTypes.ScopeGroup] - 作用域字段表达式
 * - [ValueField][CwtDataTypes.ValueField]
 *   / [IntValueField][CwtDataTypes.IntValueField]
 *   / [VariableField][CwtDataTypes.VariableField]
 *   / [IntVariableField][CwtDataTypes.IntVariableField] - 值/变量字段表达式
 *
 * ### 别名与指令类型
 *
 * - [SingleAliasRight][CwtDataTypes.SingleAliasRight]
 *   / [AliasName][CwtDataTypes.AliasName]
 *   / [AliasKeysField][CwtDataTypes.AliasKeysField]
 *   / [AliasMatchLeft][CwtDataTypes.AliasMatchLeft] - 别名相关（不直接参与脚本匹配，由别名解析机制处理）
 *
 * ### 参数类型
 *
 * - [Parameter][CwtDataTypes.Parameter]
 *   / [ParameterValue][CwtDataTypes.ParameterValue]
 *   / [LocalisationParameter][CwtDataTypes.LocalisationParameter] - 参数相关
 *
 * ### 特殊类型
 *
 * - [Command][CwtDataTypes.Command] - 命令表达式（如 `Root.GetName`）
 * - [DefineReference][CwtDataTypes.DefineReference] - 定值引用表达式（如 `define:NPortrait|GRACEFUL_AGING_START`）
 * - [DatabaseObject][CwtDataTypes.DatabaseObject] - 数据库对象表达式（如 `civic:x:y`）
 * - [StellarisNameFormat][CwtDataTypes.StellarisNameFormat] - Stellaris 名称格式表达式
 * - [ShaderEffect][CwtDataTypes.ShaderEffect] - 着色器效果
 * - [TechnologyWithLevel][CwtDataTypes.TechnologyWithLevel] - 带等级的科技（如 `some_repeatable_tech@1`）
 *
 * ### 模式感知类型
 *
 * - [Constant][CwtDataTypes.Constant] - 常量（精确匹配）
 * - [TemplateExpression][CwtDataTypes.TemplateExpression] - 模板表达式（如 `a_<b>_enum[c]`）
 * - [Ant][CwtDataTypes.Ant] - Ant路径模式（如 `ant:/foo/bar?/abc*`）
 * - [Regex][CwtDataTypes.Regex] - 正则表达式模式（如 `re:foo.*bar`）
 *
 * ### 后缀感知类型
 *
 * - [SuffixAwareDefinition][CwtDataTypes.SuffixAwareDefinition]
 *   / [SuffixAwareLocalisation][CwtDataTypes.SuffixAwareLocalisation]
 *   / [SuffixAwareSyncedLocalisation][CwtDataTypes.SuffixAwareSyncedLocalisation] - 带后缀列表的引用类型
 *
 * @see CwtDataType
 * @see CwtDataTypeSets
 */
@Suppress("unused")
object CwtDataTypes {
    /**
     * 任意类型。规则表达式：`$any`。
     *
     * 匹配任意脚本表达式，作为最低优先级的后备匹配。
     */
    val Any = CwtDataType.builder("Any")
        .withPriority(1.0) // very low
        .build()
    /**
     * 布尔类型。规则表达式：`bool`。
     *
     * 匹配脚本中的布尔值（`yes` / `no`）。
     */
    val Bool = CwtDataType.builder("Bool")
        .withPriority(100.0) // highest
        .build()
    /**
     * 整数类型。规则表达式：`int` 或 `int(min..max)`。
     *
     * 匹配脚本中的整数值。带范围参数时，还会验证值是否在指定范围内。
     * 用引号括起的数字也视为匹配（兼容原版游戏文件）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see IntRangeInfo
     */
    val Int = CwtDataType.builder("Int")
        .withPriority(90.0) // very high
        .build()
    /**
     * 浮点数类型。规则表达式：`float` 或 `float(min..max)`。
     *
     * 匹配脚本中的浮点数值。带范围参数时，还会验证值是否在指定范围内。
     * 用引号括起的数字也视为匹配（兼容原版游戏文件）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see FloatRangeInfo
     */
    val Float = CwtDataType.builder("Float")
        .withPriority(90.0) // very high
        .build()
    /**
     * 标量类型。规则表达式：`scalar` 或 `wildcard_scalar`。
     *
     * 匹配脚本中的大多数非子句表达式（字符串、数字、布尔值等），作为低优先级的宽泛匹配。
     * 作为键时总是匹配。`wildcard_scalar` 变体会设置通配符标记。
     */
    val Scalar = CwtDataType.builder("Scalar")
        .withPriority(2.0) // very low
        .build()
    /**
     * 颜色字段类型。规则表达式：`colour_field`、`color_field`、`colour[type]` 或 `color[type]`。
     *
     * 匹配脚本中的颜色值。带参数时（如 `colour[rgb]`），还会验证颜色类型前缀。
     */
    val ColorField = CwtDataType.builder("ColorField")
        .withPriority(90.0) // very high
        .build()
    /**
     * 子句类型。仅用于内部表示，不对应规则表达式字符串。
     *
     * 匹配脚本中的花括号块（`{ ... }`）。要求值位置且表达式类型为 Block，并递归匹配子句内容。
     */
    val Block = CwtDataType.builder("Block")
        .withPriority(100.0) // highest
        .build()

    /**
     * 百分比字段类型。规则表达式：`percentage_field`。
     *
     * 匹配脚本中的百分比值字符串（如 `50%`）。
     */
    val PercentageField = CwtDataType.builder("PercentageField")
        .withPriority(90.0)
        .build()
    /**
     * 日期字段类型。规则表达式：`date_field` 或 `date_field[format]`。
     *
     * 匹配脚本中的日期值字符串（如 `2200.1.1`）。带参数时还会验证日期格式。
     */
    val DateField = CwtDataType.builder("DateField")
        .withPriority(90.0)
        .build()

    /**
     * 定义引用类型。规则表达式：`<type>` 或 `<type.subtype>`。
     *
     * 匹配脚本中对指定类型定义的引用。表达式须为合法标识符（允许 `.` 和 `-`），
     * 可以是整数或浮点数（如 `<technology_tier>` 的情况）。匹配时验证引用的定义是否存在。
     */
    val Definition = CwtDataType.builder("Definition").reference()
        .withPriority(70.0)
        .build()
    /**
     * 本地化引用类型。规则表达式：`localisation`。
     *
     * 匹配脚本中对本地化键的引用。表达式须为合法标识符（允许`.`、`-`、`'`）。
     * 匹配时验证引用的本地化是否存在。
     */
    val Localisation = CwtDataType.builder("Localisation").reference()
        .withPriority(60.0)
        .build()
    /**
     * 同步本地化引用类型。规则表达式：`localisation_synced`。
     *
     * 与 [Localisation][CwtDataTypes.Localisation] 类似，但指向同步本地化键。
     */
    val SyncedLocalisation = CwtDataType.builder("SyncedLocalisation").reference()
        .withPriority(60.0)
        .build()
    /**
     * 内联本地化引用类型。规则表达式：`localisation_inline`。
     *
     * 匹配本地化键引用或用引号括起的任意字符串（后者作为内联文本，以后备匹配返回）。
     */
    val InlineLocalisation = CwtDataType.builder("InlineLocalisation").reference()
        .withPriority(60.0)
        .build()
    /**
     * 修正引用类型。规则表达式：`<modifier>`。
     *
     * 匹配脚本中对修正（modifier）的引用。表达式须为合法标识符。
     * 匹配时验证引用的修正是否在规则组中存在。优先级高于 [Definition][CwtDataTypes.Definition]。
     */
    val Modifier = CwtDataType.builder("Modifier").reference()
        .withPriority(75.0) // higher than Definition
        .build()

    /**
     * 绝对文件路径类型。规则表达式：`abs_filepath`。
     *
     * 匹配脚本中的绝对文件路径字符串。匹配时仅验证为字符串类型（通配匹配）。
     */
    val AbsoluteFilePath = CwtDataType.builder("AbsoluteFilePath").reference()
        .withPriority(70.0)
        .build()
    /**
     * 图标路径类型。规则表达式：`icon[path]`。
     *
     * 匹配脚本中对图标文件的路径引用。匹配时验证路径引用的文件是否存在。
     */
    val Icon = CwtDataType.builder("Icon").reference()
        .withPriority(70.0)
        .build()
    /**
     * 文件路径类型。规则表达式：`filepath`、`filepath[./]` 或 `filepath[path]`。
     *
     * 匹配脚本中对文件的路径引用。匹配时验证路径引用的文件是否存在。
     */
    val FilePath = CwtDataType.builder("FilePath").reference()
        .withPriority(70.0)
        .build()
    /**
     * 文件名类型。规则表达式：`filename` 或 `filename[path]`。
     *
     * 匹配脚本中对文件名的引用。匹配时验证路径引用的文件是否存在。
     */
    val FileName = CwtDataType.builder("FileName").reference()
        .withPriority(70.0)
        .build()

    /**
     * 枚举值类型。规则表达式：`enum[name]`。
     *
     * 匹配脚本中对枚举值的引用。
     * 匹配简单枚举时精确匹配枚举值列表，匹配复杂枚举时则通过索引查询。
     */
    val EnumValue = CwtDataType.builder("EnumValue").reference()
        .withPriority { configExpression, configGroup ->
            val enumName = configExpression.value ?: return@withPriority 0.0 // unexpected
            if (configGroup.enums.containsKey(enumName)) return@withPriority 80.0
            if (configGroup.complexEnums.containsKey(enumName)) return@withPriority 45.0
            0.0 // unexpected
        }
        .build()

    /**
     * 动态值读取类型。规则表达式：`value[name]`。
     *
     * 匹配脚本中的动态值表达式，表示对已声明动态值的读取引用。
     * 动态值的名字须为合法标识符（允许`.`）。
     *
     * @see ParadoxDynamicValueExpression
     */
    val Value = CwtDataType.builder("Value").reference()
        .withPriority(40.0)
        .build()
    /**
     * 动态值写入类型。规则表达式：`value_set[name]`。
     *
     * 匹配脚本中的动态值表达式，表示对动态值的写入（声明）引用。
     * 动态值的名字须为合法标识符（允许`.`）。
     *
     * @see ParadoxDynamicValueExpression
     */
    val ValueSet = CwtDataType.builder("ValueSet").reference()
        .withPriority(40.0)
        .build()
    /**
     * 动态值类型。规则表达式：`dynamic_value[name]`。
     *
     * 匹配脚本中的动态值表达式，表示对动态值的引用（不区分读写）。
     * 动态值的名字须为合法标识符（允许`.`）。
     *
     * @see ParadoxDynamicValueExpression
     */
    val DynamicValue = CwtDataType.builder("DynamicValue").reference()
        .withPriority(40.0)
        .build()

    /**
     * 作用域字段类型。规则表达式：`scope_field`。
     *
     * 匹配脚本中的作用域字段表达式（可包含作用域链，如 `root.owner`）。
     *
     * @see ParadoxScopeFieldExpression
     */
    val ScopeField = CwtDataType.builder("ScopeField").reference()
        .withPriority(50.0)
        .build()
    /**
     * 作用域类型。规则表达式：`scope[type]`。
     *
     * 匹配脚本中的作用域字段表达式，同时约束输出作用域类型。
     * 参数为 `any` 时等同于 [ScopeField][CwtDataTypes.ScopeField]。
     *
     * @see ParadoxScopeFieldExpression
     */
    val Scope = CwtDataType.builder("Scope").reference()
        .withPriority(50.0)
        .build()
    /**
     * 作用域组类型。规则表达式：`scope_group[name]`。
     *
     * 匹配脚本中的作用域字段表达式，约束输出作用域属于指定的作用域组。
     *
     * @see ParadoxScopeFieldExpression
     */
    val ScopeGroup = CwtDataType.builder("ScopeGroup").reference()
        .withPriority(50.0)
        .build()

    /**
     * 值字段类型。规则表达式：`value_field` 或 `value_field(min..max)`。
     *
     * 匹配浮点数或值字段表达式（可包含作用域链和动态值引用）。
     * 带范围参数时还会限制数值范围。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see ParadoxValueFieldExpression
     * @see FloatRangeInfo
     */
    val ValueField = CwtDataType.builder("ValueField").reference()
        .withPriority(45.0)
        .build()
    /**
     * 整数值字段类型。规则表达式：`int_value_field` 或 `int_value_field(min..max)`。
     *
     * 匹配整数或整数值字段表达式（可包含作用域链和动态值引用）。
     * 带范围参数时还会限制数值范围。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see ParadoxValueFieldExpression
     * @see IntRangeInfo
     */
    val IntValueField = CwtDataType.builder("IntValueField").reference()
        .withPriority(45.0)
        .build()

    /**
     * 变量字段类型。规则表达式：`variable_field`、`variable_field(min..max)`（或 `variable_field_32` 变体）。
     *
     * 匹配浮点数或变量字段表达式（可包含作用域链和封装变量引用）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see ParadoxVariableFieldExpression
     * @see FloatRangeInfo
     */
    val VariableField = CwtDataType.builder("VariableField").reference()
        .withPriority(45.0)
        .build()
    /**
     * 整数变量字段类型。规则表达式：`int_variable_field`、`int_variable_field(min..max)`（或 `int_variable_field_32` 变体）。
     *
     * 匹配整数或整数变量字段表达式（可包含作用域链和封装变量引用）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * @see ParadoxVariableFieldExpression
     * @see IntRangeInfo
     */
    val IntVariableField = CwtDataType.builder("IntVariableField").reference()
        .withPriority(45.0)
        .build()

    /**
     * 单别名右侧类型。规则表达式：`single_alias_right[name]`。
     *
     * 不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值。
     */
    val SingleAliasRight = CwtDataType.builder("SingleAliasRight").reference().build()
    /**
     * 别名键字段类型。规则表达式：`alias_keys_field[name]`。
     *
     * 匹配时解析别名子键并递归匹配。
     */
    val AliasKeysField = CwtDataType.builder("AliasKeysField").reference().build()
    /**
     * 别名名称类型。规则表达式：`alias_name[name]`。
     *
     * 匹配时解析别名子键并递归匹配。只能用来匹配属性键，且需要与 [AliasMatchLeft][CwtDataTypes.AliasMatchLeft] 组合使用。
     */
    val AliasName = CwtDataType.builder("AliasName").reference().build()
    /**
     * 别名匹配左侧类型。规则表达式：`alias_match_left[name]`。
     *
     * 不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值，且需要与 [AliasName][CwtDataTypes.AliasName] 组合使用。
     */
    val AliasMatchLeft = CwtDataType.builder("AliasMatchLeft").reference().build()

    /**
     * 参数名类型。规则表达式：`$parameter`。
     *
     * 匹配脚本中的参数名。表达式须为合法标识符。即使对应的定义声明中不存在该参数名，也视为匹配。
     */
    val Parameter = CwtDataType.builder("Parameter").reference()
        .withPriority(10.0)
        .build()
    /**
     * 参数值类型。规则表达式：`$parameter_value`。
     *
     * 匹配脚本中的参数值。只要不是子句即可匹配。
     */
    val ParameterValue = CwtDataType.builder("ParameterValue").reference()
        .withPriority(90.0) // same to Scalar
        .build()
    /**
     * 本地化参数名类型。规则表达式：`$localisation_parameter`。
     *
     * 匹配脚本中的本地化参数名。表达式须为合法标识符（允许`.`、`-`、`'`）。
     */
    val LocalisationParameter = CwtDataType.builder("LocalisationParameter").reference()
        .withPriority(10.0)
        .build()

    /**
     * 命令表达式类型。规则表达式：`$command`。
     *
     * 匹配脚本中的命令表达式（如 `Root.GetName`）。目前不支持用来匹配脚本表达式。
     *
     * @see ParadoxCommandExpression
     * @since 2.1.1
     */
    val Command = CwtDataType.builder("Command").reference()
        .withPriority(45.0)
        .build()
    /**
     * 定值引用表达式类型。规则表达式：`$define_reference`。
     *
     * 匹配脚本中的定值引用表达式（如 `define:NPortrait|GRACEFUL_AGING_START`）。
     *
     * @see ParadoxDefineReferenceExpression
     * @since 1.3.25
     */
    val DefineReference = CwtDataType.builder("DefineReference").reference()
        .withPriority(60.0)
        .build()
    /**
     * 数据库对象表达式类型。规则表达式：`$database_object`。
     *
     * 匹配脚本中的数据库对象表达式（如`civic:x:y`），由冒号分隔的多段引用组成。
     *
     * @see ParadoxDatabaseObjectExpression
     * @since 1.3.9
     */
    // @WithGameType(ParadoxGameType.Stellaris) // not limited yet
    val DatabaseObject = CwtDataType.builder("DatabaseObject").reference()
        .withPriority(60.0)
        .build()
    /**
     * Stellaris 名称格式表达式类型。规则表达式：`stellaris_name_format[type]`。
     *
     * 匹配脚本中的 Stellaris 名称格式表达式。仅限 Stellaris 游戏类型。
     *
     * @see StellarisNameFormatExpression
     */
    @WithGameType(ParadoxGameType.Stellaris)
    val StellarisNameFormat = CwtDataType.builder("StellarisNameFormat").reference()
        .withPriority(60.0)
        .build()

    /**
     * 着色器效果类型。规则表达式：`$shader_effect`。
     *
     * 匹配 `.shader` 文件中的效果声明。目前作为一般的字符串处理（后备匹配）。
     */
    // effects in .shader files
    val ShaderEffect = CwtDataType.builder("ShaderEffect"/*).reference(*/)
        .withPriority(85.0)
        .build()
    /**
     * 带等级的科技类型。规则表达式：`<technology_with_level>`。
     *
     * 匹配脚本中的带等级科技引用（如 `some_repeatable_tech@1`），通过 `@` 分隔科技名和等级。
     * 仅限 Stellaris 游戏类型。优先级低于 [Definition][CwtDataTypes.Definition]。
     */
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType.builder("TechnologyWithLevel").reference()
        .withPriority(69.0) // lower than Definition
        .build()

    // Pattern Aware Data Types

    /**
     * 常量类型（模式感知）。解析为此类型时，表达式字符串即为常量值本身。
     *
     * 匹配脚本中与常量值完全相同的表达式。作为值时，常量`yes`/`no`不匹配用引号括起的表达式。
     * 不含特殊字符（`:` `.` `@` `[` `]` `<` `>`）的表达式字符串解析为此类型。
     */
    val Constant = CwtDataType.builder("Constant").patternAware()
        .withPriority(100.0) // highest
        .build()
    /**
     * 模板表达式类型（模式感知）。规则表达式示例：`a_<b>_enum[c]_value[d]`。
     *
     * 由常量文本片段和引用占位符（如 `<type>`、`enum[name]`、`value[name]`）交替组成的模式。
     * 匹配时将脚本表达式按模板结构拆分，逐个验证各引用片段。
     *
     * @see ParadoxTemplateExpression
     */
    val TemplateExpression = CwtDataType.builder("TemplateExpression").patternAware()
        .withPriority(65.0)
        .build()
    /**
     * Ant路径模式类型（模式感知）。规则表达式：`ant:pattern` 或 `ant.i:pattern`（忽略大小写）。
     *
     * 匹配脚本中符合Ant路径模式的表达式。支持通配符`?`（单字符）、`*`（单段）和`**`（多段，不常用）。
     *
     * @since 1.3.6
     */
    val Ant = CwtDataType.builder("Ant").patternAware()
        .build()
    /**
     * 正则表达式模式类型（模式感知）。规则表达式：`re:pattern` 或 `re.i:pattern`（忽略大小写）。
     *
     * 匹配脚本中符合正则表达式的表达式。
     *
     * @since 1.3.6
     */
    val Regex = CwtDataType.builder("Regex").patternAware()
        .build()

    // Suffix Aware Data Types

    // TODO SUFFIX_AWARE 目前不兼容/不支持：代码补全、用法查询

    /**
     * 后缀感知定义引用类型。规则表达式：`<type>|suffix1,suffix2,...`。
     *
     * 由基础定义引用和逗号分隔的后缀列表组成。匹配时同时验证定义引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [Definition][CwtDataTypes.Definition]。
     *
     * @since 2.0.5
     */
    // #162, #193
    val SuffixAwareDefinition = CwtDataType.builder("SuffixAwareDefinition").suffixAware().build()
    /**
     * 后缀感知本地化引用类型。规则表达式：`localisation|suffix1,suffix2,...`。
     *
     * 由基础本地化引用和逗号分隔的后缀列表组成。匹配时同时验证本地化引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [Localisation][CwtDataTypes.Localisation]。
     *
     * @since 2.0.5
     */
    // #162, #193
    val SuffixAwareLocalisation = CwtDataType.builder("SuffixAwareLocalisation").suffixAware().build()
    /**
     * 后缀感知同步本地化引用类型。规则表达式：`localisation_synced|suffix1,suffix2,...`。
     *
     * 由基础同步本地化引用和逗号分隔的后缀列表组成。匹配时同时验证同步本地化引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [SyncedLocalisation][CwtDataTypes.SyncedLocalisation]。
     *
     * @since 2.0.5
     */
    // #162, #193
    val SuffixAwareSyncedLocalisation = CwtDataType.builder("SuffixAwareLocalisationSynced").suffixAware().build()
}

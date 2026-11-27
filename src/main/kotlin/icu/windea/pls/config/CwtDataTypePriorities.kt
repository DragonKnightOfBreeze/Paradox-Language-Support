package icu.windea.pls.config

/**
 * 所有预定义的数据类型优先级。
 *
 * 数据类型优先级用于在存在多个候选规则时，优先选择优先级更高的数据表达式。数值越大，优先级越高。
 * 未指定静态优先级或动态优先级的数据类型会回退到默认优先级 [DEFAULT]（0.0）。
 *
 * 这些数值在部分匹配场景下极其敏感（例如模板与定义引用、片段与本地化引用，以及各种引用类型之间的竞争），
 * 调整时必须确保所有受影响的数据类型之间的相对顺序保持不变。
 *
 * 备注：以下按优先级从高到低列出各层级常量，并说明其适用的数据类型以及与其他敏感类型之间的相对高低关系。
 *
 * @see CwtDataType
 * @see CwtDataTypes
 */
@Suppress("unused")
object CwtDataTypePriorities {
    /**
     * 最高层级优先级（1000.0）。
     *
     * 适用于块类型 [CwtDataTypes.Block] 和常量类型 [CwtDataTypes.Constant]。
     * 二者都是最精确的匹配形态，因此高于所有其他层级。
     */
    const val HIGHEST = 1000.0

    /**
     * 布尔匹配优先级（100.0）。
     *
     * 适用于布尔类型 [CwtDataTypes.Bool]。
     * 仅次于 [HIGHEST]，高于 [HIGH]，以确保布尔字面量优先于整数、浮点数和标量等通用数值匹配。
     */
    const val BOOL = 100.0

    /**
     * 高优先级（90.0）。
     *
     * 适用于整数 [CwtDataTypes.Int]、浮点数 [CwtDataTypes.Float]、颜色字段 [CwtDataTypes.ColorField]、
     * 百分比字段 [CwtDataTypes.PercentageField]、整数百分比字段 [CwtDataTypes.IntPercentageField]、
     * 日期字段 [CwtDataTypes.DateField] 以及参数值 [CwtDataTypes.ParameterValue]。
     * 低于 [BOOL] 和 [HIGHEST]，高于 [ENUM_VALUE]。
     */
    const val HIGH = 90.0

    /**
     * 简单枚举值优先级（80.0）。
     *
     * 适用于存在于 `enums` 中的简单枚举值类型 [CwtDataTypes.EnumValue]。
     * 低于 [HIGH]，高于 [MODIFIER] 和 [DEFINITION]。
     */
    const val ENUM_VALUE = 80.0

    /**
     * 修正引用优先级（75.0）。
     *
     * 适用于修正引用类型 [CwtDataTypes.Modifier]。
     * 高于 [TEMPLATE] 和 [DEFINITION]，低于 [ENUM_VALUE]。
     */
    const val MODIFIER = 75.0

    /**
     * 模板优先级（72.0）。
     *
     * 适用于模板表达式类型 [CwtDataTypes.Template]。
     * 模板比裸的定义引用更具体，因此高于 [DEFINITION]，但低于 [MODIFIER]。
     */
    const val TEMPLATE = 72.0

    /**
     * 定义引用优先级（70.0）。
     *
     * 适用于定义引用类型 [CwtDataTypes.Definition]。
     * 高于 [TECHNOLOGY_WITH_LEVEL] 和 [SNIPPET]，低于 [TEMPLATE]。
     */
    const val DEFINITION = 70.0

    /**
     * 带等级科技引用优先级（69.0）。
     *
     * 适用于带等级科技引用类型 [CwtDataTypes.TechnologyWithLevel]（仅限 Stellaris）。
     * 略低于 [DEFINITION]，高于 [SNIPPET]。
     */
    const val TECHNOLOGY_WITH_LEVEL = 69.0

    /**
     * 片段优先级（65.0）。
     *
     * 适用于定义引用片段 [CwtDataTypes.DefinitionSnippet] 和本地化引用片段 [CwtDataTypes.LocalisationSnippet]。
     * 低于 [DEFINITION] 和 [TECHNOLOGY_WITH_LEVEL]，高于 [REFERENCE]。
     */
    const val SNIPPET = 65.0

    /**
     * 通用引用优先级（60.0）。
     *
     * 适用于本地化相关的引用（[CwtDataTypes.Localisation]、[CwtDataTypes.SyncedLocalisation]、[CwtDataTypes.InlineLocalisation]）、
     * 脚本值引用 [CwtDataTypes.ScriptValueReference]、定值引用 [CwtDataTypes.DefineReference] 与数组定值引用 [CwtDataTypes.ArrayDefineReference]、
     * 标签集 [CwtDataTypes.Tags]、数据库对象 [CwtDataTypes.DatabaseObject] 以及命名格式 [CwtDataTypes.NameFormat]。
     * 低于 [SNIPPET]，高于 [PATH_REFERENCE]。
     */
    const val REFERENCE = 60.0

    /**
     * 路径引用优先级（50.0）。
     *
     * 适用于路径引用类型 [CwtDataTypes.Icon]、[CwtDataTypes.FilePath]、[CwtDataTypes.FileName] 和 [CwtDataTypes.AbsoluteFilePath]。
     * 低于 [REFERENCE]，高于 [COMPLEX_ENUM_VALUE]。
     */
    const val PATH_REFERENCE = 50.0

    /**
     * 复杂枚举值优先级（45.0）。
     *
     * 适用于存在于 `complexEnums` 中的复杂枚举值类型 [CwtDataTypes.EnumValue]。
     * 低于 [PATH_REFERENCE]，高于 [SCOPE]。
     */
    const val COMPLEX_ENUM_VALUE = 45.0

    /**
     * 作用域优先级（40.0）。
     *
     * 适用于作用域相关类型 [CwtDataTypes.ScopeField]、[CwtDataTypes.Scope] 和 [CwtDataTypes.ScopeGroup]。
     * 低于 [COMPLEX_ENUM_VALUE]，高于 [VALUE_FIELD]。
     */
    const val SCOPE = 40.0

    /**
     * 值字段优先级（35.0）。
     *
     * 适用于值字段与变量字段类型 [CwtDataTypes.ValueField]、[CwtDataTypes.IntValueField]、[CwtDataTypes.VariableField]、[CwtDataTypes.IntVariableField]，
     * 以及命令表达式 [CwtDataTypes.Command]。
     * 低于 [SCOPE]，高于 [DYNAMIC_VALUE]。
     */
    const val VALUE_FIELD = 35.0

    /**
     * 动态值优先级（30.0）。
     *
     * 适用于动态值相关类型 [CwtDataTypes.Value]、[CwtDataTypes.ValueSet] 和 [CwtDataTypes.DynamicValue]。
     * 低于 [VALUE_FIELD]，高于 [EXTERNAL_REFERENCE]。
     */
    const val DYNAMIC_VALUE = 30.0

    /**
     * 外部引用优先级（20.0）。
     *
     * 适用于外部引用类型 [CwtDataTypes.ShaderEffect] 和 [CwtDataTypes.MeshLocator]。
     * 低于 [DYNAMIC_VALUE]，高于 [PARAMETER_REFERENCE]。
     */
    const val EXTERNAL_REFERENCE = 20.0

    /**
     * 参数引用优先级（10.0）。
     *
     * 适用于参数名 [CwtDataTypes.Parameter] 和本地化参数名 [CwtDataTypes.LocalisationParameter]。
     * 低于所有引用类型，高于 [SCALAR]。
     */
    const val PARAMETER_REFERENCE = 10.0

    /**
     * 标量优先级（9.0）。
     *
     * 适用于标量类型 [CwtDataTypes.Scalar]。
     * 低于 [PARAMETER_REFERENCE]，高于 [WILDCARD_LITERAL]。
     */
    const val SCALAR = 9.0

    /**
     * 通配字面量优先级（7.0）。
     *
     * 适用于通配形式的字面量类型 [CwtDataTypes.WildcardLiteral]。
     * 高于普通字面量 [LITERAL]，低于 [SCALAR]。
     */
    const val WILDCARD_LITERAL = 7.0

    /**
     * 字面量优先级（6.0）。
     *
     * 适用于字面量类型 [CwtDataTypes.Literal]。
     * 低于 [WILDCARD_LITERAL]，高于 [WILDCARD_ANY]。
     */
    const val LITERAL = 6.0

    /**
     * 通配任意优先级（2.0）。
     *
     * 适用于通配形式的任意类型 [CwtDataTypes.WildcardAny]。
     * 高于普通任意类型 [ANY]，低于 [LITERAL]。
     */
    const val WILDCARD_ANY = 2.0

    /**
     * 任意类型优先级（1.0）。
     *
     * 适用于任意类型 [CwtDataTypes.Any]。
     * 作为最低优先级的通配匹配策略，仅高于 [DEFAULT]。
     */
    const val ANY = 1.0

    /**
     * 默认优先级（0.0）。
     *
     * 未指定静态优先级或动态优先级的数据类型会回退到该值，例如 [CwtDataTypes.UnionValue]、[CwtDataTypes.AliasKeysField]、
     * [CwtDataTypes.AliasName]、[CwtDataTypes.AliasMatchLeft]、[CwtDataTypes.SingleAliasRight]、[CwtDataTypes.Glob]、
     * [CwtDataTypes.Ant]、[CwtDataTypes.Regex]，以及动态枚举未命中时的回退值。低于所有显式定义的优先级。
     */
    const val DEFAULT = 0.0
}

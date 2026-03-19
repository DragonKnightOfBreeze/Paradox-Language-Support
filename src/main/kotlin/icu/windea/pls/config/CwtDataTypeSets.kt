package icu.windea.pls.config

/**
 * 数据类型的预定义分组。
 *
 * 将语义相关的 [CwtDataType]实例组织为数组，用于匹配逻辑中的批量判断和功能分派。
 *
 * ### 基本语义分组
 *
 * - [Int] - 整数相关类型（含整数值字段和整数变量字段）
 * - [Float] - 浮点数相关类型（含值字段和变量字段）
 * - [PathReference] - 路径引用类型
 * - [LocalisationReference] - 本地化引用类型
 *
 * ### 复杂表达式分组
 *
 * - [DynamicValue] - 动态值相关类型
 * - [ScopeField] - 作用域字段相关类型
 * - [ValueField] - 值字段表达式类型
 * - [VariableField] - 变量字段表达式类型
 *
 * ### 功能性分组
 *
 * - [ConstantAware] - 可包含常量文本的类型（用于常量提取等）
 * - [DefinitionAware] - 可解析为定义引用的类型
 * - [LocalisationAware] - 可解析为本地化引用的类型
 * - [ImageLocationAware] - 可定位图像资源的类型
 * - [LocalisationLocationAware] - 可定位本地化资源的类型
 * - [AliasNameAware] - 别名名称相关类型
 * - [PatternAware] - 所有模式感知类型
 * - [SuffixAware] - 所有后缀感知类型
 *
 * ### 组合分组
 *
 * - [DynamicValueInvolved] - 涉及动态值的所有类型（动态值 + 作用域字段 + 值字段 + 变量字段）
 * - [ParameterInvolved] - 涉及参数的类型
 * - [LocalisationParameterInvolved] - 涉及本地化参数的类型
 *
 * @see CwtDataType
 * @see CwtDataTypes
 */
@Suppress("unused")
object CwtDataTypeSets {
    /** 整数相关类型：[Int][CwtDataTypes.Int]、[IntValueField][CwtDataTypes.IntValueField]、[IntVariableField][CwtDataTypes.IntVariableField]。 */
    val Int = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
    )
    /** 浮点数相关类型：[Float][CwtDataTypes.Float]、[ValueField][CwtDataTypes.ValueField]、[VariableField][CwtDataTypes.VariableField]。 */
    val Float = arrayOf(
        CwtDataTypes.Float,
        CwtDataTypes.ValueField,
        CwtDataTypes.VariableField,
    )
    /** 路径引用类型：[AbsoluteFilePath][CwtDataTypes.AbsoluteFilePath]、[FileName][CwtDataTypes.FileName]、[FilePath][CwtDataTypes.FilePath]、[Icon][CwtDataTypes.Icon]。 */
    val PathReference = arrayOf(
        CwtDataTypes.AbsoluteFilePath,
        CwtDataTypes.FileName,
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
    )
    /** 本地化引用类型：[Localisation][CwtDataTypes.Localisation]、[SyncedLocalisation][CwtDataTypes.SyncedLocalisation]、[InlineLocalisation][CwtDataTypes.InlineLocalisation]。 */
    val LocalisationReference = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )

    /** 动态值相关类型：[Value][CwtDataTypes.Value]、[ValueSet][CwtDataTypes.ValueSet]、[DynamicValue][CwtDataTypes.DynamicValue]。 */
    val DynamicValue = arrayOf(
        CwtDataTypes.Value,
        CwtDataTypes.ValueSet,
        CwtDataTypes.DynamicValue,
    )
    /** 作用域字段相关类型：[ScopeField][CwtDataTypes.ScopeField]、[Scope][CwtDataTypes.Scope]、[ScopeGroup][CwtDataTypes.ScopeGroup]。 */
    val ScopeField = arrayOf(
        CwtDataTypes.ScopeField,
        CwtDataTypes.Scope,
        CwtDataTypes.ScopeGroup,
    )
    /** 值字段表达式类型：[IntValueField][CwtDataTypes.IntValueField]、[ValueField][CwtDataTypes.ValueField]。 */
    val ValueField = arrayOf(
        CwtDataTypes.IntValueField,
        CwtDataTypes.ValueField,
    )
    /** 变量字段表达式类型：[IntVariableField][CwtDataTypes.IntVariableField]、[VariableField][CwtDataTypes.VariableField]。 */
    val VariableField = arrayOf(
        CwtDataTypes.IntVariableField,
        CwtDataTypes.VariableField,
    )

    /** 可包含常量文本的类型：[Constant][CwtDataTypes.Constant]、[TemplateExpression][CwtDataTypes.TemplateExpression]。 */
    val ConstantAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    /** 可解析为定义引用的类型：[Definition][CwtDataTypes.Definition]、[TechnologyWithLevel][CwtDataTypes.TechnologyWithLevel]。 */
    val DefinitionAware = arrayOf(
        CwtDataTypes.Definition,
        CwtDataTypes.TechnologyWithLevel,
    )
    /** 可解析为本地化引用的类型：[Localisation][CwtDataTypes.Localisation]、[InlineLocalisation][CwtDataTypes.InlineLocalisation]。 */
    val LocalisationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 可定位图像资源的类型：[FilePath][CwtDataTypes.FilePath]、[Icon][CwtDataTypes.Icon]、[Definition][CwtDataTypes.Definition]。 */
    val ImageLocationAware = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition,
    )
    /** 可定位本地化资源的类型：[Localisation][CwtDataTypes.Localisation]、[SyncedLocalisation][CwtDataTypes.SyncedLocalisation]、[InlineLocalisation][CwtDataTypes.InlineLocalisation]。 */
    val LocalisationLocationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 别名名称相关类型：[AliasName][CwtDataTypes.AliasName]、[AliasKeysField][CwtDataTypes.AliasKeysField]。 */
    val AliasNameAware = arrayOf(
        CwtDataTypes.AliasName,
        CwtDataTypes.AliasKeysField,
    )
    /** 所有模式感知类型：[Constant][CwtDataTypes.Constant]、[TemplateExpression][CwtDataTypes.TemplateExpression]、[Ant][CwtDataTypes.Ant]、[Regex][CwtDataTypes.Regex]。 */
    val PatternAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.Ant,
        CwtDataTypes.Regex,
    )
    /** 所有后缀感知类型：[SuffixAwareDefinition][CwtDataTypes.SuffixAwareDefinition]、[SuffixAwareLocalisation][CwtDataTypes.SuffixAwareLocalisation]、[SuffixAwareSyncedLocalisation][CwtDataTypes.SuffixAwareSyncedLocalisation]。 */
    val SuffixAware = arrayOf(
        CwtDataTypes.SuffixAwareDefinition,
        CwtDataTypes.SuffixAwareLocalisation,
        CwtDataTypes.SuffixAwareSyncedLocalisation,
    )

    /** 涉及动态值的所有类型（[DynamicValue] + [ScopeField] + [ValueField] + [VariableField]的并集）。 */
    val DynamicValueInvolved = arrayOf(
        *DynamicValue,
        *ScopeField,
        *ValueField,
        *VariableField,
    )
    /** 涉及参数的类型：[Parameter][CwtDataTypes.Parameter]。 */
    val ParameterInvolved = arrayOf(
        CwtDataTypes.Parameter
    )
    /** 涉及本地化参数的类型：[LocalisationParameter][CwtDataTypes.LocalisationParameter]。 */
    val LocalisationParameterInvolved = arrayOf(
        CwtDataTypes.LocalisationParameter
    )
}

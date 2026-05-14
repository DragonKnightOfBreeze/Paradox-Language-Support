package icu.windea.pls.config

/**
 * 数据类型的预定义分组。
 *
 * 将语义相关的 [CwtDataType] 实例组织为数组，用于匹配逻辑中的批量判断和功能分派。
 *
 * @see CwtDataType
 * @see CwtDataTypes
 */
@Suppress("unused")
object CwtDataTypeSets {
    /** 整数相关类型。 */
    val Int = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
    )
    /** 浮点数相关类型。 */
    val Float = arrayOf(
        CwtDataTypes.Float,
        CwtDataTypes.ValueField,
        CwtDataTypes.VariableField,
    )
    /** 本地化引用类型。 */
    val LocalisationReference = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 路径引用类型]。 */
    val PathReference = arrayOf(
        CwtDataTypes.AbsoluteFilePath,
        CwtDataTypes.FileName,
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
    )

    /** 动态值相关类型。 */
    val DynamicValue = arrayOf(
        CwtDataTypes.Value,
        CwtDataTypes.ValueSet,
        CwtDataTypes.DynamicValue,
    )
    /** 作用域字段相关类型。 */
    val ScopeField = arrayOf(
        CwtDataTypes.ScopeField,
        CwtDataTypes.Scope,
        CwtDataTypes.ScopeGroup,
    )
    /** 值字段表达式类型。 */
    val ValueField = arrayOf(
        CwtDataTypes.IntValueField,
        CwtDataTypes.ValueField,
    )
    /** 变量字段表达式类型。 */
    val VariableField = arrayOf(
        CwtDataTypes.IntVariableField,
        CwtDataTypes.VariableField,
    )

    /** 可包含常量文本的类型。 */
    val ConstantAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    /** 可解析为定义引用的类型。 */
    val DefinitionAware = arrayOf(
        CwtDataTypes.Definition,
        CwtDataTypes.TechnologyWithLevel,
    )
    /** 可解析为本地化引用的类型。 */
    val LocalisationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 可定位图像资源的类型。 */
    val ImageLocationAware = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition,
    )
    /** 可定位本地化资源的类型。 */
    val LocalisationLocationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 别名名称相关类型。 */
    val AliasNameAware = arrayOf(
        CwtDataTypes.AliasKeysField,
        CwtDataTypes.AliasName,
    )
    /** 所有模式感知类型。 */
    val PatternAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.Ant,
        CwtDataTypes.Regex,
    )
    /** 所有后缀感知类型。 */
    val SuffixAware = arrayOf(
        CwtDataTypes.SuffixAwareDefinition,
        CwtDataTypes.SuffixAwareLocalisation,
        CwtDataTypes.SuffixAwareSyncedLocalisation,
    )

    /** 所有涉及动态值的类型。 */
    val DynamicValueInvolved = arrayOf(
        *DynamicValue,
        *ScopeField,
        *ValueField,
        *VariableField,
    )
    /** 所有涉及参数的类型。 */
    val ParameterInvolved = arrayOf(
        CwtDataTypes.Parameter
    )
    /** 所有涉及本地化参数的类型。 */
    val LocalisationParameterInvolved = arrayOf(
        CwtDataTypes.LocalisationParameter
    )
    /** 所有涉及（目前作为动态引用处理的）外部引用的类型。 */
    val ExternalReferenceInvolved = arrayOf(
        CwtDataTypes.ShaderEffect,
        CwtDataTypes.MeshLocator,
    )
}

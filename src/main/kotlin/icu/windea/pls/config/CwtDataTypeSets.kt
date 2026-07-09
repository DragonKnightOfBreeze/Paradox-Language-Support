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
    /** 整数字段相关的数据类型。 */
    val IntField = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
    )
    /** 浮点数字段相关的数据类型。 */
    val FloatField = arrayOf(
        CwtDataTypes.Float,
        CwtDataTypes.ValueField,
        CwtDataTypes.VariableField,
    )
    /** 动态值相关的数据类型。 */
    val DynamicValue = arrayOf(
        CwtDataTypes.Value,
        CwtDataTypes.ValueSet,
        CwtDataTypes.DynamicValue,
    )
    /** 作用域字段相关的数据类型。 */
    val ScopeField = arrayOf(
        CwtDataTypes.ScopeField,
        CwtDataTypes.Scope,
        CwtDataTypes.ScopeGroup,
    )
    /** 值字段表达式的数据类型。 */
    val ValueField = arrayOf(
        CwtDataTypes.IntValueField,
        CwtDataTypes.ValueField,
    )
    /** 变量字段表达式的数据类型。 */
    val VariableField = arrayOf(
        CwtDataTypes.IntVariableField,
        CwtDataTypes.VariableField,
    )

    /** 本地化引用的数据类型。 */
    val LocalisationReference = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 路径引用的数据类型。 */
    val PathReference = arrayOf(
        CwtDataTypes.FileName,
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.AbsoluteFilePath,
    )

    /** 可包含常量文本的数据类型。 */
    val ConstantAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    /** 可解析为定义的数据类型。 */
    val DefinitionAware = arrayOf(
        CwtDataTypes.Definition,
        CwtDataTypes.TechnologyWithLevel,
    )
    /** 可解析为普通本地化的数据类型。 */
    val NormalLocalisationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 可解析为同步本地化的数据类型。 */
    val SyncedLocalisationAware = arrayOf(
        CwtDataTypes.SyncedLocalisation,
    )
    /** 可定位图像资源的数据类型。 */
    val ImageLocationAware = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition,
    )
    /** 可定位本地化资源的数据类型。 */
    val LocalisationLocationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    /** 所有模式感知的数据类型。 */
    val PatternAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.Glob,
        CwtDataTypes.Ant,
        CwtDataTypes.Regex,
    )
    /** 所有后缀感知的数据类型。 */
    val SuffixAware = arrayOf(
        CwtDataTypes.SuffixAwareDefinition,
        CwtDataTypes.SuffixAwareLocalisation,
        CwtDataTypes.SuffixAwareSyncedLocalisation,
    )

    /** 可以展开为一组候选项的数据类型。 */
    val Expandable = arrayOf(
        CwtDataTypes.UnionValue,
        CwtDataTypes.AliasKeysField,
    )

    /** 所有可评估脚本值引用的数据类型。 */
    val ScriptValueReferenceEvaluatable = arrayOf(
        CwtDataTypes.ScriptValueReference,
        *ValueField,
    )
    /** 所有可评估定值引用的数据类型。 */
    val DefineReferenceEvaluatable = arrayOf(
        CwtDataTypes.DefineReference,
        *ValueField,
        CwtDataTypes.Command,
    )
    /** 所有可评估数组定值引用的数据类型。 */
    val ArrayDefineReferenceEvaluatable = arrayOf(
        CwtDataTypes.ArrayDefineReference,
        *ValueField,
        CwtDataTypes.Command,
    )
    /** 所有可评估的复杂表达式的数据类型。 */
    val Evaluatable = arrayOf(
        CwtDataTypes.ScriptValueReference,
        CwtDataTypes.DefineReference,
        CwtDataTypes.ArrayDefineReference,
        *ValueField,
        CwtDataTypes.Command,
    )

    /** 所有涉及动态值的数据类型。 */
    val DynamicValueInvolved = arrayOf(
        *DynamicValue,
        *ScopeField,
        *ValueField,
        *VariableField,
        CwtDataTypes.Command,
        CwtDataTypes.Tags
    )
    /** 所有涉及参数的数据类型。 */
    val ParameterInvolved = arrayOf(
        CwtDataTypes.Parameter
    )
    /** 所有涉及本地化参数的数据类型。 */
    val LocalisationParameterInvolved = arrayOf(
        CwtDataTypes.LocalisationParameter
    )
    /** 所有涉及（目前作为动态引用处理的）外部引用的数据类型。 */
    val ExternalReferenceInvolved = arrayOf(
        CwtDataTypes.ShaderEffect,
        CwtDataTypes.MeshLocator,
    )
}

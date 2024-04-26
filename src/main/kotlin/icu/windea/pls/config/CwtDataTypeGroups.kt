package icu.windea.pls.config

object CwtDataTypeGroups {
    val Int = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
    )
    val Float = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.Float,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
        CwtDataTypes.ValueField,
        CwtDataTypes.VariableField,
    )
    val PathReference = arrayOf(
        CwtDataTypes.AbsoluteFilePath,
        CwtDataTypes.FileName,
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
    )
    val ScopeField = arrayOf(
        CwtDataTypes.ScopeField,
        CwtDataTypes.Scope,
        CwtDataTypes.ScopeGroup,
    )
    val ValueField = arrayOf(
        CwtDataTypes.IntValueField,
        CwtDataTypes.ValueField,
    )
    val VariableField = arrayOf(
        CwtDataTypes.IntVariableField,
        CwtDataTypes.VariableField,
    )
    val DynamicValue = arrayOf(
        CwtDataTypes.Value,
        CwtDataTypes.ValueSet,
        CwtDataTypes.DynamicValue,
    )
    val ConstantLike = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    val KeyReference = arrayOf(
        CwtDataTypes.Bool,
        CwtDataTypes.Int,
        CwtDataTypes.Float,
        CwtDataTypes.Scalar,
        CwtDataTypes.Constant,
        CwtDataTypes.Any,
    )
    
    val ImageLocationResolved = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition
    )
    val LocalisationLocationResolved = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation
    )
    
    val PatternLike = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.AntExpression,
        CwtDataTypes.Regex
    )
}
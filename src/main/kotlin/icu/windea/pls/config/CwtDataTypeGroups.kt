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
    val TextReference = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )

    val DynamicValue = arrayOf(
        CwtDataTypes.Value,
        CwtDataTypes.ValueSet,
        CwtDataTypes.DynamicValue,
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

    val ConstantLike = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    // val AliasNameLike = arrayOf(
    //     CwtDataTypes.AliasName,
    //     CwtDataTypes.AliasKeysField,
    // )
    // val KeyReference = arrayOf(
    //     CwtDataTypes.Bool,
    //     CwtDataTypes.Int,
    //     CwtDataTypes.Float,
    //     CwtDataTypes.Scalar,
    //     CwtDataTypes.Constant,
    //     CwtDataTypes.Any,
    // )

    val ImageLocationResolved = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition,
    )
    val LocalisationLocationResolved = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )

    val DefinitionAware = arrayOf(
        CwtDataTypes.Definition,
        CwtDataTypes.TechnologyWithLevel,
    )
    val LocalisationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
    )
    val PatternAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.AntExpression,
        CwtDataTypes.Regex,
    )
    @Suppress("unused")
    val SuffixAware = arrayOf(
        CwtDataTypes.SuffixAwareDefinition,
        CwtDataTypes.SuffixAwareLocalisation,
        CwtDataTypes.SuffixAwareSyncedLocalisation,
    )
}

package icu.windea.pls.config

@Suppress("unused")
object CwtDataTypeSets {
    val Int = arrayOf(
        CwtDataTypes.Int,
        CwtDataTypes.IntValueField,
        CwtDataTypes.IntVariableField,
    )
    val Float = arrayOf(
        CwtDataTypes.Float,
        CwtDataTypes.ValueField,
        CwtDataTypes.VariableField,
    )
    val PathReference = arrayOf(
        CwtDataTypes.AbsoluteFilePath,
        CwtDataTypes.FileName,
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
    )
    val LocalisationReference = arrayOf(
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

    val ConstantAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
    )
    val DefinitionAware = arrayOf(
        CwtDataTypes.Definition,
        CwtDataTypes.TechnologyWithLevel,
    )
    val LocalisationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
    )
    val ImageLocationAware = arrayOf(
        CwtDataTypes.FilePath,
        CwtDataTypes.Icon,
        CwtDataTypes.Definition,
    )
    val LocalisationLocationAware = arrayOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.InlineLocalisation,
    )
    val AliasNameAware = arrayOf(
        CwtDataTypes.AliasName,
        CwtDataTypes.AliasKeysField,
    )
    val PatternAware = arrayOf(
        CwtDataTypes.Constant,
        CwtDataTypes.TemplateExpression,
        CwtDataTypes.Ant,
        CwtDataTypes.Regex,
    )
    val SuffixAware = arrayOf(
        CwtDataTypes.SuffixAwareDefinition,
        CwtDataTypes.SuffixAwareLocalisation,
        CwtDataTypes.SuffixAwareSyncedLocalisation,
    )
}

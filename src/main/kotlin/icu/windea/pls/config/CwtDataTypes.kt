package icu.windea.pls.config

import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.model.ParadoxGameType

object CwtDataTypes {
    val Block = CwtDataType("Block")
    val Bool = CwtDataType("Bool")
    val Int = CwtDataType("Int")
    val Float = CwtDataType("Float")
    val Scalar = CwtDataType("Scalar")
    val ColorField = CwtDataType("ColorField")

    val PercentageField = CwtDataType("PercentageField")
    val DateField = CwtDataType("DateField")
    val Definition = CwtDataType("Definition", isReference = true)
    val Localisation = CwtDataType("Localisation", isReference = true)
    val SyncedLocalisation = CwtDataType("SyncedLocalisation", isReference = true)
    val InlineLocalisation = CwtDataType("InlineLocalisation", isReference = true)
    val AbsoluteFilePath = CwtDataType("AbsoluteFilePath", isReference = true)
    val Icon = CwtDataType("Icon", isReference = true)
    val FilePath = CwtDataType("FilePath", isReference = true)
    val FileName = CwtDataType("FileName", isReference = true)
    val EnumValue = CwtDataType("EnumValue", isReference = true)
    val Value = CwtDataType("Value", isReference = true)
    val ValueSet = CwtDataType("ValueSet", isReference = true)
    val DynamicValue = CwtDataType("DynamicValue", isReference = true)
    val ScopeField = CwtDataType("ScopeField", isReference = true)
    val Scope = CwtDataType("Scope", isReference = true)
    val ScopeGroup = CwtDataType("ScopeGroup", isReference = true)
    val ValueField = CwtDataType("ValueField", isReference = true)
    val IntValueField = CwtDataType("IntValueField", isReference = true)
    val VariableField = CwtDataType("VariableField", isReference = true)
    val IntVariableField = CwtDataType("IntVariableField", isReference = true)
    val Modifier = CwtDataType("Modifier", isReference = true)
    val SingleAliasRight = CwtDataType("SingleAliasRight", isReference = true)
    val AliasName = CwtDataType("AliasName", isReference = true)
    val AliasKeysField = CwtDataType("AliasKeysField", isReference = true)
    val AliasMatchLeft = CwtDataType("AliasMatchLeft", isReference = true)

    val Any = CwtDataType("Any")
    val Parameter = CwtDataType("Parameter", isReference = true)
    val ParameterValue = CwtDataType("ParameterValue", isReference = true)
    val LocalisationParameter = CwtDataType("LocalisationParameter", isReference = true)
    // effects in .shader files
    val ShaderEffect = CwtDataType("ShaderEffect"/*, isReference = true*/)

    // e.g., civic:xxx:xxx
    /** @since 1.3.9 */
    // @WithGameType(ParadoxGameType.Stellaris) // not limited yet
    val DatabaseObject = CwtDataType("DatabaseObject", isReference = true)
    // e.g., define:NPortrait|GRACEFUL_AGING_START
    /** @since 1.3.25 */
    // @WithGameType(ParadoxGameType.Vic3) // not limited yet
    val DefineReference = CwtDataType("DefineReference", isReference = true)

    @WithGameType(ParadoxGameType.Stellaris)
    val StellarisNameFormat = CwtDataType("StellarisNameFormat"/*, isReference = true*/)
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType("TechnologyWithLevel", isReference = true)

    // Pattern Aware Data Types

    val Constant = CwtDataType("Constant", isPatternAware = true)
    // e.g., a_<b>_enum[c]_value[d]
    val TemplateExpression = CwtDataType("TemplateExpression", isPatternAware = true)
    // e.g., /foo/bar?/*
    /** @since 1.3.6 */
    val AntExpression = CwtDataType("AntExpression", isPatternAware = true)
    // e.g., foo.*bar
    /** @since 1.3.6 */
    val Regex = CwtDataType("Regex", isPatternAware = true)

    // Suffix Aware Data Types

    // NOTE SUFFIX_AWARE 目前不兼容/不支持：代码补全、使用查询

    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareDefinition = CwtDataType("SuffixAwareDefinition", isSuffixAware = true)
    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareLocalisation = CwtDataType("SuffixAwareLocalisation", isSuffixAware = true)
    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareSyncedLocalisation = CwtDataType("SuffixAwareLocalisationSynced", isSuffixAware = true)
}

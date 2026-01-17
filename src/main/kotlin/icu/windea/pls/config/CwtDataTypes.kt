package icu.windea.pls.config

import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.model.ParadoxGameType

@Suppress("unused")
object CwtDataTypes {
    val Any = CwtDataType.builder("Any").build()
    val Bool = CwtDataType.builder("Bool").build()
    val Int = CwtDataType.builder("Int").build()
    val Float = CwtDataType.builder("Float").build()
    val Scalar = CwtDataType.builder("Scalar").build()
    val ColorField = CwtDataType.builder("ColorField").build()
    val Block = CwtDataType.builder("Block").build()

    val PercentageField = CwtDataType.builder("PercentageField").build()
    val DateField = CwtDataType.builder("DateField").build()
    val Definition = CwtDataType.builder("Definition").reference().build()
    val Localisation = CwtDataType.builder("Localisation").reference().build()
    val SyncedLocalisation = CwtDataType.builder("SyncedLocalisation").reference().build()
    val InlineLocalisation = CwtDataType.builder("InlineLocalisation").reference().build()
    val AbsoluteFilePath = CwtDataType.builder("AbsoluteFilePath").reference().build()
    val Icon = CwtDataType.builder("Icon").reference().build()
    val FilePath = CwtDataType.builder("FilePath").reference().build()
    val FileName = CwtDataType.builder("FileName").reference().build()
    val EnumValue = CwtDataType.builder("EnumValue").reference().build()
    val Value = CwtDataType.builder("Value").reference().build()
    val ValueSet = CwtDataType.builder("ValueSet").reference().build()
    val DynamicValue = CwtDataType.builder("DynamicValue").reference().build()
    val ScopeField = CwtDataType.builder("ScopeField").reference().build()
    val Scope = CwtDataType.builder("Scope").reference().build()
    val ScopeGroup = CwtDataType.builder("ScopeGroup").reference().build()
    val ValueField = CwtDataType.builder("ValueField").reference().build()
    val IntValueField = CwtDataType.builder("IntValueField").reference().build()
    val VariableField = CwtDataType.builder("VariableField").reference().build()
    val IntVariableField = CwtDataType.builder("IntVariableField").reference().build()
    val Modifier = CwtDataType.builder("Modifier").reference().build()
    val SingleAliasRight = CwtDataType.builder("SingleAliasRight").reference().build()
    val AliasName = CwtDataType.builder("AliasName").reference().build()
    val AliasKeysField = CwtDataType.builder("AliasKeysField").reference().build()
    val AliasMatchLeft = CwtDataType.builder("AliasMatchLeft").reference().build()

    val Parameter = CwtDataType.builder("Parameter").reference().build()
    val ParameterValue = CwtDataType.builder("ParameterValue").reference().build()
    val LocalisationParameter = CwtDataType.builder("LocalisationParameter").reference().build()

    // e.g., civic:xxx:xxx
    /** @since 1.3.9 */
    // @WithGameType(ParadoxGameType.Stellaris) // not limited yet
    val DatabaseObject = CwtDataType.builder("DatabaseObject").reference().build()
    // e.g., define:NPortrait|GRACEFUL_AGING_START
    /** @since 1.3.25 */
    // @WithGameType(ParadoxGameType.Vic3) // not limited yet
    val DefineReference = CwtDataType.builder("DefineReference").reference().build()
    @WithGameType(ParadoxGameType.Stellaris)
    val StellarisNameFormat = CwtDataType.builder("StellarisNameFormat").reference().build()

    // effects in .shader files
    val ShaderEffect = CwtDataType.builder("ShaderEffect"/*).reference(*/).build()
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType.builder("TechnologyWithLevel").reference().build()

    // Pattern Aware Data Types

    val Constant = CwtDataType.builder("Constant").patternAware().build()
    // e.g., a_<b>_enum[c]_value[d]
    val TemplateExpression = CwtDataType.builder("TemplateExpression").patternAware().build()
    // e.g., /foo/bar?/*
    /** @since 1.3.6 */
    val Ant = CwtDataType.builder("Ant").patternAware().build()
    // e.g., foo.*bar
    /** @since 1.3.6 */
    val Regex = CwtDataType.builder("Regex").patternAware().build()

    // Suffix Aware Data Types

    // TODO SUFFIX_AWARE 目前不兼容/不支持：代码补全、使用查询

    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareDefinition = CwtDataType.builder("SuffixAwareDefinition").suffixAware().build()
    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareLocalisation = CwtDataType.builder("SuffixAwareLocalisation").suffixAware().build()
    // #162, #193
    /** @since 2.0.5 */
    val SuffixAwareSyncedLocalisation = CwtDataType.builder("SuffixAwareLocalisationSynced").suffixAware().build()
}

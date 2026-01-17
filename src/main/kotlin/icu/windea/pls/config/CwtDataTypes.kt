package icu.windea.pls.config

import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.model.ParadoxGameType

@Suppress("unused")
object CwtDataTypes {
    val Any = CwtDataType.builder("Any")
        .withPriority(1.0) // very low
        .build()
    val Bool = CwtDataType.builder("Bool")
        .withPriority(100.0) // highest
        .build()
    val Int = CwtDataType.builder("Int")
        .withPriority(90.0) // very high
        .build()
    val Float = CwtDataType.builder("Float")
        .withPriority(90.0) // very high
        .build()
    val Scalar = CwtDataType.builder("Scalar")
        .withPriority(2.0) // very low
        .build()
    val ColorField = CwtDataType.builder("ColorField")
        .withPriority(90.0) // very high
        .build()
    val Block = CwtDataType.builder("Block")
        .withPriority(100.0) // highest
        .build()

    val PercentageField = CwtDataType.builder("PercentageField")
        .withPriority(90.0)
        .build()
    val DateField = CwtDataType.builder("DateField")
        .withPriority(90.0)
        .build()

    val Definition = CwtDataType.builder("Definition").reference()
        .withPriority(70.0)
        .build()
    val Localisation = CwtDataType.builder("Localisation").reference()
        .withPriority(60.0)
        .build()
    val SyncedLocalisation = CwtDataType.builder("SyncedLocalisation").reference()
        .withPriority(60.0)
        .build()
    val InlineLocalisation = CwtDataType.builder("InlineLocalisation").reference()
        .withPriority(60.0)
        .build()
    val Modifier = CwtDataType.builder("Modifier").reference()
        .withPriority(75.0) // higher than Definition
        .build()

    val AbsoluteFilePath = CwtDataType.builder("AbsoluteFilePath").reference()
        .withPriority(70.0)
        .build()
    val Icon = CwtDataType.builder("Icon").reference()
        .withPriority(70.0)
        .build()
    val FilePath = CwtDataType.builder("FilePath").reference()
        .withPriority(70.0)
        .build()
    val FileName = CwtDataType.builder("FileName").reference()
        .withPriority(70.0)
        .build()

    val EnumValue = CwtDataType.builder("EnumValue").reference()
        .withPriority { configExpression, configGroup ->
            val enumName = configExpression.value ?: return@withPriority 0.0 // unexpected
            if (configGroup.enums.containsKey(enumName)) return@withPriority 80.0
            if (configGroup.complexEnums.containsKey(enumName)) return@withPriority 45.0
            0.0 // unexpected
        }
        .build()
    val Value = CwtDataType.builder("Value").reference()
        .withPriority(40.0)
        .build()
    val ValueSet = CwtDataType.builder("ValueSet").reference()
        .withPriority(40.0)
        .build()
    val DynamicValue = CwtDataType.builder("DynamicValue").reference()
        .withPriority(40.0)
        .build()

    val ScopeField = CwtDataType.builder("ScopeField").reference()
        .withPriority(50.0)
        .build()
    val Scope = CwtDataType.builder("Scope").reference()
        .withPriority(50.0)
        .build()
    val ScopeGroup = CwtDataType.builder("ScopeGroup").reference()
        .withPriority(50.0)
        .build()
    val ValueField = CwtDataType.builder("ValueField").reference()
        .withPriority(45.0)
        .build()
    val IntValueField = CwtDataType.builder("IntValueField").reference()
        .withPriority(45.0)
        .build()
    val VariableField = CwtDataType.builder("VariableField").reference()
        .withPriority(45.0)
        .build()
    val IntVariableField = CwtDataType.builder("IntVariableField").reference()
        .withPriority(45.0)
        .build()

    val SingleAliasRight = CwtDataType.builder("SingleAliasRight").reference().build()
    val AliasName = CwtDataType.builder("AliasName").reference().build()
    val AliasKeysField = CwtDataType.builder("AliasKeysField").reference().build()
    val AliasMatchLeft = CwtDataType.builder("AliasMatchLeft").reference().build()

    val Parameter = CwtDataType.builder("Parameter").reference()
        .withPriority(10.0)
        .build()
    val ParameterValue = CwtDataType.builder("ParameterValue").reference()
        .withPriority(90.0) // same to Scalar
        .build()
    val LocalisationParameter = CwtDataType.builder("LocalisationParameter").reference()
        .withPriority(10.0)
        .build()

    // e.g., Root.GetName
    /** @since 2.1.1 */
    val Command = CwtDataType.builder("Command").reference()
        .withPriority(45.0)
        .build()
    // e.g., civic:xxx:xxx
    /** @since 1.3.9 */
    // @WithGameType(ParadoxGameType.Stellaris) // not limited yet
    val DatabaseObject = CwtDataType.builder("DatabaseObject").reference()
        .withPriority(60.0)
        .build()
    // e.g., define:NPortrait|GRACEFUL_AGING_START
    /** @since 1.3.25 */
    // @WithGameType(ParadoxGameType.Vic3) // not limited yet
    val DefineReference = CwtDataType.builder("DefineReference").reference()
        .withPriority(60.0)
        .build()
    @WithGameType(ParadoxGameType.Stellaris)
    val StellarisNameFormat = CwtDataType.builder("StellarisNameFormat").reference()
        .withPriority(60.0)
        .build()

    // effects in .shader files
    val ShaderEffect = CwtDataType.builder("ShaderEffect"/*).reference(*/)
        .withPriority(85.0)
        .build()
    // {technology}@{level}
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType.builder("TechnologyWithLevel").reference()
        .withPriority(69.0) // lower than Definition
        .build()

    // Pattern Aware Data Types

    val Constant = CwtDataType.builder("Constant").patternAware()
        .withPriority(100.0) // highest
        .build()
    // e.g., a_<b>_enum[c]_value[d]
    val TemplateExpression = CwtDataType.builder("TemplateExpression").patternAware()
        .withPriority(65.0)
        .build()

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

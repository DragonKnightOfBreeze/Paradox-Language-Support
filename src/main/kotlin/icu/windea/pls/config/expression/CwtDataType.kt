package icu.windea.pls.config.expression

import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*

data class CwtDataType(val name: String)

object CwtDataTypes

val CwtDataTypes.Block by lazy { CwtDataType("Block") }
val CwtDataTypes.Bool by lazy { CwtDataType("Bool") }
val CwtDataTypes.Int by lazy { CwtDataType("Int") }
val CwtDataTypes.Float by lazy { CwtDataType("Float") }
val CwtDataTypes.Scalar by lazy { CwtDataType("Scalar") }
val CwtDataTypes.ColorField by lazy { CwtDataType("ColorField") }
val CwtDataTypes.Other by lazy { CwtDataType("Other") }

val CwtDataTypes.PercentageField by lazy { CwtDataType("PercentageField") }
val CwtDataTypes.DateField by lazy { CwtDataType("DateField") }
val CwtDataTypes.Localisation by lazy { CwtDataType("Localisation") }
val CwtDataTypes.SyncedLocalisation by lazy { CwtDataType("SyncedLocalisation") }
val CwtDataTypes.InlineLocalisation by lazy { CwtDataType("InlineLocalisation") }
val CwtDataTypes.Definition by lazy { CwtDataType("Definition") }
val CwtDataTypes.AbsoluteFilePath by lazy { CwtDataType("AbsoluteFilePath") }
val CwtDataTypes.Icon by lazy { CwtDataType("Icon") }
val CwtDataTypes.FilePath by lazy { CwtDataType("FilePath") }
val CwtDataTypes.FileName by lazy { CwtDataType("FileName") }
val CwtDataTypes.EnumValue by lazy { CwtDataType("EnumValue") }
val CwtDataTypes.Value by lazy { CwtDataType("Value") }
val CwtDataTypes.ValueSet by lazy { CwtDataType("ValueSet") }
val CwtDataTypes.DynamicValue by lazy { CwtDataType("DynamicValue") }
val CwtDataTypes.ScopeField by lazy { CwtDataType("ScopeField") }
val CwtDataTypes.Scope by lazy { CwtDataType("Scope") }
val CwtDataTypes.ScopeGroup by lazy { CwtDataType("ScopeGroup") }
val CwtDataTypes.ValueField by lazy { CwtDataType("ValueField") }
val CwtDataTypes.IntValueField by lazy { CwtDataType("IntValueField") }
val CwtDataTypes.VariableField by lazy { CwtDataType("VariableField") }
val CwtDataTypes.IntVariableField by lazy { CwtDataType("IntVariableField") }
val CwtDataTypes.Modifier  by lazy { CwtDataType("Modifier") }
val CwtDataTypes.SingleAliasRight by lazy { CwtDataType("SingleAliasRight") }
val CwtDataTypes.AliasName by lazy { CwtDataType("AliasName") }
val CwtDataTypes.AliasKeysField by lazy { CwtDataType("AliasKeysField") }
val CwtDataTypes.AliasMatchLeft by lazy { CwtDataType("AliasMatchLeft") }
val CwtDataTypes.Template by lazy { CwtDataType("Template") }
val CwtDataTypes.Constant by lazy { CwtDataType("Constant") }

val CwtDataTypes.Any by lazy { CwtDataType("Any") }
val CwtDataTypes.Parameter by lazy { CwtDataType("Parameter") }
val CwtDataTypes.ParameterValue by lazy { CwtDataType("ParameterValue") }
val CwtDataTypes.LocalisationParameter by lazy { CwtDataType("LocalisationParameter") }
val CwtDataTypes.ShaderEffect by lazy { CwtDataType("ShaderEffect") } //对应`.shader`文件中的effect

@WithGameType(ParadoxGameType.Stellaris)
val CwtDataTypes.StellarisNameFormat by lazy { CwtDataType("StellarisNameFormat") }
@WithGameType(ParadoxGameType.Stellaris)
val CwtDataTypes.TechnologyWithLevel by lazy { CwtDataType("TechnologyWithLevel") }

fun CwtDataType.isIntType() =
    this == CwtDataTypes.Int || this == CwtDataTypes.IntValueField || this == CwtDataTypes.IntVariableField

fun CwtDataType.isFloatType() =
    this == CwtDataTypes.Int || this == CwtDataTypes.Float || this == CwtDataTypes.ValueField
        || this == CwtDataTypes.IntValueField || this == CwtDataTypes.VariableField || this == CwtDataTypes.IntVariableField

fun CwtDataType.isPathReferenceType() =
    this == CwtDataTypes.AbsoluteFilePath || this == CwtDataTypes.FileName || this == CwtDataTypes.FilePath
        || this == CwtDataTypes.Icon

fun CwtDataType.isScopeFieldType() =
    this == CwtDataTypes.ScopeField || this == CwtDataTypes.Scope || this == CwtDataTypes.ScopeGroup

fun CwtDataType.isValueFieldType() =
    this == CwtDataTypes.ValueField || this == CwtDataTypes.IntValueField

fun CwtDataType.isVariableFieldType() =
    this == CwtDataTypes.VariableField || this == CwtDataTypes.IntVariableField

fun CwtDataType.isDynamicValueType() =
    this == CwtDataTypes.Value || this == CwtDataTypes.ValueSet || this == CwtDataTypes.DynamicValue

fun CwtDataType.isConstantLikeType() =
    this == CwtDataTypes.Constant || this == CwtDataTypes.Template

fun CwtDataType.isKeyReferenceType() =
    this == CwtDataTypes.Bool || this == CwtDataTypes.Int || this == CwtDataTypes.Float
        || this == CwtDataTypes.Scalar || this == CwtDataTypes.Constant || this == CwtDataTypes.Any
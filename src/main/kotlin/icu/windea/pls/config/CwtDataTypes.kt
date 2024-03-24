package icu.windea.pls.config

import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*

object CwtDataTypes {
    val Block = CwtDataType("Block")
    val Bool = CwtDataType("Bool")
    val Int = CwtDataType("Int")
    val Float = CwtDataType("Float")
    val Scalar = CwtDataType("Scalar")
    val ColorField = CwtDataType("ColorField")
    val Other = CwtDataType("Other")
    
    val PercentageField = CwtDataType("PercentageField")
    val DateField = CwtDataType("DateField")
    val Localisation = CwtDataType("Localisation")
    val SyncedLocalisation = CwtDataType("SyncedLocalisation")
    val InlineLocalisation = CwtDataType("InlineLocalisation")
    val Definition = CwtDataType("Definition")
    val AbsoluteFilePath = CwtDataType("AbsoluteFilePath")
    val Icon = CwtDataType("Icon")
    val FilePath = CwtDataType("FilePath")
    val FileName = CwtDataType("FileName")
    val EnumValue = CwtDataType("EnumValue")
    val Value = CwtDataType("Value")
    val ValueSet = CwtDataType("ValueSet")
    val DynamicValue = CwtDataType("DynamicValue")
    val ScopeField = CwtDataType("ScopeField")
    val Scope = CwtDataType("Scope")
    val ScopeGroup = CwtDataType("ScopeGroup")
    val ValueField = CwtDataType("ValueField")
    val IntValueField = CwtDataType("IntValueField")
    val VariableField = CwtDataType("VariableField")
    val IntVariableField = CwtDataType("IntVariableField")
    val Modifier = CwtDataType("Modifier")
    val SingleAliasRight = CwtDataType("SingleAliasRight")
    val AliasName = CwtDataType("AliasName")
    val AliasKeysField = CwtDataType("AliasKeysField")
    val AliasMatchLeft = CwtDataType("AliasMatchLeft")
    val Template = CwtDataType("Template")
    val Constant = CwtDataType("Constant")
    
    val Any = CwtDataType("Any")
    val Parameter = CwtDataType("Parameter")
    val ParameterValue = CwtDataType("ParameterValue")
    val LocalisationParameter = CwtDataType("LocalisationParameter")
    val ShaderEffect = CwtDataType("ShaderEffect") //对应`.shader`文件中的effect
    
    @WithGameType(ParadoxGameType.Stellaris)
    val StellarisNameFormat = CwtDataType("StellarisNameFormat")
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType("TechnologyWithLevel")
}
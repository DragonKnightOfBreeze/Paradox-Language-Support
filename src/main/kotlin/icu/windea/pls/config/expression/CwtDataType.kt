package icu.windea.pls.config.expression

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

enum class CwtDataType {
    Block,
    Bool,
    Int,
    Float,
    Scalar,
    ColorField,
    PercentageField,
    DateField,
    Localisation,
    SyncedLocalisation,
    InlineLocalisation,
    Definition,
    //EXTENDED BY PLS
    AbsoluteFilePath,
    Icon,
    FilePath,
    //EXTENDED BY PLS
    FileName,
    EnumValue,
    Value,
    ValueSet,
    ScopeField,
    Scope,
    ScopeGroup,
    ValueField,
    IntValueField,
    VariableField,
    IntVariableField,
    Modifier, //<modifier>
    SingleAliasRight,
    AliasName,
    AliasKeysField,
    AliasMatchLeft,
    Template,
    Constant,
    Other,
    @WithGameType(ParadoxGameType.Stellaris)
    StellarisNameFormat,
    //EXTENDED BY PLS
	/** 对应`.shader`文件中的effect。 */
    ShaderEffect;
    
    //modify implementation of below methods should also check codes that directly based on enum constants
    //so they are just as a convenience
    
    fun isNumberType() = this == Int || this == Float
        || this == ValueField || this == IntValueField
        || this == VariableField || this == IntVariableField
    
    fun isPathReferenceType() = this == AbsoluteFilePath
        || this == FileName || this == FilePath || this == Icon
    
    fun isScopeFieldType() = this == ScopeField || this == Scope || this == ScopeGroup
    
    fun isValueFieldType() = this == ValueField || this == IntValueField
    
    fun isVariableFieldType() = this == VariableField || this == IntVariableField
    
    fun isValueSetValueType() = this == Value || this == ValueSet
    
    fun isConstantLikeType() = this == Constant || this == Template
}
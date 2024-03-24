package icu.windea.pls.ep.expression

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Result
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

class BaseCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Any? {
        return when {
            configExpression.type == CwtDataTypes.Block -> {
                if(expression.isKey != false) return Result.NotMatch
                if(expression.type != ParadoxType.Block) return Result.NotMatch
                if(config !is CwtMemberConfig) return Result.NotMatch
                Result.LazyBlockAwareMatch {
                    matchesScriptExpressionInBlock(element, config)
                }
            }
            configExpression.type == CwtDataTypes.Bool -> {
                val r = expression.type.isBooleanType()
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Int -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.text.toIntOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.Float -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.text.toFloatOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.Scalar -> {
                val r = when {
                    expression.isKey == true -> true //key -> ok
                    expression.type == ParadoxType.Parameter -> true //parameter -> ok
                    expression.type.isBooleanType() -> true //boolean -> sadly, also ok for compatibility
                    expression.type.isIntType() -> true //number -> ok according to vanilla game files
                    expression.type.isFloatType() -> true //number -> ok according to vanilla game files
                    expression.type.isStringType() -> true //unquoted/quoted string -> ok
                    else -> false
                }
                Result.ofFallback(r)
            }
            configExpression.type == CwtDataTypes.ColorField -> {
                val r = expression.type.isColorType() && configExpression.value?.let { expression.text.startsWith(it) } != false
                Result.of(r)
            }
            else -> null
        }
    }
    
    private fun matchesScriptExpressionInBlock(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        val block = when {
            element is ParadoxScriptProperty -> element.propertyValue()
            element is ParadoxScriptBlock -> element
            else -> null
        } ?: return false
        //简单判断：如果block中包含configsInBlock声明的必须的任意propertyKey（作为常量字符串，忽略大小写），则认为匹配
        //注意：不同的子句规则可以拥有部分相同的propertyKey
        val keys = CwtConfigHandler.getInBlockKeys(config)
        if(keys.isEmpty()) return true
        val actualKeys = mutableSetOf<String>()
        //注意这里需要考虑内联和可选的情况
        block.processData(conditional = true, inline = true) {
            if(it is ParadoxScriptProperty) actualKeys.add(it.name)
            true
        }
        return actualKeys.any { it in keys }
    }
}

class CoreCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Any? {
        val project = configGroup.project
        return when {
            configExpression.type == CwtDataTypes.PercentageField -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxType.isPercentageField(expression.text)
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.DateField -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxType.isDateField(expression.text)
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Localisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                if(!expression.text.isExactIdentifier('.', '-', '\'')) return Result.NotMatch
                CwtConfigMatcher.Impls.getLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.SyncedLocalisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                if(!expression.text.isExactIdentifier('.', '-', '\'')) return Result.NotMatch
                CwtConfigMatcher.Impls.getSyncedLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.InlineLocalisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.quoted) return Result.FallbackMatch //"quoted_string" -> any string
                if(expression.isParameterized()) return Result.ParameterizedMatch
                if(!expression.text.isExactIdentifier('.', '-', '\'')) return Result.NotMatch
                CwtConfigMatcher.Impls.getSyncedLocalisationMatchResult(element, expression, project)
            }
            configExpression.type == CwtDataTypes.Definition -> {
                //注意这里可能是一个整数，例如，对于<technology_tier>
                if(!expression.type.isStringType() && expression.type != ParadoxType.Int) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                if(!expression.text.isExactIdentifier('.', '-')) return Result.NotMatch
                CwtConfigMatcher.Impls.getDefinitionMatchResult(element, expression, configExpression, project)
            }
            configExpression.type == CwtDataTypes.AbsoluteFilePath -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                Result.ExactMatch //总是认为匹配
            }
            configExpression.type in CwtDataTypeGroups.PathReference -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                CwtConfigMatcher.Impls.getPathReferenceMatchResult(element, expression, configExpression, project)
            }
            configExpression.type == CwtDataTypes.EnumValue -> {
                if(expression.type.isBlockLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val name = expression.text
                val enumName = configExpression.value ?: return Result.NotMatch //invalid cwt config
                //匹配简单枚举
                val enumConfig = configGroup.enums[enumName]
                if(enumConfig != null) {
                    val r = name in enumConfig.values
                    return Result.of(r)
                }
                //匹配复杂枚举
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    //complexEnumValue的值必须合法
                    if(ParadoxComplexEnumValueHandler.getName(expression.text) == null) return Result.NotMatch
                    return CwtConfigMatcher.Impls.getComplexEnumValueMatchResult(element, name, enumName, complexEnumConfig, project)
                }
                Result.NotMatch
            }
            configExpression.type in CwtDataTypeGroups.DynamicValue -> {
                if(expression.type.isBlockLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //dynamicValue的值必须合法
                val name = ParadoxDynamicValueHandler.getName(expression.text)
                if(name == null) return Result.NotMatch
                val dynamicValueType = configExpression.value
                if(dynamicValueType == null) return Result.NotMatch
                CwtConfigMatcher.Impls.getDynamicValueMatchResult(element, name, dynamicValueType, project)
            }
            configExpression.type in CwtDataTypeGroups.ScopeField -> {
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                CwtConfigMatcher.Impls.getScopeFieldMatchResult(element, expression, configExpression, configGroup)
            }
            configExpression.type in CwtDataTypeGroups.ValueField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(configExpression.type == CwtDataTypes.ValueField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return Result.ExactMatch
                } else if(configExpression.type == CwtDataTypes.IntValueField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return Result.ExactMatch
                }
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup)
                val r = valueFieldExpression != null
                Result.of(r)
            }
            configExpression.type in CwtDataTypeGroups.VariableField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(configExpression.type == CwtDataTypes.VariableField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return Result.ExactMatch
                } else if(configExpression.type == CwtDataTypes.IntVariableField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return Result.ExactMatch
                }
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup)
                val r = variableFieldExpression != null
                Result.of(r)
            }
            configExpression.type == CwtDataTypes.Modifier -> {
                if(expression.isParameterized()) return Result.ParameterizedMatch
                CwtConfigMatcher.Impls.getModifierMatchResult(element, expression, configGroup)
            }
            configExpression.type == CwtDataTypes.SingleAliasRight -> {
                Result.NotMatch //不在这里处理
            }
            configExpression.type == CwtDataTypes.AliasKeysField -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = CwtConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                CwtConfigMatcher.matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            configExpression.type == CwtDataTypes.AliasName -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = CwtConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                CwtConfigMatcher.matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            configExpression.type == CwtDataTypes.AliasMatchLeft -> {
                return Result.NotMatch //不在这里处理
            }
            else -> null
        }
    }
}

class ExtendedCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Any? {
        return when {
            configExpression.type == CwtDataTypes.Any -> {
                Result.FallbackMatch
            }
            configExpression.type == CwtDataTypes.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.ParameterValue -> {
                if(expression.type != ParadoxType.Block) return Result.ExactMatch
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.LocalisationParameter -> {
                //匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.ShaderEffect -> {
                //TODO 1.2.2+ 暂时作为一般的字符串处理
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                Result.NotMatch
            }
            configExpression.type == CwtDataTypes.StellarisNameFormat -> {
                //TODO 1.2.2+ 需要考虑进一步的支持
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                Result.FallbackMatch
            }
            configExpression.type == CwtDataTypes.TechnologyWithLevel -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val r = expression.text.length > 1 && expression.text.indexOf('@') >= 1
                return Result.of(r)
            }
            else -> null
        }
    }
}

class ConstantCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Any? {
        return when {
            configExpression.type == CwtDataTypes.Constant -> {
                val value = configExpression.value
                if(configExpression is CwtValueExpression) {
                    //常量的值也可能是yes/no
                    val text = expression.text
                    if((value == "yes" || value == "no") && text.isLeftQuoted()) return Result.NotMatch
                }
                //这里也用来匹配空字符串
                val r = expression.text.equals(value, true) //忽略大小写
                Result.of(r)
            }
            else -> null
        }
    }
}

class TemplateCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, options: Int): Any? {
        return when {
            configExpression.type == CwtDataTypes.Template -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //允许用引号括起
                CwtConfigMatcher.Impls.getTemplateMatchResult(element, expression, configExpression, configGroup)
            }
            else -> null
        }
    }
}
package icu.windea.pls.ep.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configExpression.constrained
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.ignoreCase
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.text.TextPattern
import icu.windea.pls.core.util.text.TextPatternBasedBuilder
import icu.windea.pls.core.util.text.TextPatternBasedProvider
import icu.windea.pls.core.util.text.TextPatternMatchResult

abstract class CwtTextPatternBasedDataExpressionResolver : CwtDataExpressionResolver {
    protected data class Match(
        val type: CwtDataType,
        val action: CwtDataExpression.() -> Unit = {}
    )

    private val providers = mutableListOf<TextPatternBasedProvider<Match, out TextPatternMatchResult>>()
    private val builder by lazy { TextPatternBasedBuilder<Match>(providers) }

    protected fun fromLiteral(type: CwtDataType, value: String, action: CwtDataExpression.() -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(value)) { _, _ -> Match(type, action) }
    }

    protected fun fromParameterized(type: CwtDataType, prefix: String, suffix: String, action: CwtDataExpression.(String) -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, suffix)) { _, r -> Match(type) { action(r.value) } }
    }

    protected fun fromRanged(type: CwtDataType, prefix: String, action: CwtDataExpression.(String) -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, "")) { _, r -> if (isRangeLike(r.value)) Match(type) { action(r.value) } else null }
    }

    private fun isRangeLike(v: String): Boolean {
        return v.length >= 2 && v.first().let { c -> c == '[' || c == '(' } && v.last().let { c -> c == ']' || c == ')' }
    }

    final override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        val match = builder.build(expressionString) ?: return null
        return CwtDataExpression.create(expressionString, isKey, match.type).apply(match.action)
    }

    override fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean {
        return providers.process { provider -> consumer.process(provider.pattern) }
    }
}

class CwtBaseDataExpressionResolver : CwtTextPatternBasedDataExpressionResolver() {
    init {
        fromLiteral(CwtDataTypes.Any, "\$any")

        fromLiteral(CwtDataTypes.Bool, "bool")

        fromLiteral(CwtDataTypes.Int, "int")
        fromRanged(CwtDataTypes.Int, "int") { intRange = IntRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.Float, "float")
        fromRanged(CwtDataTypes.Float, "float") { floatRange = FloatRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.Scalar, "scalar")
        fromLiteral(CwtDataTypes.Scalar, "constrained_scalar") { constrained = true }

        fromLiteral(CwtDataTypes.ColorField, "colour_field")
        fromParameterized(CwtDataTypes.ColorField, "colour[", "]") { value = it.orNull() }
        fromLiteral(CwtDataTypes.ColorField, "color_field")
        fromParameterized(CwtDataTypes.ColorField, "color[", "]") { value = it.orNull() }
    }
}

class CwtCoreDataExpressionResolver : CwtTextPatternBasedDataExpressionResolver() {
    init {
        fromLiteral(CwtDataTypes.PercentageField, "percentage_field")
        fromLiteral(CwtDataTypes.DateField, "date_field")
        fromParameterized(CwtDataTypes.DateField, "date_field[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.Localisation, "localisation")
        fromLiteral(CwtDataTypes.SyncedLocalisation, "localisation_synced")
        fromLiteral(CwtDataTypes.InlineLocalisation, "localisation_inline")

        fromLiteral(CwtDataTypes.AbsoluteFilePath, "abs_filepath")
        fromLiteral(CwtDataTypes.FileName, "filename")
        fromParameterized(CwtDataTypes.FileName, "filename[", "]") { value = it.orNull() }
        fromLiteral(CwtDataTypes.FilePath, "filepath")
        fromLiteral(CwtDataTypes.FilePath, "filepath[./]") { value = "./" } // fixed (should keep `"./"`)
        fromParameterized(CwtDataTypes.FilePath, "filepath[", "]") { value = it.optimizedPath().orNull() }
        fromParameterized(CwtDataTypes.Icon, "icon[", "]") { value = it.optimizedPath().orNull() }

        fromLiteral(CwtDataTypes.Modifier, "<modifier>")
        fromLiteral(CwtDataTypes.TechnologyWithLevel, "<technology_with_level>")
        fromParameterized(CwtDataTypes.Definition, "<", ">") { value = it.orNull() }

        fromParameterized(CwtDataTypes.Value, "value[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.ValueSet, "value_set[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.DynamicValue, "dynamic_value[", "]") { value = it.orNull() }

        fromParameterized(CwtDataTypes.EnumValue, "enum[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.ScopeField, "scope_field")
        fromParameterized(CwtDataTypes.Scope, "scope[", "]") { value = it.orNull().takeIf { v -> v != "any" } }
        fromParameterized(CwtDataTypes.ScopeGroup, "scope_group[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.ValueField, "value_field")
        fromRanged(CwtDataTypes.ValueField, "value_field") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntValueField, "int_value_field")
        fromRanged(CwtDataTypes.IntValueField, "int_value_field") { intRange = IntRangeInfo.from(it) }

        fromLiteral(CwtDataTypes.VariableField, "variable_field")
        fromRanged(CwtDataTypes.VariableField, "variable_field") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.VariableField, "variable_field32")
        fromRanged(CwtDataTypes.VariableField, "variable_field32") { floatRange = FloatRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntVariableField, "int_variable_field")
        fromRanged(CwtDataTypes.IntVariableField, "int_variable_field") { intRange = IntRangeInfo.from(it) }
        fromLiteral(CwtDataTypes.IntVariableField, "int_variable_field_32")
        fromRanged(CwtDataTypes.IntVariableField, "int_variable_field_32") { intRange = IntRangeInfo.from(it) }

        fromParameterized(CwtDataTypes.SingleAliasRight, "single_alias_right[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasName, "alias_name[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasMatchLeft, "alias_match_left[", "]") { value = it.orNull() }
        fromParameterized(CwtDataTypes.AliasKeysField, "alias_keys_field[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.Command, "\$command")
        fromLiteral(CwtDataTypes.DatabaseObject, "\$database_object")
        fromLiteral(CwtDataTypes.DefineReference, "\$define_reference")
        fromParameterized(CwtDataTypes.StellarisNameFormat, "stellaris_name_format[", "]") { value = it.orNull() }

        fromLiteral(CwtDataTypes.Parameter, "\$parameter")
        fromLiteral(CwtDataTypes.ParameterValue, "\$parameter_value")
        fromLiteral(CwtDataTypes.LocalisationParameter, "\$localisation_parameter")

        fromLiteral(CwtDataTypes.ShaderEffect, "\$shader_effect")
    }
}

class CwtConstantDataExpressionResolver : CwtDataExpressionResolver {
    private val forceRegex = """\w*\[[\w:]*]""".toRegex() // `type[x]`, `alias[x:y]`, etc.
    private val excludeCharacters = ":.@[]<>".toCharArray() // `x_<y>_enum[z]`, etc.

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (expressionString.any { c -> c in excludeCharacters } && !forceRegex.matches(expressionString)) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Constant).apply { value = expressionString }
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtTemplateDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        if (CwtTemplateExpression.resolve(expressionString).expressionString.isEmpty()) return null
        return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.TemplateExpression).apply { value = expressionString }
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtAntDataExpressionResolver : CwtDataExpressionResolver {
    private val prefix = "ant:"
    private val prefixIgnoreCase = "ant.i:"
    private val dataType = CwtDataTypes.Ant

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        run {
            val v = expressionString.removePrefixOrNull(prefix) ?: return@run
            return CwtDataExpression.create(expressionString, isKey, dataType).apply { value = v.orNull() }
        }
        run {
            val v = expressionString.removePrefixOrNull(prefixIgnoreCase) ?: return@run
            return CwtDataExpression.create(expressionString, isKey, dataType).apply { value = v.orNull() }.apply { ignoreCase = true }
        }
        return null
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtRegexDataExpressionResolver : CwtDataExpressionResolver {
    private val prefix = "re:"
    private val prefixIgnoreCase = "re.i:"
    private val dataType = CwtDataTypes.Regex

    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        run {
            val v = expressionString.removePrefixOrNull(prefix) ?: return@run
            return CwtDataExpression.create(expressionString, isKey, dataType).apply { value = v.orNull() }
        }
        run {
            val v = expressionString.removePrefixOrNull(prefixIgnoreCase) ?: return@run
            return CwtDataExpression.create(expressionString, isKey, dataType).apply { value = v.orNull() }.apply { ignoreCase = true }
        }
        return null
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

class CwtSuffixAwareDataExpressionResolver : CwtDataExpressionResolver {
    override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        val separatorIndex = expressionString.indexOf('|')
        if (separatorIndex == -1) return null
        val text = expressionString.substring(0, separatorIndex)
        val suffixes = expressionString.substring(separatorIndex + 1).toCommaDelimitedStringSet()
        run {
            val t = text.removeSurroundingOrNull("<", ">") ?: return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Definition).apply { value = t.orNull() }
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareDefinition).apply { value = t.orNull() }.apply { this.suffixes = suffixes }
        }
        run {
            if (text != "localisation") return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.Localisation)
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareLocalisation).apply { this.suffixes = suffixes }
        }
        run {
            if (text != "localisation_synced") return@run
            if (suffixes.isEmpty()) return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SyncedLocalisation)
            return CwtDataExpression.create(expressionString, isKey, CwtDataTypes.SuffixAwareSyncedLocalisation).apply { this.suffixes = suffixes }
        }
        return null
    }

    override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null
    }
}

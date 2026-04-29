package icu.windea.pls.lang.resolve

import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxType
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object ParadoxTypeService {
    private val percentageFieldRegex = """[1-9]?[0-9]+%""".toRegex()
    private val colorFieldRegex = """(?:rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d. \t]*}""".toRegex()
    private val dateFieldFormatters = ConcurrentHashMap<String, DateTimeFormatter>()

    fun resolve(value: String): ParadoxType {
        return when {
            value.isEmpty() -> ParadoxType.String
            isBoolean(value) -> ParadoxType.Boolean
            isInt(value) -> ParadoxType.Int
            isFloat(value) -> ParadoxType.Float
            else -> ParadoxType.String
        }
    }

    fun isBoolean(value: String): Boolean {
        return value == "yes" || value == "no"
    }

    fun isInt(value: String): Boolean {
        // return expression.toIntOrNull() != null

        // use handwrite implementation to optimize performance and restrict validation
        // can be: 0, 1, 01, -1
        var isFirst = true
        var containsDigit = false
        value.forEach f@{ c ->
            if (isFirst) {
                isFirst = false
                if (c == '+' || c == '-') return@f
            }
            if (c.isExactDigit()) {
                if (!containsDigit) {
                    containsDigit = true
                }
                return@f
            }
            return false
        }
        return containsDigit
    }

    fun isFloat(value: String): Boolean {
        // return expression.toFloatOrNull() != null

        // use handwrite implementation to optimize performance and restrict validation
        // can be: 0, 1, 01, -1, 0.0, 1.0, 01.0, .0
        var isFirst = true
        var containsDot = false
        var containsDigit = false
        value.forEach f@{ c ->
            if (isFirst) {
                isFirst = false
                if (c == '+' || c == '-') return@f
            }
            if (c == '.') {
                if (containsDot) return false else containsDot = true
                return@f
            }
            if (c.isExactDigit()) {
                if (!containsDigit) {
                    containsDigit = true
                }
                return@f
            }
            return false
        }
        return containsDigit
    }

    fun isPercentageField(value: String): Boolean {
        return value.matches(percentageFieldRegex)
    }

    fun isColorField(value: String): Boolean {
        return value.matches(colorFieldRegex)
    }

    fun isDateField(expression: String, datePattern: String?): Boolean {
        return try {
            val pattern = datePattern?.orNull() ?: "y.M.d"
            val dateTimeFormatter = dateFieldFormatters.getOrPut(pattern) { DateTimeFormatter.ofPattern(pattern) }
            dateTimeFormatter.parse(expression)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isBoolean(type: ParadoxType): Boolean {
        return type == ParadoxType.Boolean
    }

    fun isRelaxInt(type: ParadoxType): Boolean {
        return type == ParadoxType.Unknown || type == ParadoxType.Int || type == ParadoxType.Parameter || type == ParadoxType.InlineMath
    }

    fun isRelaxFloat(type: ParadoxType): Boolean {
        return type == ParadoxType.Unknown || type == ParadoxType.Int || type == ParadoxType.Float || type == ParadoxType.Parameter || type == ParadoxType.InlineMath
    }

    fun isRelaxString(type: ParadoxType): Boolean {
        return type == ParadoxType.Unknown || type == ParadoxType.String || type == ParadoxType.Parameter
    }

    fun isNumberOrRelaxString(type: ParadoxType): Boolean {
        return type == ParadoxType.Unknown || type == ParadoxType.Int || type == ParadoxType.Float || type == ParadoxType.String || type == ParadoxType.Parameter
    }

    fun isStringLike(type: ParadoxType): Boolean {
        return type == ParadoxType.Unknown || type == ParadoxType.String || type == ParadoxType.Parameter || type == ParadoxType.Int || type == ParadoxType.Float
    }

    fun isBlockLike(type: ParadoxType): Boolean {
        return type == ParadoxType.Block || type == ParadoxType.Color || type == ParadoxType.InlineMath
    }

    fun isPossibleScriptedVariableValue(type: ParadoxType): Boolean {
        return type == ParadoxType.Boolean || type == ParadoxType.Int || type == ParadoxType.Float || type == ParadoxType.String
    }
}

@file:Suppress("unused")

package icu.windea.pls.lang.codeInsight

import fleet.multiplatform.shims.ConcurrentHashMap
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxType
import java.time.format.DateTimeFormatter

object ParadoxTypeResolver {
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
        //return expression.toIntOrNull() != null

        //use handwrite implementation to optimize memory and restrict validation
        //can be: 0, 1, 01, -1
        var isFirst = true
        var containsDigit = false
        value.forEach f@{ c ->
            if (isFirst) {
                isFirst = false
                if (c == '+' || c == '-') return@f
            }
            if (c.isExactDigit()) {
                if(!containsDigit) {
                    containsDigit = true
                }
                return@f
            }
            return false
        }
        return containsDigit
    }

    fun isFloat(value: String): Boolean {
        //return expression.toFloatOrNull() != null

        //use handwrite implementation to optimize memory and restrict validation
        //can be: 0, 1, 01, -1, 0.0, 1.0, 01.0, .0
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
                if(!containsDigit) {
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
        } catch (e: Exception) {
            false
        }
    }
}

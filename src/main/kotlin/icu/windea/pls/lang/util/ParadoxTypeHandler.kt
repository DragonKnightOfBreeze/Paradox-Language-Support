package icu.windea.pls.lang.util

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import java.time.format.*

object ParadoxTypeHandler {
    private val percentageFieldRegex = """[1-9]?[0-9]+%""".toRegex()
    private val colorFieldRegex = """(?:rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d. \t]*}""".toRegex()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    
    fun isBoolean(value: String): Boolean {
        return value == "yes" || value == "no"
    }
    
    fun isInt(value: String): Boolean {
        //return expression.toIntOrNull() != null
        
        //use handwrite implementation to optimize memory and restrict validation
        //can be: 0, 1, 01, -1
        var isFirst = true
        value.forEachFast f@{ c ->
            if(isFirst) {
                isFirst = false
                if(c == '+' || c == '-') return@f
            }
            if(c.isExactDigit()) return@f
            return false
        }
        return true
    }
    
    fun isFloat(value: String): Boolean {
        //return expression.toFloatOrNull() != null
        
        //use handwrite implementation to optimize memory and restrict validation
        //can be: 0, 1, 01, -1, 0.0, 1.0, 01.0, .0
        var isFirst = true
        var containsDot = false
        value.forEachFast f@{ c ->
            if(isFirst) {
                isFirst = false
                if(c == '+' || c == '-') return@f
            }
            if(c.isExactDigit()) return@f
            if(c == '.') {
                if(containsDot) return false else containsDot = true
                return@f
            }
            return false
        }
        return true
    }
    
    
    fun isPercentageField(value: String): Boolean {
        return value.matches(percentageFieldRegex)
    }
    
    
    fun isColorField(value: String): Boolean {
        return value.matches(colorFieldRegex)
    }
    
    fun isDateField(expression: String): Boolean {
        return try {
            dateFormatter.parse(expression)
            true
        } catch(e: Exception) {
            false
        }
    }
}

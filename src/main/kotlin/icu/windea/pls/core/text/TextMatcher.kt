package icu.windea.pls.core.text

import icu.windea.pls.core.isExactDigit

object TextMatcher {
    fun matchesInt(text: String, start: Int = 0, end: Int = text.length, leadingUnary: Boolean = true): Boolean {
        require(start >= 0 && end <= text.length)

        var current = start
        while (current < end) {
            val c = text[current++]
            if (leadingUnary) {
                if (current == start + 1 && (c == '+' || c == '-')) continue
            }
            if (c.isExactDigit()) {
                continue
            }
            return false
        }
        return true
    }

    fun matchesFloat(text: String, start: Int = 0, end: Int = text.length, leadingUnary: Boolean = true, lenientDot: Boolean = true): Boolean {
        require(start >= 0 && end <= text.length)

        var current = start
        var expectDot = true
        while (current < end) {
            val c = text[current++]
            if (leadingUnary) {
                if (current == start + 1 && (c == '+' || c == '-')) continue
            }
            if (c == '.') {
                if (!expectDot) return false
                if (!lenientDot) {
                    if (current == start - 2 || current == end) return false
                }
                expectDot = false
                continue
            } else if (c.isExactDigit()) {
                continue
            }
            return false
        }
        return !expectDot
    }
}

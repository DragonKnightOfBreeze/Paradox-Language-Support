package icu.windea.pls.core.match.date

import org.junit.Assert.*
import org.junit.Test

/**
 * @see DefaultDateMatcher
 */
class DateMatchersTest {
    @Test
    fun matchesDefaultPattern() {
        val matcher = DefaultDateMatcher("y.M.d")

        assertTrue(matcher.matches("2024.1.15"))
        assertTrue(matcher.matches("1000.12.31"))
        assertTrue(matcher.matches("1.1.1"))
        assertFalse(matcher.matches("2024-1-15"))
        assertFalse(matcher.matches("not.a.date"))
        assertFalse(matcher.matches(""))
    }

    @Test
    fun matchesKnownPatternsWithPrecheck() {
        assertTrue(DefaultDateMatcher("y.m").matches("2024.1"))
        assertFalse(DefaultDateMatcher("y.m").matches("2024.1.2"))

        assertTrue(DefaultDateMatcher("y.m.").matches("2024.1."))
        assertFalse(DefaultDateMatcher("y.m.").matches("2024.1"))

        assertTrue(DefaultDateMatcher("y.m.d.h").matches("2024.1.15.12"))
        assertTrue(DefaultDateMatcher("yyyy.MM.dd").matches("2024.01.15"))
    }

    @Test
    fun fallsBackForUnknownPattern() {
        val matcher = DefaultDateMatcher("yyyy-MM-dd")

        assertTrue(matcher.matches("2024-01-15"))
        assertFalse(matcher.matches("2024.01.15"))
    }
}

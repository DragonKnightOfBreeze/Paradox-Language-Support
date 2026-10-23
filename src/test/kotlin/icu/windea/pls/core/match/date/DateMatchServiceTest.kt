package icu.windea.pls.core.match.date

import org.junit.Assert.*
import org.junit.Test

/**
 * @see DateMatchService
 */
class DateMatchServiceTest {
    @Test
    fun matchesKnownPatterns() {
        assertTrue(DateMatchService.matches("2024.1.15", "y.M.d"))
        assertTrue(DateMatchService.matches("2024.1", "y.m"))
        assertTrue(DateMatchService.matches("2024.1.", "y.m."))
        assertTrue(DateMatchService.matches("2024.1.15.12", "y.m.d.h"))
        assertTrue(DateMatchService.matches("2024.01.15", "yyyy.MM.dd"))
    }

    @Test
    fun matchesCustomPattern() {
        assertTrue(DateMatchService.matches("2024-01-15", "yyyy-MM-dd"))
        assertTrue(DateMatchService.matches("2024", "yyyy"))
        assertFalse(DateMatchService.matches("abc", "yyyy"))
    }

    @Test
    fun returnsFalseForInvalidInput() {
        assertFalse(DateMatchService.matches("", "y.M.d"))
        assertFalse(DateMatchService.matches("2024-1-15", "y.M.d"))
    }

    @Test
    fun returnsFalseForInvalidPattern() {
        assertFalse(DateMatchService.matches("2024.1.15", "["))
    }

    @Test
    fun reusesCachedMatcher() {
        repeat(3) {
            assertTrue(DateMatchService.matches("2024.1.15", "y.M.d"))
            assertTrue(DateMatchService.matches("2024.1.15", "y.M.d"))
        }
    }
}

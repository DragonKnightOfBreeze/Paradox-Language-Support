package icu.windea.pls.core.match

import org.junit.Test
import kotlin.test.assertTrue

class MatchServiceTest {
    @Test
    fun anyMatch_forGlob() {
        assertTrue(MatchService.matchesPatterns("abc", "bc*;ab*"))
        assertTrue(MatchService.matchesPatterns("abc", "bc*;ab*;"))
        assertTrue(MatchService.matchesPatterns("abc", ";bc*;ab*"))
        assertTrue(MatchService.matchesPatterns("abc", ";;bc*;ab*;;"))
    }

    @Test
    fun anyMatch_forAnt() {
        assertTrue(MatchService.matchesAntPatterns("/foo/bar/name", "/bar/*;/foo/bar/*"))
        assertTrue(MatchService.matchesAntPatterns("/foo/bar/name", "/bar/*;/foo/bar/*;"))
        assertTrue(MatchService.matchesAntPatterns("/foo/bar/name", ";/bar/*;/foo/bar/*"))
        assertTrue(MatchService.matchesAntPatterns("/foo/bar/name", ";;/bar/*;/foo/bar/*;;"))
    }
}

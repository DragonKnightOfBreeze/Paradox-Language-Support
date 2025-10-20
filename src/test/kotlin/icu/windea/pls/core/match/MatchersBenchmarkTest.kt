package icu.windea.pls.core.match

import icu.windea.pls.test.AssumePredicates
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

class MatchersBenchmarkTest {
    @Before
    fun setup() = AssumePredicates.includeBenchmark()

    @Suppress("DEPRECATION")
    @Test
    fun antBenchmarkTest() {

        val patterns = listOf(
            "foo/bar/baz",
            "foo/*/baz",
            "foo/**/baz",
            "**/baz",
            "foo/bar?baz",
            "foo/*/bar/**/baz",
            "**",
            "foo/bar/*",
            "foo/bar/**",
            "foo/bar/ba*",
            "foo/bar/ba?",
        )
        val paths = listOf(
            "foo/bar/baz",
            "foo/abc/baz",
            "foo/bar/abc/baz",
            "foo/bar/baz/qux",
            "foo/barbaz",
            "foo/bar/xyz/baz",
            "foo/bar/abc/def/baz",
            "foo/bar/ba",
            "foo/bar/baa",
            "foo/bar/baz",
            "foo/bar/baZ",
        )
        val iterations = 500_000
        var dummy = 0

        // 纯Kotlin实现
        val t1 = measureTimeMillis {
            repeat(iterations) {
                for (pattern in patterns) {
                    for (path in paths) {
                        if (AntMatcher.matches(path, pattern)) dummy++
                    }
                }
            }
        }

        // 正则实现
        val t2 = measureTimeMillis {
            repeat(iterations) {
                for (pattern in patterns) {
                    for (path in paths) {
                        // 注意必须在这个循环中直接调用匹配方法，否则测试结果可能会显示基于正则的实现更快
                        if (AntFromRegexMatcher.matches(path, pattern)) dummy++
                    }
                }
            }
        }

        println("Pure Kotlin impl: $t1 ms")
        println("Regex impl: $t2 ms")
        println("Ratio: ${t1.toDouble() / t2}")
        println("Dummy size: $dummy")

        // Pure Kotlin impl: 7184 ms
        // Regex impl: 14275 ms
        // Ratio: 0.5032574430823117
        // Dummy size: 51500000

        Assert.assertEquals(51500000, dummy)
    }
}

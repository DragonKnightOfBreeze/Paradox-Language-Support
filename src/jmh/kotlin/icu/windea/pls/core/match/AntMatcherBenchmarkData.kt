package icu.windea.pls.core.match

/**
 * [AntMatcherBenchmark] 与 [RegexBasedAntMatcherBenchmark] 共用的输入数据。
 *
 * 数据在类初始化时生成，不参与被测方法的耗时。
 */
internal object AntMatcherBenchmarkData {
    const val BASE_OPERATIONS = 11 * 11
    const val DEEP_PATH_COUNT = 64
    const val DEEP_PATTERN_COUNT = 6
    const val DEEP_OPERATIONS = DEEP_PATH_COUNT * DEEP_PATTERN_COUNT
    const val LONG_PATH_COUNT = 64
    const val LONG_PATTERN_COUNT = 6
    const val LONG_OPERATIONS = LONG_PATH_COUNT * LONG_PATTERN_COUNT
    const val PATTERN_SET_PATH_COUNT = LONG_PATH_COUNT + 11
    const val UNIQUE_PATTERN_COUNT = 12_000
    const val MISS_OPERATIONS = 11 * 3

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

    val deepPatterns = listOf(
        "**/leaf0",
        "k1/**/leaf*",
        "k1/k2/**/k10/**",
        "*/k*/**/leaf*",
        "k1/*/k3/**/k10/leaf?",
        "**/k5/**",
    )

    val deepPaths = List(DEEP_PATH_COUNT) { index ->
        (1..10).joinToString("/") { "k$it" } + "/leaf$index"
    }

    val longPatterns = listOf(
        "gfx/models/**/*.dds",
        "gfx/**/model_*.dds",
        "**/entities/**/model_1?.dds",
        "gfx/models/entities/**",
        "gfx/*/entities/**",
        "**/segment_5_*/**",
    )

    val longPaths = List(LONG_PATH_COUNT) { index ->
        val segments = (1..12).joinToString("/") { "segment_${it}_xxxxxxxx" }
        "gfx/models/entities/$segments/model_$index.dds"
    }

    val missPatterns = listOf(
        "nonexistent/**",
        "**/not_found",
        "foo/bar/never?",
    )

    val patternSet = listOf(
        "common/**",
        "events/**",
        "gfx/models/**",
        "gfx/interface/**",
        "localisation/**",
        "history/**",
        "decisions/**",
        "scripted_triggers/**",
        "**/models/**",
        "**/interface/**",
    )

    val patternSetPaths = longPaths + paths

    val uniquePatterns = List(UNIQUE_PATTERN_COUNT) { index ->
        "generated/group_$index/**/leaf_$index"
    }

    const val UNIQUE_PATH = "generated/group_0/leaf_0"
}

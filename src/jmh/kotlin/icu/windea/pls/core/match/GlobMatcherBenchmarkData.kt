package icu.windea.pls.core.match

/**
 * [GlobMatcherBenchmark] 与 [RegexBasedGlobMatcherBenchmark] 共用的输入数据。
 *
 * 数据在类初始化时生成，不参与被测方法的耗时。
 */
internal object GlobMatcherBenchmarkData {
    const val SHORT_OPERATIONS = 11 * 11
    const val LONG_PATH_COUNT = 8
    const val LONG_PATTERN_COUNT = 8
    const val LONG_OPERATIONS = LONG_PATH_COUNT * LONG_PATTERN_COUNT
    const val MISS_OPERATIONS = 11 * 5

    val patterns = listOf(
        "*",
        "foo*",
        "*bar",
        "foo*bar",
        "foo?bar",
        "*_*",
        "prefix*",
        "*suffix",
        "a*b*c",
        "*.*",
        "foo*bar*baz",
    )

    val inputs = listOf(
        "foo",
        "bar",
        "foobar",
        "foo_bar",
        "prefix_foo_bar",
        "foo.txt",
        "a/b/c",
        "abc",
        "a_b_c",
        "foobarbaz",
        "x",
    )

    val longPatterns = listOf(
        "*",
        "prefix*",
        "*suffix",
        "prefix*suffix",
        "*a*a*a*",
        "*a?b?c*",
        "*middle*",
        "prefix*_*suffix",
    )

    val longInputs = List(LONG_PATH_COUNT) { index ->
        val body = "a".repeat(32 + index * 8)
        "prefix_${body}_middle_${body}_suffix_$index"
    }

    val missPatterns = listOf(
        "none*",
        "*not_found",
        "foo?bar?baz",
        "prefix",
        "*@*",
    )
}

package icu.windea.pls.core

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

private const val PATH_CASE_COUNT = 7
private const val DELIMITED_TEXT_COUNT = 8
private const val DELIMITED_STRING_LIST_COUNT = 4

/**
 * `StdlibFastExtraExtensions.kt` 中的字符串/路径相关扩展方法的基准测试。
 *
 * 每个 `@Fast` 方法都与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class StdlibFastExtraExtensionsBenchmark {
    private val pathCases = listOf(
        PathCase("a/b/c", "a/b/c"),
        PathCase("a/b", "a/b/c"),
        PathCase("a/b", "a/bc"),
        PathCase("a/b/c", "a/b"),
        PathCase("/a/b/", "a/b/c", trim = true),
        PathCase("a/b", "a/b/c", strict = true),
        PathCase("a/b", "a/b/c/d", strict = true),
    )

    private val delimitedTexts = listOf(
        "a,b,c",
        " a , b , c ",
        "a,,c",
        ",a,b,",
        "",
        "abc",
        "a",
        "a,b,c,d,e,f,g,h,i,j",
    )

    private val delimitedStringLists = listOf(
        emptyList(),
        listOf("a"),
        listOf("a", "b", "c", "d", "e", "f", "g", "h"),
        listOf("a", "b", "c"),
    )

    // region matchesPath

    @Benchmark
    @OperationsPerInvocation(PATH_CASE_COUNT)
    fun matchesPath(blackhole: Blackhole) {
        for (case in pathCases) {
            blackhole.consume(
                case.path.matchesPath(
                    case.other,
                    acceptSelf = case.acceptSelf,
                    strict = case.strict,
                    trim = case.trim,
                ),
            )
        }
    }

    @Benchmark
    @OperationsPerInvocation(PATH_CASE_COUNT)
    fun matchesPathByReference(blackhole: Blackhole) {
        for (case in pathCases) {
            blackhole.consume(
                referenceMatchesPath(
                    case.path,
                    case.other,
                    acceptSelf = case.acceptSelf,
                    strict = case.strict,
                    trim = case.trim,
                ),
            )
        }
    }

    // endregion

    // region toDelimitedList

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedList(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(text.toDelimitedList())
        }
    }

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedListByReference(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(referenceToDelimitedList(text))
        }
    }

    // endregion

    // region toDelimitedSet

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedSet(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(text.toDelimitedSet())
        }
    }

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedSetByReference(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(referenceToDelimitedSet(text))
        }
    }

    // endregion

    // region toDelimitedMutableList

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedMutableList(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(text.toDelimitedMutableList())
        }
    }

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedMutableListByReference(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(referenceToDelimitedMutableList(text))
        }
    }

    // endregion

    // region toDelimitedMutableSet

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedMutableSet(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(text.toDelimitedMutableSet())
        }
    }

    @Benchmark
    @OperationsPerInvocation(DELIMITED_TEXT_COUNT)
    fun toDelimitedMutableSetByReference(blackhole: Blackhole) {
        for (text in delimitedTexts) {
            blackhole.consume(referenceToDelimitedMutableSet(text))
        }
    }

    // endregion

    // region toDelimitedString

    @Benchmark
    @OperationsPerInvocation(DELIMITED_STRING_LIST_COUNT)
    fun toDelimitedString(blackhole: Blackhole) {
        for (list in delimitedStringLists) {
            blackhole.consume(list.toDelimitedString())
        }
    }

    @Benchmark
    @OperationsPerInvocation(DELIMITED_STRING_LIST_COUNT)
    fun toDelimitedStringByReference(blackhole: Blackhole) {
        for (list in delimitedStringLists) {
            blackhole.consume(list.joinToString(","))
        }
    }

    // endregion
}

private data class PathCase(
    val path: String,
    val other: String,
    val acceptSelf: Boolean = true,
    val strict: Boolean = false,
    val trim: Boolean = false,
)

private fun referenceMatchesPath(
    path: String,
    other: String,
    acceptSelf: Boolean,
    strict: Boolean,
    trim: Boolean,
): Boolean {
    val p = if (trim) path.trim('/') else path
    if (!other.startsWith(p)) return false
    if (p.length == other.length) return acceptSelf
    if (other[p.length] != '/') return false
    if (strict && other.indexOf('/', p.length + 1) != -1) return false
    return true
}

private fun referenceToDelimitedList(text: String): List<String> {
    return text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

private fun referenceToDelimitedSet(text: String): Set<String> {
    return text.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}

private fun referenceToDelimitedMutableList(text: String): MutableList<String> {
    return text.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
}

private fun referenceToDelimitedMutableSet(text: String): MutableSet<String> {
    return text.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
}

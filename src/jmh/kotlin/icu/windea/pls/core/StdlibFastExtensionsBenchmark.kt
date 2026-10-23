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

private const val ASCII_PAIR_COUNT = 4
private const val NON_ASCII_PAIR_COUNT = 3
private const val TRIM_COUNT = 9
private const val SPLIT_COUNT = 7
private const val JOIN_LIST_COUNT = 4

/**
 * [StdlibFastExtensions] 中字符串相关扩展方法的基准测试。
 *
 * 每个 `@Fast` 方法都与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 *
 * @see StdlibFastExtensions
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class StdlibFastExtensionsBenchmark {
    private val asciiPairs = listOf(
        "foo_bar_baz" to "foo_bar_baz",
        "foo_bar_baz" to "foo_bar_baZ",
        "foo_bar_baz" to "foo_bar_ba",
        "abc" to "abd",
    )

    private val nonAsciiPairs = listOf(
        "变量_名字" to "变量_名字",
        "变量_名字" to "变量_名子",
        "éclair" to "éclair",
    )

    private val trimTexts = listOf(
        "/foo/bar/",
        "//foo//",
        "foo/bar",
        "////",
        "/",
        "",
        "///foo",
        "foo///",
        "foo/bar/baz",
    )

    private val splitTexts = listOf(
        "a/b/c",
        "a//c",
        "abc",
        "/a/b/",
        "",
        "a/b/c/d/e",
        "/",
    )

    private val joinLists = listOf(
        emptyList<String>(),
        listOf("a"),
        listOf("a", "b", "c", "d", "e", "f", "g", "h"),
        listOf("a", "b", "c"),
    )

    @Benchmark
    @OperationsPerInvocation(ASCII_PAIR_COUNT)
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    open fun equalsFastAscii(blackhole: Blackhole) {
        for ((a, b) in asciiPairs) {
            blackhole.consume(a.equalsFast(b))
        }
    }

    @Benchmark
    @OperationsPerInvocation(ASCII_PAIR_COUNT)
    open fun equalsAscii(blackhole: Blackhole) {
        for ((a, b) in asciiPairs) {
            blackhole.consume(a.equals(b))
        }
    }

    @Benchmark
    @OperationsPerInvocation(ASCII_PAIR_COUNT)
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    open fun equalsFastAsciiIgnoreCase(blackhole: Blackhole) {
        for ((a, b) in asciiPairs) {
            blackhole.consume(a.equalsFast(b, ignoreCase = true))
        }
    }

    @Benchmark
    @OperationsPerInvocation(ASCII_PAIR_COUNT)
    open fun equalsAsciiIgnoreCase(blackhole: Blackhole) {
        for ((a, b) in asciiPairs) {
            blackhole.consume(a.equals(b, ignoreCase = true))
        }
    }

    @Benchmark
    @OperationsPerInvocation(NON_ASCII_PAIR_COUNT)
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    open fun equalsFastNonAscii(blackhole: Blackhole) {
        for ((a, b) in nonAsciiPairs) {
            blackhole.consume(a.equalsFast(b))
        }
    }

    @Benchmark
    @OperationsPerInvocation(NON_ASCII_PAIR_COUNT)
    open fun equalsNonAscii(blackhole: Blackhole) {
        for ((a, b) in nonAsciiPairs) {
            blackhole.consume(a.equals(b))
        }
    }

    @Benchmark
    @OperationsPerInvocation(TRIM_COUNT)
    open fun trimFast(blackhole: Blackhole) {
        for (text in trimTexts) {
            blackhole.consume(text.trimFast('/'))
        }
    }

    @Benchmark
    @OperationsPerInvocation(TRIM_COUNT)
    open fun trim(blackhole: Blackhole) {
        for (text in trimTexts) {
            blackhole.consume(text.trim('/'))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SPLIT_COUNT)
    open fun splitFast(blackhole: Blackhole) {
        for (text in splitTexts) {
            blackhole.consume(text.splitFast('/'))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SPLIT_COUNT)
    open fun split(blackhole: Blackhole) {
        for (text in splitTexts) {
            blackhole.consume(text.split('/'))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SPLIT_COUNT)
    open fun splitFastWithLimit(blackhole: Blackhole) {
        for (text in splitTexts) {
            blackhole.consume(text.splitFast('/', limit = 2))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SPLIT_COUNT)
    open fun splitWithLimit(blackhole: Blackhole) {
        for (text in splitTexts) {
            blackhole.consume(text.split('/', limit = 2))
        }
    }

    @Benchmark
    @OperationsPerInvocation(JOIN_LIST_COUNT)
    open fun joinToStringFast(blackhole: Blackhole) {
        for (list in joinLists) {
            blackhole.consume(list.joinToStringFast())
        }
    }

    @Benchmark
    @OperationsPerInvocation(JOIN_LIST_COUNT)
    open fun joinToString(blackhole: Blackhole) {
        for (list in joinLists) {
            blackhole.consume(list.joinToString())
        }
    }

    @Benchmark
    @OperationsPerInvocation(JOIN_LIST_COUNT)
    open fun joinToStringFastWithTransform(blackhole: Blackhole) {
        for (list in joinLists) {
            blackhole.consume(list.joinToStringFast { it.uppercase() })
        }
    }

    @Benchmark
    @OperationsPerInvocation(JOIN_LIST_COUNT)
    open fun joinToStringWithTransform(blackhole: Blackhole) {
        for (list in joinLists) {
            blackhole.consume(list.joinToString { it.uppercase() })
        }
    }
}

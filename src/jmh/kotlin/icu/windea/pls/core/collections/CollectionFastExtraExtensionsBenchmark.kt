package icu.windea.pls.core.collections

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

private const val PREFIX_CASE_COUNT = 6
private const val SUFFIX_CASE_COUNT = 6

/**
 * [CollectionFastExtraExtensions] 的基准测试。
 *
 * 每个 `@Fast` 方法都与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 *
 * @see CollectionFastExtraExtensions
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class CollectionFastExtraExtensionsBenchmark {
    private val prefixCases = listOf(
        SliceCase(listOf("a", "b", "c", "d"), emptyList()),
        SliceCase(listOf("a", "b", "c", "d"), listOf("a", "b")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("a", "b", "c", "d")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("a", "x")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("a", "*"), wildcard = "*"),
        SliceCase(listOf("a", "b"), listOf("a", "b", "c")),
    )

    private val suffixCases = listOf(
        SliceCase(listOf("a", "b", "c", "d"), emptyList()),
        SliceCase(listOf("a", "b", "c", "d"), listOf("c", "d")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("a", "b", "c", "d")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("x", "d")),
        SliceCase(listOf("a", "b", "c", "d"), listOf("*", "d"), wildcard = "*"),
        SliceCase(listOf("c", "d"), listOf("a", "b", "c", "d")),
    )

    @Benchmark
    @OperationsPerInvocation(PREFIX_CASE_COUNT)
    open fun removePrefixOrNull(blackhole: Blackhole) {
        for (case in prefixCases) {
            blackhole.consume(case.list.removePrefixOrNull(case.slice, case.wildcard))
        }
    }

    @Benchmark
    @OperationsPerInvocation(PREFIX_CASE_COUNT)
    open fun removePrefixOrNullByReference(blackhole: Blackhole) {
        for (case in prefixCases) {
            blackhole.consume(referenceRemovePrefix(case.list, case.slice, case.wildcard))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SUFFIX_CASE_COUNT)
    open fun removeSuffixOrNull(blackhole: Blackhole) {
        for (case in suffixCases) {
            blackhole.consume(case.list.removeSuffixOrNull(case.slice, case.wildcard))
        }
    }

    @Benchmark
    @OperationsPerInvocation(SUFFIX_CASE_COUNT)
    open fun removeSuffixOrNullByReference(blackhole: Blackhole) {
        for (case in suffixCases) {
            blackhole.consume(referenceRemoveSuffix(case.list, case.slice, case.wildcard))
        }
    }
}

private data class SliceCase(
    val list: List<String>,
    val slice: List<String>,
    val wildcard: String? = null,
)

private fun referenceRemovePrefix(list: List<String>, prefix: List<String>, wildcard: String?): List<String>? {
    if (prefix.isEmpty()) return list
    if (list.size < prefix.size) return null
    for (index in prefix.indices) {
        val e = prefix[index]
        if (wildcard != null && wildcard == e) continue
        if (e != list[index]) return null
    }
    return list.drop(prefix.size)
}

private fun referenceRemoveSuffix(list: List<String>, suffix: List<String>, wildcard: String?): List<String>? {
    if (suffix.isEmpty()) return list
    if (list.size < suffix.size) return null
    for (index in suffix.indices) {
        val e = suffix[index]
        if (wildcard != null && wildcard == e) continue
        if (e != list[list.size - suffix.size + index]) return null
    }
    return list.dropLast(suffix.size)
}

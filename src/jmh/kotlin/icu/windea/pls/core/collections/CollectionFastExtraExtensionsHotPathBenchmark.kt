package icu.windea.pls.core.collections

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

private const val HOT_PATH_SLICE_COUNT = 256

/**
 * `CollectionFastExtraExtensions.kt` 中的方法在“大量小规模切片操作”场景下的基准测试。
 *
 * 用于模拟索引构建、语义匹配流水线中对短成员路径的重复前缀/后缀处理。
 * 覆盖 0、1 元素等快速返回路径，并可通过 `gc` profiler 观察每次操作的临时分配。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class CollectionFastExtraExtensionsHotPathBenchmark {
    @Param("0", "1", "4", "64")
    var size: Int = 0

    private lateinit var cases: List<HotPathSliceCase>

    @Setup(Level.Trial)
    fun setup() {
        cases = List(HOT_PATH_SLICE_COUNT) { caseIndex ->
            val list = List(size) { elementIndex -> "k${caseIndex}_$elementIndex" }
            val half = size / 2
            HotPathSliceCase(
                list = list,
                prefixOne = if (size == 0) emptyList() else list.subList(0, 1),
                prefixHalf = list.subList(0, half),
                suffixOne = if (size == 0) emptyList() else list.subList(size - 1, size),
                suffixHalf = list.subList(size - half, size),
            )
        }
    }

    // region removePrefixOrNull

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removePrefixOrNullOne(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(case.list.removePrefixOrNull(case.prefixOne))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removePrefixOrNullOneByReference(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(referenceRemovePrefixOrNull(case.list, case.prefixOne))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removePrefixOrNullHalf(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(case.list.removePrefixOrNull(case.prefixHalf))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removePrefixOrNullHalfByReference(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(referenceRemovePrefixOrNull(case.list, case.prefixHalf))
        }
    }

    // endregion

    // region removeSuffixOrNull

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removeSuffixOrNullOne(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(case.list.removeSuffixOrNull(case.suffixOne))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removeSuffixOrNullOneByReference(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(referenceRemoveSuffixOrNull(case.list, case.suffixOne))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removeSuffixOrNullHalf(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(case.list.removeSuffixOrNull(case.suffixHalf))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_SLICE_COUNT)
    fun removeSuffixOrNullHalfByReference(blackhole: Blackhole) {
        for (case in cases) {
            blackhole.consume(referenceRemoveSuffixOrNull(case.list, case.suffixHalf))
        }
    }

    // endregion
}

private data class HotPathSliceCase(
    val list: List<String>,
    val prefixOne: List<String>,
    val prefixHalf: List<String>,
    val suffixOne: List<String>,
    val suffixHalf: List<String>,
)

private fun referenceRemovePrefixOrNull(list: List<String>, prefix: List<String>): List<String>? {
    if (prefix.isEmpty()) return list
    if (list.size < prefix.size) return null
    for (index in prefix.indices) {
        if (prefix[index] != list[index]) return null
    }
    return list.drop(prefix.size)
}

private fun referenceRemoveSuffixOrNull(list: List<String>, suffix: List<String>): List<String>? {
    if (suffix.isEmpty()) return list
    if (list.size < suffix.size) return null
    for (index in suffix.indices) {
        if (suffix[index] != list[list.size - suffix.size + index]) return null
    }
    return list.dropLast(suffix.size)
}

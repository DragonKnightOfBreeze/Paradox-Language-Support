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

private const val HOT_PATH_LIST_COUNT = 256

/**
 * `CollectionFastExtensions.kt` 中的方法在“大量小规模集合操作”场景下的基准测试。
 *
 * 用于模拟索引构建、语义匹配等流水线中 `X * Y * Z` 形式的重复操作：
 * 每次基准方法调用会处理 [HOT_PATH_LIST_COUNT] 个小集合，再按操作数归一化。
 * 重点覆盖 0、1 元素等快速返回路径，并可通过 `gc` profiler 观察每次操作的临时分配。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class CollectionFastExtensionsHotPathBenchmark {
    @Param("0", "1", "4", "64")
    var size: Int = 0

    private lateinit var lists: List<List<Int>>

    @Setup(Level.Trial)
    fun setup() {
        lists = List(HOT_PATH_LIST_COUNT) { listIndex ->
            List(size) { elementIndex -> listIndex * 31 + elementIndex }
        }
    }

    // region forEachFast

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun forEachFast(blackhole: Blackhole) {
        var sum = 0L
        for (list in lists) {
            list.forEachFast { sum += it }
        }
        blackhole.consume(sum)
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun forEach(blackhole: Blackhole) {
        var sum = 0L
        for (list in lists) {
            list.forEach { sum += it }
        }
        blackhole.consume(sum)
    }

    // endregion

    // region mapFast

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun mapFast(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.mapFast { it + 1 })
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun map(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.map { it + 1 })
        }
    }

    // endregion

    // region filterFast

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun filterFastNone(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.filterFast { it < 0 })
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun filterNone(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.filter { it < 0 })
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun filterFastHalf(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.filterFast { it % 2 == 0 })
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun filterHalf(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.filter { it % 2 == 0 })
        }
    }

    // endregion

    // region dropFast

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropFastZero(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.dropFast(0))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropZero(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.drop(0))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropFastOne(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.dropFast(1))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropOne(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.drop(1))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropFastHalf(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.dropFast(list.size / 2))
        }
    }

    @Benchmark
    @OperationsPerInvocation(HOT_PATH_LIST_COUNT)
    fun dropHalf(blackhole: Blackhole) {
        for (list in lists) {
            blackhole.consume(list.drop(list.size / 2))
        }
    }

    // endregion
}

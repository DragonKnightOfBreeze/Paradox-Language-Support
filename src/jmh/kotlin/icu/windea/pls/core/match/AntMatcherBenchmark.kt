package icu.windea.pls.core.match

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

/**
 * [AntMatcher] 的基准测试。
 *
 * 备注：
 * - 既包含固定的微基准，也包含更接近实际使用的成员路径、文件路径和模式集合场景。
 *
 * @see AntMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class AntMatcherBenchmark {
    @Benchmark
    @OperationsPerInvocation(AntMatcherBenchmarkData.BASE_OPERATIONS)
    open fun baseline(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.patterns) {
            for (path in AntMatcherBenchmarkData.paths) {
                blackhole.consume(pattern.isNotEmpty() && path.isNotEmpty())
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(AntMatcherBenchmarkData.BASE_OPERATIONS)
    open fun matches(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.patterns) {
            for (path in AntMatcherBenchmarkData.paths) {
                blackhole.consume(AntMatcher.matches(path, pattern))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(AntMatcherBenchmarkData.BASE_OPERATIONS)
    open fun matchesIgnoreCase(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.patterns) {
            for (path in AntMatcherBenchmarkData.paths) {
                blackhole.consume(AntMatcher.matches(path, pattern, ignoreCase = true))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(AntMatcherBenchmarkData.DEEP_OPERATIONS)
    open fun matchesDeepMemberPath(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.deepPatterns) {
            for (path in AntMatcherBenchmarkData.deepPaths) {
                blackhole.consume(AntMatcher.matches(path, pattern))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(AntMatcherBenchmarkData.LONG_OPERATIONS)
    open fun matchesLongFilePath(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.longPatterns) {
            for (path in AntMatcherBenchmarkData.longPaths) {
                blackhole.consume(AntMatcher.matches(path, pattern))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(AntMatcherBenchmarkData.PATTERN_SET_PATH_COUNT)
    open fun matchesFirstInPatternSet(blackhole: Blackhole) {
        var matched = false
        for (path in AntMatcherBenchmarkData.patternSetPaths) {
            for (pattern in AntMatcherBenchmarkData.patternSet) {
                if (AntMatcher.matches(path, pattern)) {
                    matched = true
                    break
                }
            }
        }
        blackhole.consume(matched)
    }

    @Benchmark
    @Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(AntMatcherBenchmarkData.UNIQUE_PATTERN_COUNT)
    open fun matchesUniquePatterns(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.uniquePatterns) {
            blackhole.consume(AntMatcher.matches(AntMatcherBenchmarkData.UNIQUE_PATH, pattern))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(AntMatcherBenchmarkData.MISS_OPERATIONS)
    open fun matchesMiss(blackhole: Blackhole) {
        for (pattern in AntMatcherBenchmarkData.missPatterns) {
            for (path in AntMatcherBenchmarkData.paths) {
                blackhole.consume(AntMatcher.matches(path, pattern))
            }
        }
    }
}

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

private const val INT_COUNT = 12
private const val FLOAT_COUNT = 12
private const val PERCENTAGE_COUNT = 9
private const val DATE_COUNT = 7

/**
 * [TextMatcher] 中非平凡方法的基准测试。
 *
 * `isNumberUnaryChar` 等简单字符判断不纳入基准。
 * 结果仅作为示例，不作为长期性能报告。
 *
 * @see TextMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class TextMatcherBenchmark {
    private val intTexts = listOf(
        "12345",
        "0",
        "000",
        "+123",
        "-456",
        "12a34",
        "12 34",
        "abc",
        "12.34",
        "+",
        "-",
        "",
    )

    private val floatTexts = listOf(
        "3.14",
        "-2.718",
        ".5",
        "5.",
        "1.2.3",
        "1..2",
        "1.2a",
        "abc",
        "123",
        "+",
        ".",
        "",
    )

    private val percentageTexts = listOf(
        "100%",
        "0%",
        "+50%",
        "-25%",
        "12.5%",
        "abc%",
        "%",
        "100",
        "",
    )

    private val dateTexts = listOf(
        "2024.1.15",
        "1000.12.31",
        "1.1.1",
        "not.a.date",
        "2024-01-15",
        "-2200.1.1",
        "",
    )

    @Benchmark
    @OperationsPerInvocation(INT_COUNT)
    open fun matchesInt(blackhole: Blackhole) {
        for (text in intTexts) {
            blackhole.consume(TextMatcher.matchesInt(text))
        }
    }

    @Benchmark
    @OperationsPerInvocation(INT_COUNT)
    open fun matchesIntWithoutLeadingUnary(blackhole: Blackhole) {
        for (text in intTexts) {
            blackhole.consume(TextMatcher.matchesInt(text, leadingUnary = false))
        }
    }

    @Benchmark
    @OperationsPerInvocation(FLOAT_COUNT)
    open fun matchesFloat(blackhole: Blackhole) {
        for (text in floatTexts) {
            blackhole.consume(TextMatcher.matchesFloat(text))
        }
    }

    @Benchmark
    @OperationsPerInvocation(FLOAT_COUNT)
    open fun matchesFloatStrictDot(blackhole: Blackhole) {
        for (text in floatTexts) {
            blackhole.consume(TextMatcher.matchesFloat(text, lenientDot = false))
        }
    }

    @Benchmark
    @OperationsPerInvocation(PERCENTAGE_COUNT)
    open fun matchesIntPercentageField(blackhole: Blackhole) {
        for (text in percentageTexts) {
            blackhole.consume(TextMatcher.matchesIntPercentageField(text))
        }
    }

    @Benchmark
    @OperationsPerInvocation(PERCENTAGE_COUNT)
    open fun matchesFloatPercentageField(blackhole: Blackhole) {
        for (text in percentageTexts) {
            blackhole.consume(TextMatcher.matchesFloatPercentageField(text))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(DATE_COUNT)
    open fun matchesDateField(blackhole: Blackhole) {
        for (text in dateTexts) {
            blackhole.consume(TextMatcher.matchesDateField(text))
        }
    }
}

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
private const val VALID_DATE_COUNT = 4
private const val INVALID_DATE_COUNT = 3
private const val DATE_COUNT = VALID_DATE_COUNT + INVALID_DATE_COUNT

/**
 * [TextMatcher] 中非平凡方法的基准测试。
 *
 * `isNumberUnaryChar` 等简单字符判断不纳入基准。
 * 包含与惯用形式、正则参考实现和日期预检查的对比；结果用于回归观察和相对比较。
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

    private val validDateTexts = listOf(
        "2024.1.15",
        "1000.12.31",
        "1.1.1",
        "-2200.1.1",
    )

    private val invalidDateTexts = listOf(
        "not.a.date",
        "2024-01-15",
        "",
    )

    private val dateTexts = validDateTexts + invalidDateTexts

    private val intRegex = Regex("^[+-]?\\d+$")
    private val floatRegex = Regex("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)$")
    private val floatStrictRegex = Regex("^[+-]?\\d+(\\.\\d+)?$")
    private val datePrecheckRegex = Regex("^[+-]?\\d{1,4}\\.\\d{1,2}\\.\\d{1,2}$")

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

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(INT_COUNT)
    open fun matchesIntByRegex(blackhole: Blackhole) {
        for (text in intTexts) {
            blackhole.consume(intRegex.matches(text))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(INT_COUNT)
    open fun matchesIntByIdiomatic(blackhole: Blackhole) {
        // 注意：toIntOrNull 与 matchesInt 的溢出语义不完全一致，数据集中不包含溢出值
        for (text in intTexts) {
            blackhole.consume(text.toIntOrNull() != null)
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(FLOAT_COUNT)
    open fun matchesFloatByRegex(blackhole: Blackhole) {
        for (text in floatTexts) {
            blackhole.consume(floatRegex.matches(text))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(FLOAT_COUNT)
    open fun matchesFloatByIdiomatic(blackhole: Blackhole) {
        // 注意：toDoubleOrNull 接受指数、NaN、Infinity 等，数据集中不包含这些形式
        for (text in floatTexts) {
            blackhole.consume(text.toDoubleOrNull() != null)
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(FLOAT_COUNT)
    open fun matchesFloatStrictDotByRegex(blackhole: Blackhole) {
        for (text in floatTexts) {
            blackhole.consume(floatStrictRegex.matches(text))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(PERCENTAGE_COUNT)
    open fun matchesIntPercentageFieldByIdiomatic(blackhole: Blackhole) {
        for (text in percentageTexts) {
            blackhole.consume(text.endsWith("%") && text.dropLast(1).toIntOrNull() != null)
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(PERCENTAGE_COUNT)
    open fun matchesFloatPercentageFieldByIdiomatic(blackhole: Blackhole) {
        for (text in percentageTexts) {
            blackhole.consume(text.endsWith("%") && text.dropLast(1).toDoubleOrNull() != null)
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(DATE_COUNT)
    open fun matchesDateFieldWithPrecheck(blackhole: Blackhole) {
        for (text in dateTexts) {
            val result = datePrecheckRegex.matches(text) && TextMatcher.matchesDateField(text)
            blackhole.consume(result)
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(VALID_DATE_COUNT)
    open fun matchesDateFieldValid(blackhole: Blackhole) {
        for (text in validDateTexts) {
            blackhole.consume(TextMatcher.matchesDateField(text))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(INVALID_DATE_COUNT)
    open fun matchesDateFieldInvalid(blackhole: Blackhole) {
        for (text in invalidDateTexts) {
            blackhole.consume(TextMatcher.matchesDateField(text))
        }
    }
}

package icu.windea.pls.core.match

import icu.windea.pls.test.ChronicleAssume
import org.junit.AssumptionViolatedException
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File

/**
 * 用于在受限测试中运行 JMH 基准测试的启动器。
 *
 * 基准测试类位于 `src/jmh/kotlin`（`AntMatcherBenchmark`、`AntFromRegexBenchmark`），
 * 通过 `ChronicleAssume.includeBenchmark()` 控制是否执行。
 */
object MatchersBenchmarkLauncher {
    private val benchmarkClassNames = listOf(
        "icu.windea.pls.core.match.AntMatcherBenchmark",
        "icu.windea.pls.core.match.AntFromRegexBenchmark",
    )

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            ChronicleAssume.includeBenchmark()
        } catch (e: AssumptionViolatedException) {
            println("Skip benchmarks: ${e.message}")
            return
        }

        val names = if (args.isEmpty()) benchmarkClassNames else args.toList()
        for (name in names) {
            runBenchmark(name)
        }
    }

    private fun runBenchmark(benchmarkClassName: String) {
        val resultFile = File("build/reports/benchmarks/${benchmarkClassName.substringAfterLast('.')}.txt")
        resultFile.parentFile.mkdirs()

        val options = OptionsBuilder()
            .include(benchmarkClassName)
            .result(resultFile.absolutePath)
            .resultFormat(ResultFormatType.TEXT)
            .build()

        Runner(options).run()
    }
}

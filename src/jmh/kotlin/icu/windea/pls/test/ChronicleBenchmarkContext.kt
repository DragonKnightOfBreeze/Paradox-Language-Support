package icu.windea.pls.test

import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.core.toDelimitedList
import icu.windea.pls.core.toPath
import java.io.File
import java.nio.file.Path

/**
 * 用于运行 JMH 基准测试的上下文对象。
 */
class ChronicleBenchmarkContext {
    val classDirs: List<Path> = computeClassDirs()
    val reportDir: Path = computeReportDir()
    val profiles: List<String> = computeProfiles()
    val params: List<Pair<String, String>> = computeParams()

    private fun computeReportDir(): Path {
        return ChronicleCapabilities.Benchmark.reportDir
            .toPath()
    }

    private fun computeClassDirs(): List<Path> {
        return ChronicleCapabilities.Benchmark.classesDir
            .toDelimitedList(File.pathSeparatorChar)
            .map { it.toPath() }
    }

    private fun computeProfiles(): List<String> {
        return ChronicleCapabilities.Benchmark.profilers
            .toDelimitedList(',')
    }

    private fun computeParams(): List<Pair<String, String>> {
        return ChronicleCapabilities.Benchmark.params
            .toDelimitedList(',')
            .mapNotNull { entry ->
                val index = entry.indexOf('=')
                if (index <= 0 || index == entry.lastIndex) return@mapNotNull null
                entry.substring(0, index).trim() to entry.substring(index + 1).trim()
            }
    }
}

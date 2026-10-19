package icu.windea.pls.test

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.jar.JarFile
import java.util.regex.Pattern

private const val JMH_VERSION = "1.37"
private const val CLASSES_DIR_PROPERTY = "chronicle.benchmark.classesDir"
private const val REPORT_DIR_PROPERTY = "chronicle.benchmark.reportDir"
private const val DEFAULT_REPORT_DIR = "build/reports/benchmarks"

/**
 * 用于运行 JMH 基准测试的通用启动器。
 *
 * 基准测试类位于 `src/jmh/kotlin`，通过 Gradle 的 `benchmark` 任务运行。
 * 如果未传入参数，则扫描 `chronicle.benchmark.classesDir`（或回退到 classpath）
 * 下所有以 `Benchmark` 结尾、且包含 `@Benchmark` 方法的类。
 */
object ChronicleBenchmarkLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val classDirs = System.getProperty(CLASSES_DIR_PROPERTY)
            ?.split(File.pathSeparator)
            ?.filter { it.isNotBlank() }
            ?.map { Paths.get(it) }
            ?: emptyList()
        val reportDir = Paths.get(System.getProperty(REPORT_DIR_PROPERTY) ?: DEFAULT_REPORT_DIR)

        val allBenchmarks = discoverBenchmarks(classDirs)
        if (allBenchmarks.isEmpty()) {
            error("No benchmarks found. Run it via `./gradlew benchmark` or set -D$CLASSES_DIR_PROPERTY.")
        }

        val selectedBenchmarks = selectBenchmarks(allBenchmarks, args.toList())
        if (selectedBenchmarks.isEmpty()) {
            val available = allBenchmarks.joinToString { it.className }
            error("No benchmarks matched: ${args.joinToString()}. Available benchmarks: $available")
        }

        prepareReportDir(reportDir)
        writeRunMetadata(reportDir, selectedBenchmarks)

        for (benchmark in selectedBenchmarks) {
            println("Benchmark: ${benchmark.className} [${benchmark.methods.joinToString()}]")
            runBenchmark(benchmark, reportDir)
        }
    }

    private fun discoverBenchmarks(classDirs: List<Path>): List<BenchmarkDescriptor> {
        val classNames = linkedSetOf<String>()
        if (classDirs.isNotEmpty()) {
            classDirs.forEach { scanDirectory(it, classNames) }
        } else {
            scanClasspath(classNames)
        }
        return classNames.mapNotNull(::toBenchmarkDescriptor).sortedBy { it.className }
    }

    private fun scanDirectory(root: Path, classNames: MutableSet<String>) {
        if (!Files.isDirectory(root)) return
        Files.walk(root).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .map { root.relativize(it).toString().replace(File.separatorChar, '/') }
                .filter { it.endsWith("Benchmark.class") }
                .filter { !it.contains("jmh_generated") }
                .filter { !it.contains('$') }
                .forEach { relativePath ->
                    classNames.add(relativePath.removeSuffix(".class").replace('/', '.'))
                }
        }
    }

    private fun scanClasspath(classNames: MutableSet<String>) {
        val classpath = System.getProperty("java.class.path").orEmpty()
        for (entry in classpath.split(File.pathSeparator)) {
            if (entry.isBlank()) continue
            val path = Paths.get(entry)
            when {
                Files.isDirectory(path) -> scanDirectory(path, classNames)
                Files.isRegularFile(path) && entry.endsWith(".jar", ignoreCase = true) -> scanJar(path, classNames)
            }
        }
    }

    private fun scanJar(path: Path, classNames: MutableSet<String>) {
        runCatching {
            JarFile(path.toFile()).use { jar ->
                for (entry in jar.entries()) {
                    if (entry.isDirectory) continue
                    val name = entry.name
                    if (!name.startsWith("icu/windea/pls/")) continue
                    if (!name.endsWith("Benchmark.class")) continue
                    if (name.contains("jmh_generated") || name.contains('$')) continue
                    classNames.add(name.removeSuffix(".class").replace('/', '.'))
                }
            }
        }
    }

    private fun toBenchmarkDescriptor(className: String): BenchmarkDescriptor? {
        return runCatching {
            val classLoader = Thread.currentThread().contextClassLoader
                ?: ChronicleBenchmarkLauncher::class.java.classLoader
            val clazz = Class.forName(className, false, classLoader)
            if (Modifier.isAbstract(clazz.modifiers)) return@runCatching null
            if (clazz.isInterface || clazz.isEnum || clazz.isAnnotation) return@runCatching null

            val methods = linkedSetOf<String>()
            var current: Class<*>? = clazz
            while (current != null) {
                for (method in current.declaredMethods) {
                    if (method.isAnnotationPresent(Benchmark::class.java)) {
                        methods.add(method.name)
                    }
                }
                current = current.superclass
            }
            if (methods.isEmpty()) return@runCatching null
            BenchmarkDescriptor(className, methods.sorted())
        }.getOrNull()
    }

    private fun selectBenchmarks(
        allBenchmarks: List<BenchmarkDescriptor>,
        patterns: List<String>,
    ): List<BenchmarkDescriptor> {
        if (patterns.isEmpty()) return allBenchmarks

        val regexes = patterns.map { it.toGlobRegex() }
        return allBenchmarks.mapNotNull { benchmark ->
            val simpleName = benchmark.className.substringAfterLast('.')
            val selectedMethods = benchmark.methods.filter { method ->
                val candidates = listOf(
                    benchmark.className,
                    simpleName,
                    "${benchmark.className}.$method",
                    "$simpleName.$method",
                )
                regexes.any { regex -> candidates.any { regex.matches(it) } }
            }
            if (selectedMethods.isEmpty()) null else benchmark.copy(methods = selectedMethods)
        }
    }

    private fun String.toGlobRegex(): Regex {
        val regex = buildString {
            append('^')
            for (c in this@toGlobRegex) {
                when (c) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    else -> {
                        if (c.isLetterOrDigit() || c == '_') append(c) else append('\\').append(c)
                    }
                }
            }
            append('$')
        }
        return Regex(regex)
    }

    private fun prepareReportDir(reportDir: Path) {
        Files.createDirectories(reportDir)
        Files.list(reportDir).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".json") }
                .forEach { Files.deleteIfExists(it) }
        }
    }

    private fun writeRunMetadata(reportDir: Path, benchmarks: List<BenchmarkDescriptor>) {
        val entries = benchmarks.joinToString(",") { benchmark ->
            val methods = benchmark.methods.joinToString(",") { "\"$it\"" }
            """{"className":"${benchmark.className}","methods":[$methods]}"""
        }
        val content = buildString {
            append("""{"timestamp":"${Instant.now()}","jmhVersion":"$JMH_VERSION",""")
            append(""""javaVersion":"${System.getProperty("java.version")}",""")
            append(""""javaVendor":"${System.getProperty("java.vendor")}",""")
            append(""""osName":"${System.getProperty("os.name")}",""")
            append(""""osVersion":"${System.getProperty("os.version")}",""")
            append(""""osArch":"${System.getProperty("os.arch")}",""")
            append(""""processors":${Runtime.getRuntime().availableProcessors()},""")
            append(""""benchmarks":[$entries]}""")
        }
        Files.writeString(reportDir.resolve("run.json"), content)
    }

    private fun runBenchmark(benchmark: BenchmarkDescriptor, reportDir: Path) {
        val resultFile = reportDir.resolve("${benchmark.className}.json").toFile()
        val options = OptionsBuilder()
            .apply {
                benchmark.methods.forEach { method ->
                    include("^${Pattern.quote(benchmark.className)}\\.${Pattern.quote(method)}$")
                }
            }
            .result(resultFile.absolutePath)
            .resultFormat(ResultFormatType.JSON)
            .build()
        Runner(options).run()
    }

    private data class BenchmarkDescriptor(
        val className: String,
        val methods: List<String>,
    )
}

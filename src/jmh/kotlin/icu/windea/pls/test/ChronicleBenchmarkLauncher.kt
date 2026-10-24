package icu.windea.pls.test

import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.core.match.GlobMatcher
import icu.windea.pls.core.toDelimitedList
import icu.windea.pls.core.toPath
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

/**
 * 用于运行 JMH 基准测试的通用启动器。
 *
 * 基准测试类位于 `src/jmh/kotlin`，通过 Gradle 的 `benchmark` 任务运行。
 * 如果未传入参数，则扫描 `ChronicleCapabilities.Benchmark.classesDir`（或回退到 classpath）
 * 下所有以 `Benchmark` 结尾、且包含 `@Benchmark` 方法的类。
 *
 * 相关的能力项（[ChronicleCapabilities.Benchmark]）：
 * - [classesDir][ChronicleCapabilities.Benchmark.classesDir]：基准测试类目录，多个目录使用系统路径分隔符分隔。
 * - [reportDir][ChronicleCapabilities.Benchmark.reportDir]：报告输出目录。
 * - [profilers][ChronicleCapabilities.Benchmark.profilers]：JMH profiler 列表，逗号分隔。
 * - [params][ChronicleCapabilities.Benchmark.params]：JMH 参数过滤列表，逗号分隔的 `key=value` 形式。
 *
 * @see ChronicleBenchmarkContext
 * @see ChronicleBenchmarkDescriptor
 */
object ChronicleBenchmarkLauncher {
    private const val JMH_VERSION = "1.37"

    @JvmStatic
    fun main(args: Array<String>) {
        val context = ChronicleBenchmarkContext()

        val allBenchmarks = discoverBenchmarks(context)
        if (allBenchmarks.isEmpty()) {
            error("No benchmarks found. Run it via `./gradlew benchmark` or set -Dchronicle.benchmark.classesDir.")
        }

        val selectedBenchmarks = selectBenchmarks(allBenchmarks, args.toList())
        if (selectedBenchmarks.isEmpty()) {
            val available = allBenchmarks.joinToString { it.className }
            error("No benchmarks matched: ${args.joinToString()}. Available benchmarks: $available")
        }

        prepareReportDir(context)
        writeRunMetadata(context, selectedBenchmarks)

        for (benchmark in selectedBenchmarks) {
            println("Benchmark: ${benchmark.className} [${benchmark.methods.joinToString()}]")
            runBenchmark(context, benchmark)
        }
    }

    private fun discoverBenchmarks(context: ChronicleBenchmarkContext): List<ChronicleBenchmarkDescriptor> {
        val classDirs = context.classDirs
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

    private fun toBenchmarkDescriptor(className: String): ChronicleBenchmarkDescriptor? {
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
            ChronicleBenchmarkDescriptor(className, methods.sorted())
        }.getOrNull()
    }

    private fun selectBenchmarks(
        allBenchmarks: List<ChronicleBenchmarkDescriptor>,
        patterns: List<String>,
    ): List<ChronicleBenchmarkDescriptor> {
        if (patterns.isEmpty()) return allBenchmarks

        return allBenchmarks.mapNotNull { benchmark ->
            val simpleName = benchmark.className.substringAfterLast('.')
            val selectedMethods = benchmark.methods.filter { method ->
                val candidates = listOf(
                    benchmark.className,
                    simpleName,
                    "${benchmark.className}.$method",
                    "$simpleName.$method",
                )
                patterns.any { pattern -> candidates.any { candidate -> GlobMatcher.matches(candidate, pattern) } }
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

    private fun prepareReportDir(context: ChronicleBenchmarkContext) {
        val reportDir = context.reportDir
        Files.createDirectories(reportDir)
        Files.list(reportDir).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".json") }
                .forEach { Files.deleteIfExists(it) }
        }
    }

    private fun writeRunMetadata(context: ChronicleBenchmarkContext, benchmarks: List<ChronicleBenchmarkDescriptor>) {
        val reportDir = context.reportDir
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

    private fun runBenchmark(context: ChronicleBenchmarkContext, benchmark: ChronicleBenchmarkDescriptor) {
        val reportDir = context.reportDir
        val resultFile = reportDir.resolve("${benchmark.className}.json").toFile()
        val options = OptionsBuilder()
            .apply {
                benchmark.methods.forEach { method -> include("^${Pattern.quote(benchmark.className)}\\.${Pattern.quote(method)}$") }
                context.profiles.forEach { profiler -> addProfiler(profiler) }
                context.params.forEach { (k, v) -> param(k, v) }
            }
            .result(resultFile.absolutePath)
            .resultFormat(ResultFormatType.JSON)
            .build()
        Runner(options).run()
    }
}

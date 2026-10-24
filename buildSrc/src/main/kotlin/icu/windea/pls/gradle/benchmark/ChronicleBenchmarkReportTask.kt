@file:Suppress("unused")

package icu.windea.pls.gradle.benchmark

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 从 JMH JSON 结果生成带时间戳的 Markdown/TEXT 报告。
 */
abstract class ChronicleBenchmarkReportTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:Internal
    abstract val outputBaseDir: DirectoryProperty

    @get:Input
    abstract val jmhVersion: Property<String>

    @get:Input
    abstract val gradleVersion: Property<String>

    @get:Input
    abstract val command: Property<String>

    @TaskAction
    fun generateReport() {
        val inputPath = inputDir.get().asFile.toPath()
        val jsonFiles = inputPath.toFile().listFiles { file ->
            file.isFile && file.extension == "json" && file.name != "run.json"
        }?.sortedBy { it.name }.orEmpty()
        if (jsonFiles.isEmpty()) {
            throw GradleException("No benchmark JSON results found in $inputPath. Run `./gradlew benchmark` first.")
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val outputDir = outputBaseDir.get().dir(timestamp).asFile
        outputDir.mkdirs()
        jsonFiles.forEach { it.copyTo(File(outputDir, it.name), overwrite = true) }
        val runMetadata = inputPath.resolve("run.json").toFile()
        if (runMetadata.exists()) runMetadata.copyTo(File(outputDir, runMetadata.name), overwrite = true)

        val rows = BenchmarkReport.readRows(jsonFiles)
        val runInfo = BenchmarkReport.readRunInfo(runMetadata)
        File(outputDir, "summary.md").writeText(
            BenchmarkReport.renderSummary(rows, timestamp, jmhVersion.get())
        )
        rows.groupBy { it.className }.forEach { (className, classRows) ->
            File(outputDir, "${className.substringAfterLast('.')}.txt").writeText(
                BenchmarkReport.renderClassReport(className, classRows)
            )
        }
        File(outputDir, "metadata.md").writeText(
            BenchmarkReport.renderMetadata(
                timestamp = timestamp,
                runInfo = runInfo,
                jmhVersion = jmhVersion.get(),
                gradleVersion = gradleVersion.get(),
                command = command.get(),
                gitCommit = exec("git", "rev-parse", "--short", "HEAD"),
                gitBranch = exec("git", "rev-parse", "--abbrev-ref", "HEAD"),
                gitDirty = exec("git", "status", "--porcelain")?.let { if (it.isBlank()) "false" else "true" },
            )
        )
        logger.lifecycle("Benchmark report: ${outputDir.absolutePath}")
    }

    private fun exec(vararg command: String): String? = runCatching {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) output else null
    }.getOrNull()
}

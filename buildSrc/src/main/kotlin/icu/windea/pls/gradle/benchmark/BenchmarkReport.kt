@file:Suppress("unused")

package icu.windea.pls.gradle.benchmark

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import java.io.File
import java.util.Locale

/**
 * JMH JSON 结果解析与 Markdown/TEXT 渲染。
 */
object BenchmarkReport {
    data class BenchmarkRow(
        val benchmark: String,
        val params: String,
        val mode: String,
        val score: Double,
        val error: Double,
        val unit: String,
        val forks: Int,
    ) {
        val className: String get() = benchmark.substringBeforeLast('.')
    }

    fun readRows(jsonFiles: List<File>): List<BenchmarkRow> {
        val rows = mutableListOf<BenchmarkRow>()
        for (file in jsonFiles) {
            val root = runCatching { Json.parseToJsonElement(file.readText()) }.getOrNull() as? JsonArray ?: continue
            for (element in root) {
                val map = element as? JsonObject ?: continue
                val benchmark = map["benchmark"]?.asString() ?: continue
                val primaryMetric = map["primaryMetric"] as? JsonObject ?: continue
                val params = (map["params"] as? JsonObject)
                    ?.entries
                    ?.joinToString(", ") { (key, value) -> "$key=${value.asString() ?: value}" }
                    .orEmpty()
                rows.add(
                    BenchmarkRow(
                        benchmark = benchmark,
                        params = params,
                        mode = map["mode"]?.asString().orEmpty(),
                        score = (primaryMetric["score"] as? JsonPrimitive)?.doubleOrNull ?: Double.NaN,
                        error = (primaryMetric["scoreError"] as? JsonPrimitive)?.doubleOrNull ?: Double.NaN,
                        unit = primaryMetric["scoreUnit"]?.asString().orEmpty(),
                        forks = (map["forks"] as? JsonPrimitive)?.intOrNull ?: 0,
                    )
                )
            }
        }
        return rows.sortedBy { it.benchmark }
    }

    fun readRunInfo(file: File): JsonObject? {
        if (!file.exists()) return null
        return runCatching { Json.parseToJsonElement(file.readText()) as? JsonObject }.getOrNull()
    }

    fun renderSummary(rows: List<BenchmarkRow>, timestamp: String, jmhVersion: String): String = buildString {
        appendLine("# Benchmark Report")
        appendLine()
        appendLine("- Timestamp: $timestamp")
        appendLine("- JMH version: $jmhVersion")
        appendLine()
        appendTable(rows)
    }

    fun renderClassReport(className: String, rows: List<BenchmarkRow>): String = buildString {
        appendLine("# $className")
        appendLine()
        appendTable(rows)
    }

    fun renderMetadata(
        timestamp: String,
        runInfo: JsonObject?,
        jmhVersion: String,
        gradleVersion: String,
        command: String,
        gitCommit: String?,
        gitBranch: String?,
        gitDirty: String?,
    ): String {
        fun info(key: String, fallback: String): String =
            runInfo?.get(key)?.asString() ?: fallback

        return buildString {
            appendLine("# Benchmark Metadata")
            appendLine()
            appendLine("- Timestamp: $timestamp")
            appendLine("- JMH version: ${info("jmhVersion", jmhVersion)}")
            appendLine("- Gradle version: $gradleVersion")
            appendLine("- Java: ${info("javaVersion", System.getProperty("java.version"))} (${info("javaVendor", System.getProperty("java.vendor"))})")
            appendLine(
                "- OS: ${info("osName", System.getProperty("os.name"))} " +
                    "${info("osVersion", System.getProperty("os.version"))} " +
                    "(${info("osArch", System.getProperty("os.arch"))})",
            )
            appendLine("- Processors: ${info("processors", Runtime.getRuntime().availableProcessors().toString())}")
            appendLine("- Git commit: ${gitCommit ?: "unknown"}")
            appendLine("- Git branch: ${gitBranch ?: "unknown"}")
            appendLine("- Git dirty: ${gitDirty ?: "unknown"}")
            appendLine("- Command: $command")
            val benchmarkNames = (runInfo?.get("benchmarks") as? JsonArray)
                ?.mapNotNull { (it as? JsonObject)?.get("className")?.asString() }
                ?.joinToString(", ")
                .orEmpty()
            appendLine("- Benchmarks: $benchmarkNames")
        }
    }

    private fun StringBuilder.appendTable(rows: List<BenchmarkRow>) {
        appendLine("| Benchmark | Params | Mode | Score | Error | Unit | Forks |")
        appendLine("|---|---|---|---:|---:|---|---:|")
        for (row in rows) {
            append("| ${row.benchmark.escapeTable()} | ${row.params.escapeTable()} | ${row.mode.escapeTable()} | ")
            append("${formatNumber(row.score)} | ${formatNumber(row.error)} | ${row.unit.escapeTable()} | ${row.forks} |")
            appendLine()
        }
    }

    private fun JsonElement.asString(): String? = (this as? JsonPrimitive)?.contentOrNull

    private fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        return String.format(Locale.ROOT, "%.3f", value)
    }

    private fun String.escapeTable(): String = replace("|", "\\|")
}

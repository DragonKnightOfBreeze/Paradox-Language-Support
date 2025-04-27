package icu.windea.pls.integration.tiger

import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import com.google.gson.Gson

/**
 * 调用 tiger 校验工具并解析其 JSON 输出。
 * @param tigerPath tiger 可执行文件路径
 */
object TigerChecker {
    private val logger = Logger.getInstance(TigerChecker::class.java)
    private val gson = Gson()

    data class TigerLocation(
        val column: Int?,
        val from: String?,
        val fullpath: String?,
        val length: Int?,
        val line: String?,
        val linenr: Int?,
        val path: String?,
        val tag: String?
    )

    data class TigerResult(
        val confidence: String?,
        val info: String?,
        val key: String?,
        val locations: List<TigerLocation>?,
        val message: String?,
        val severity: String?
    )

    /**
     * 执行 tiger 校验。
     * @param modPath 模组目录
     * @param tigerPath tiger 可执行文件路径
     * @param extraArgs 其他参数
     * @return 校验结果列表
     */
    fun check(modPath: String, tigerPath: String, extraArgs: List<String> = emptyList()): List<TigerResult> {
        val args = mutableListOf(tigerPath, "--json")
        args.addAll(extraArgs)
        args.add(modPath)
        try {
            val pb = ProcessBuilder(args)
            pb.redirectErrorStream(true)
            pb.directory(File(modPath))
            val process = pb.start()
            val output = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).readText()
            val exitCode = process.waitFor()
            if(exitCode != 0) {
                logger.warn("Tiger exited with code $exitCode. Output: $output")
            }
            return gson.fromJson(output, Array<TigerResult>::class.java)?.toList() ?: emptyList()
        } catch(e: Exception) {
            logger.error("Failed to run Tiger checker", e)
            return emptyList()
        }
    }
}

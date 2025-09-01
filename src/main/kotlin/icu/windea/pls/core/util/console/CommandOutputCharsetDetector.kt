package icu.windea.pls.core.util.console

import java.nio.charset.Charset

/**
 * 命令输出编码探测器。
 *
 * - CMD：通过执行 `cmd /c "echo 中文"`，以候选编码解读输出，命中即为该编码，否则回退 UTF-8。
 * - PowerShell/Shell：固定采用 UTF-8。
 */
object CommandOutputCharsetDetector {
    /** 根据命令类型推断标准输出所使用的编码。 */
    fun detect(commandType: CommandType): Charset {
        return when (commandType) {
            CommandType.CMD -> charsetForCmd
            CommandType.POWER_SHELL -> Charsets.UTF_8
            CommandType.SHELL -> Charsets.UTF_8
        }
    }

    /** 待检测的候选编码列表（顺序敏感）。 */
    private val charsetsToDetect = listOf(Charsets.UTF_8, Charset.forName("GBK"))

    /** 针对 CMD 的编码探测结果（惰性求值且缓存）。 */
    private val charsetForCmd by lazy {
        val process = ProcessBuilder(listOf("cmd", "/c", "echo 中文")).start()
        val bytes = process.inputStream.readBytes()
        charsetsToDetect.find { bytes.toString(it).trim() == "中文" } ?: Charsets.UTF_8
    }
}

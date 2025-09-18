package icu.windea.pls.core.util.console

import java.nio.charset.Charset

/**
 * 命令输出编码探测器。
 *
 * - 对 Shell/PowerShell 固定为 UTF-8；
 * - 对 CMD 通过执行 `cmd /c "echo 中文"`，在候选集中匹配“中文”的正确编码；
 * - 若未匹配成功则回退到 UTF-8。
 */
object CommandOutputCharsetDetector {
    /** 根据命令类型 [commandType] 返回用于读取输出流的字符集。*/
    fun detect(commandType: CommandType): Charset {
        return when (commandType) {
            CommandType.CMD -> charsetForCmd
            CommandType.POWER_SHELL -> Charsets.UTF_8
            CommandType.SHELL -> Charsets.UTF_8
        }
    }

    private val charsetsToDetect = listOf(Charsets.UTF_8, Charset.forName("GBK"))

    // 对 CMD 进行一次性探测并缓存结果
    private val charsetForCmd by lazy {
        val process = ProcessBuilder(listOf("cmd", "/c", "echo 中文")).start()
        val bytes = process.inputStream.readBytes()
        charsetsToDetect.find { bytes.toString(it).trim() == "中文" } ?: Charsets.UTF_8
    }
}

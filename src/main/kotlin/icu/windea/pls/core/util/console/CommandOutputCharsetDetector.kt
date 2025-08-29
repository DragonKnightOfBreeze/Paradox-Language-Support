package icu.windea.pls.core.util.console

import java.nio.charset.Charset

object CommandOutputCharsetDetector {
    fun detect(commandType: CommandType): Charset {
        return when (commandType) {
            CommandType.CMD -> charsetForCmd
            CommandType.POWER_SHELL -> Charsets.UTF_8
            CommandType.SHELL -> Charsets.UTF_8
        }
    }

    private val charsetsToDetect = listOf(Charsets.UTF_8, Charset.forName("GBK"))

    private val charsetForCmd by lazy {
        val process = ProcessBuilder(listOf("cmd", "/c", "echo 中文")).start()
        val bytes = process.inputStream.readBytes()
        charsetsToDetect.find { bytes.toString(it).trim() == "中文" } ?: Charsets.UTF_8
    }
}

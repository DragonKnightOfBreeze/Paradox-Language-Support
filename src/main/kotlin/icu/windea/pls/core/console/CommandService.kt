package icu.windea.pls.core.console

import com.intellij.util.system.OS
import java.nio.charset.Charset

object CommandService {
    private val outputCharsetCandidates = listOf(Charsets.UTF_8, Charset.forName("GBK"))

    private val outputCharsetForCmd by lazy {
        val process = ProcessBuilder(listOf("cmd", "/c", "echo 中文")).start()
        val bytes = process.inputStream.readBytes()
        outputCharsetCandidates.find { bytes.toString(it).trim() == "中文" } ?: Charsets.UTF_8
    }

    /**
     * 根据指定的命令类型 [commandType]，得到最终使用的适用于当前操作系统的命令类型。
     */
    fun getCommandTypeForOs(commandType: CommandType = CommandType.AUTO): CommandType {
        return when (commandType) {
            CommandType.AUTO -> if (OS.CURRENT == OS.Windows) CommandType.POWER_SHELL else CommandType.SHELL
            CommandType.CMD -> commandType.also { if (OS.CURRENT != OS.Windows) throw UnsupportedOperationException() }
            CommandType.POWER_SHELL -> commandType.also { if (OS.CURRENT != OS.Windows) throw UnsupportedOperationException() }
            CommandType.SHELL -> commandType // this is allowed and maybe available
            CommandType.NONE -> commandType
        }
    }

    /**
     * 根据指定的命令类型 [commandType]，得到最终使用的命令列表。
     */
    fun getCommands(command: String, commandType: CommandType = CommandType.AUTO): List<String> {
        return when (commandType) {
            CommandType.AUTO -> getCommands(command, getCommandTypeForOs(commandType))
            CommandType.SHELL -> listOf("/bin/sh", "-c", command)
            CommandType.CMD -> listOf("cmd", "/c", command)
            CommandType.POWER_SHELL -> listOf("powershell", "-Command", "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; $command")
            CommandType.NONE -> listOf(command)
        }
    }

    /**
     * 尝试检测并得到得到指定的命令类型 [commandType] 的输出流的字符集。
     */
    fun getOutputCharset(commandType: CommandType = CommandType.AUTO): Charset {
        return when (commandType) {
            CommandType.AUTO -> getOutputCharset(getCommandTypeForOs(commandType))
            CommandType.CMD -> outputCharsetForCmd
            CommandType.POWER_SHELL -> Charsets.UTF_8
            CommandType.SHELL -> Charsets.UTF_8
            CommandType.NONE -> Charsets.UTF_8
        }
    }
}

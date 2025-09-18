package icu.windea.pls.core.util.console

import com.intellij.openapi.diagnostic.logger
import icu.windea.pls.core.util.OS
import io.ktor.utils.io.charsets.Charset
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets

/**
 * 外部命令执行器。
 *
 * - 支持设定环境变量 [environment]、工作目录 [directory]、超时 [timeout]；
 * - Windows 下默认使用 [CommandType.POWER_SHELL] 并强制 UTF-8 输出编码；
 * - 非 Windows 下设置 `LANG/LC_ALL=en_US.UTF-8` 以稳定输出编码；
 * - 执行失败时抛出 [CommandExecutionException]，错误信息来自标准错误输出。
 */
class CommandExecutor(
    val environment: Map<String, String> = emptyMap(),
    val directory: File? = null,
    val timeout: Long? = null
) {
    companion object {
        private val logger = logger<CommandExecutor>()
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    /** 直接执行完整的命令数组 [commands] 并以 UTF-8 解析输出。*/
    fun execute(commands: List<String>): String {
        logger.info("Executing commands: $commands")
        return doExecute(commands, Charsets.UTF_8)
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    /**
     * 以命令行字符串 [command] 执行。
     *
     * - [commandType] 未指定时按当前 OS 选择合适的 Shell；
     * - 输出编码由 [CommandOutputCharsetDetector] 判定。
     */
    fun execute(command: String, commandType: CommandType?): String {
        logger.info("Executing command: $command")
        val commandTypeToUse = getCommandTypeToUse(commandType)
        val commands = getCommands(command, commandTypeToUse)
        val outputCharset = CommandOutputCharsetDetector.detect(commandTypeToUse)
        return doExecute(commands, outputCharset)
    }

    /** 实际执行 [commands] 并按 [outputCharset] 读取输出，处理超时与错误流。*/
    private fun doExecute(commands: List<String>, outputCharset: Charset): String {
        val processBuilder = ProcessBuilder(commands)
        val env = processBuilder.environment()
        env.putAll(environment)
        if (OS.value != OS.Windows) {
            env.put("LANG", "en_US.UTF-8")
            env.put("LC_ALL", "en_US.UTF-8")
        }
        processBuilder.directory(directory)
        val process = processBuilder.start()
        if (timeout == null) {
            process.waitFor()
        } else {
            process.waitFor(timeout, TimeUnit.MILLISECONDS)
        }
        val result = process.inputStream.bufferedReader(outputCharset).readText().trim()
        if (result.isNotEmpty()) {
            logger.info("Command result: $result")
        } else {
            logger.info("Done.")
        }
        if (result.isNotEmpty() || process.exitValue() == 0) return result
        val errorResult = process.errorStream.bufferedReader(outputCharset).readText().trim()
        logger.info("Command error result: $errorResult")
        throw CommandExecutionException(errorResult)
    }

    /** 根据 OS 与入参确定实际使用的命令类型。Windows 下仅允许 CMD/PowerShell。*/
    private fun getCommandTypeToUse(commandType: CommandType?): CommandType {
        if (commandType == CommandType.CMD || commandType == CommandType.POWER_SHELL) {
            if (OS.value != OS.Windows) throw UnsupportedOperationException()
        }
        if (commandType != null) return commandType
        return when (OS.value) {
            OS.Windows -> CommandType.POWER_SHELL
            OS.Linux -> CommandType.SHELL
        }
    }

    /** 将命令行字符串转换为实际的可执行命令数组。*/
    private fun getCommands(command: String, commandType: CommandType): List<String> {
        return when (commandType) {
            CommandType.SHELL -> listOf("/bin/sh", "-c", command)
            CommandType.CMD -> listOf("cmd", "/c", command)
            CommandType.POWER_SHELL -> listOf("powershell", "-Command", "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; $command")
        }
    }
}

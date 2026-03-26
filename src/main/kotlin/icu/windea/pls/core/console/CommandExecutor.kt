package icu.windea.pls.core.console

import com.intellij.openapi.diagnostic.logger
import com.intellij.util.system.OS
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

    /** 直接执行完整的命令数组 [commands] 并以 UTF-8 解析输出。 */
    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(commands: List<String>): String {
        logger.info("Executing commands: $commands")
        return doExecute(commands, Charsets.UTF_8)
    }

    /**
     * 以命令行字符串 [command] 执行。
     *
     * - [commandType] 未指定时按当前 OS 选择合适的 Shell；
     * - 输出编码由 [CommandService] 判定。
     */
    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(command: String, commandType: CommandType = CommandType.AUTO): String {
        logger.info("Executing command: $command")
        val commands = CommandService.getCommands(command, commandType)
        val outputCharset = CommandService.getOutputCharset(commandType)
        return doExecute(commands, outputCharset)
    }

    private fun doExecute(commands: List<String>, outputCharset: Charset): String {
        val processBuilder = ProcessBuilder(commands)
        val env = processBuilder.environment()
        env.putAll(environment)
        if (OS.CURRENT != OS.Windows) {
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
}

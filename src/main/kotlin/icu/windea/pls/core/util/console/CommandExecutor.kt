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
 * - 自动根据平台与 [CommandType] 选择合适的启动命令与输出编码。
 * - 支持注入环境变量、工作目录与超时时间。
 * - 当进程返回非 0 且无标准输出时，抛出 [CommandExecutionException]。
 */
class CommandExecutor(
    /** 追加到进程环境变量的键值对。 */
    val environment: Map<String, String> = emptyMap(),
    /** 进程工作目录。 */
    val directory: File? = null,
    /** 等待进程结束的超时毫秒数；为 null 表示无限等待。 */
    val timeout: Long? = null
) {
    companion object {
        private val logger = logger<CommandExecutor>()
    }

    /**
     * 直接执行完整命令列表。
     *
     * @throws IOException 进程创建或 I/O 失败
     * @throws InterruptedException 等待被中断
     * @throws CommandExecutionException 命令执行失败
     */
    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(commands: List<String>): String {
        logger.info("Executing commands: $commands")
        return doExecute(commands, Charsets.UTF_8)
    }

    /**
     * 执行一条命令字符串。
     * 会依据 [commandType]（为空则按平台推断）封装成底层实际命令（如 `powershell -Command ...`）。
     *
     * @throws IOException 进程创建或 I/O 失败
     * @throws InterruptedException 等待被中断
     * @throws CommandExecutionException 命令执行失败
     */
    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(command: String, commandType: CommandType?): String {
        logger.info("Executing command: $command")
        val commandTypeToUse = getCommandTypeToUse(commandType)
        val commands = getCommands(command, commandTypeToUse)
        val outputCharset = CommandOutputCharsetDetector.detect(commandTypeToUse)
        return doExecute(commands, outputCharset)
    }

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

    private fun getCommands(command: String, commandType: CommandType): List<String> {
        return when (commandType) {
            CommandType.SHELL -> listOf("/bin/sh", "-c", command)
            CommandType.CMD -> listOf("cmd", "/c", command)
            CommandType.POWER_SHELL -> listOf("powershell", "-Command", "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; $command")
        }
    }
}

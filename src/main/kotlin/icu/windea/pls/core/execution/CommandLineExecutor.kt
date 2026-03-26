package icu.windea.pls.core.execution

import com.intellij.openapi.diagnostic.logger
import com.intellij.util.system.OS
import io.ktor.utils.io.charsets.Charset
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets

/**
 * 外部命令的执行器。
 *
 * @param environment 额外环境变量。
 * @param workDirectory 工作目录。
 * @param timeout 超时（毫秒）。
 */
class CommandLineExecutor(
    val environment: Map<String, String> = emptyMap(),
    val workDirectory: File? = null,
    val timeout: Long? = null
) {
    companion object {
        private val logger = logger<CommandLineExecutor>()
    }

    /**
     * 执行外部命令数组。
     *
     * @param commands 命令数组，如 ["git", "status"]。
     * @return 标准输出文本（已按编码解码并去除首尾空白）。
     *
     * @throws IOException 启动进程失败时抛出。
     * @throws InterruptedException 进程被中断。
     * @throws CommandLineExecutionException 退出码非 0 且无标准输出，或有错误输出时抛出。
     */
    @Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
    fun execute(commands: List<String>): String {
        logger.info("Executing commands: $commands")
        return doExecute(commands, Charsets.UTF_8)
    }

    /**
     * 执行外部命令，并指定命令类型。
     *
     * @param command 命令，如 "git status"。
     * @param commandType 命令类型。
     * @return 标准输出文本（已按编码解码并去除首尾空白）。
     *
     * @throws IOException 启动进程失败时抛出。
     * @throws InterruptedException 进程被中断。
     * @throws CommandLineExecutionException 退出码非 0 且无标准输出，或有错误输出时抛出。
     */
    @Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
    fun execute(command: String, commandType: CommandType = CommandType.AUTO): String {
        logger.info("Executing command: $command")
        val commands = CommandLineService.getCommands(command, commandType)
        val outputCharset = CommandLineService.getOutputCharset(commandType)
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
        processBuilder.directory(workDirectory)
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
        throw CommandLineExecutionException(errorResult)
    }
}

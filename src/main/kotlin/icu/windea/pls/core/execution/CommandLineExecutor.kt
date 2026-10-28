@file:Suppress("unused", "DEPRECATION")

package icu.windea.pls.core.execution

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.milliseconds

/**
 * 外部命令执行器。
 *
 * ## 弃用声明
 *
 * 自插件版本 3.0.5 开始，应改为使用 [ProgressExecutor]，以取得更好的兼容性和性能。
 * 目前暂时保留。
 *
 * @param environment 额外环境变量。
 * @param workDirectory 工作目录。
 * @param timeout 超时（毫秒）。
 */
@Deprecated("Use `ProgressExecutor` instead.")
class CommandLineExecutor(
    val environment: Map<String, String> = emptyMap(),
    val workDirectory: File? = null,
    val timeout: Long? = null
) {
    /**
     * 执行外部命令数组。
     *
     * @param commands 命令数组，如 ["git", "status"]。
     * @return 标准输出文本（已按编码解码并去除首尾空白）。
     *
     * @throws IOException 启动进程失败时抛出。
     * @throws InterruptedException 进程被中断。
     * @throws CommandLineExecutionException 退出码非 0 且无标准输出时抛出。
     */
    @Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
    fun execute(commands: List<String>): String {
        val commandLine = createCommandLine(commands, Charsets.UTF_8)
        return doExecute(commandLine)
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
     * @throws CommandLineExecutionException 退出码非 0 且无标准输出时抛出。
     */
    @Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
    fun execute(command: String, commandType: CommandType = CommandType.AUTO): String {
        val commands = CommandLineService.getCommands(command, commandType)
        val outputCharset = CommandLineService.getOutputCharset(commandType)
        val commandLine = createCommandLine(commands, outputCharset)
        return doExecute(commandLine)
    }

    private fun createCommandLine(commands: List<String>, charset: Charset): GeneralCommandLine {
        return GeneralCommandLine(commands)
            .withEnvironment(environment)
            .withWorkingDirectory(workDirectory?.toPath())
            .withCharset(charset)
    }

    private fun doExecute(commandLine: GeneralCommandLine): String {
        if (!SystemInfo.isWindows) {
            commandLine.withEnvironment("LANG", "en_US.UTF-8")
            commandLine.withEnvironment("LC_ALL", "en_US.UTF-8")
        }
        val executor = ProgressExecutor(commandLine)
        timeout?.let { executor.withTimeout(it.milliseconds) }
        val output = executor.execute()
        val result = output.stdout.trim()
        if (result.isNotEmpty() || output.exitCode == 0) return result
        val errorResult = output.stderr.trim()
        throw CommandLineExecutionException(errorResult)
    }
}

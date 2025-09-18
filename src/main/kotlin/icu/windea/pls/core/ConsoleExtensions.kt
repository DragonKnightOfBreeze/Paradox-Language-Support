package icu.windea.pls.core

import icu.windea.pls.core.util.console.CommandExecutionException
import icu.windea.pls.core.util.console.CommandExecutor
import icu.windea.pls.core.util.console.CommandType
import java.io.File
import java.io.IOException

/**
 * 执行外部命令行字符串。
 *
 * @param command 命令行，如 "git status"。
 * @param commandType 命令类型，未指定时按当前 OS 选择（Windows 默认 PowerShell）。
 * @param environment 额外环境变量。
 * @param workDirectory 工作目录。
 * @param timeout 超时（毫秒）。
 * @return 标准输出文本（已按编码解码并去除首尾空白）。
 * @throws IOException 启动进程失败时抛出。
 * @throws InterruptedException 进程被中断。
 * @throws CommandExecutionException 退出码非 0 且无标准输出，或有错误输出时抛出。
 */
@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    command: String,
    commandType: CommandType? = null,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(command, commandType)
}

/**
 * 执行外部命令数组（已拆分的命令与参数）。
 *
 * @param commands 命令数组，如 ["git", "status" ]。
 * @param environment 额外环境变量。
 * @param workDirectory 工作目录。
 * @param timeout 超时（毫秒）。
 * @return 标准输出文本。
 * @throws IOException 启动进程失败时抛出。
 * @throws InterruptedException 进程被中断。
 * @throws CommandExecutionException 退出码非 0 且无标准输出，或有错误输出时抛出。
 */
@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    commands: List<String>,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(commands)
}

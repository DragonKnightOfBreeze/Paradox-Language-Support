package icu.windea.pls.core

import icu.windea.pls.core.util.console.CommandExecutionException
import icu.windea.pls.core.util.console.CommandExecutor
import icu.windea.pls.core.util.console.CommandType
import java.io.File
import java.io.IOException

/** 执行一条命令并返回标准输出。可指定命令类型、环境变量、工作目录与超时。 */
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

/** 执行多段命令（顺序执行）并返回标准输出。可指定环境变量、工作目录与超时。 */
@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    commands: List<String>,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(commands)
}

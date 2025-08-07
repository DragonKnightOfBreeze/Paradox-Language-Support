package icu.windea.pls.core

import icu.windea.pls.core.util.console.*
import java.io.*

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

@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    commands: List<String>,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(commands)
}

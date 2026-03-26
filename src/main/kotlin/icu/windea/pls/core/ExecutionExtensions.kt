@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.execution.CommandLineExecutionException
import icu.windea.pls.core.execution.CommandLineExecutor
import icu.windea.pls.core.execution.CommandType
import java.io.File
import java.io.IOException

/** @see CommandLineExecutor.execute */
@Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
inline fun executeCommandLine(
    command: String,
    commandType: CommandType = CommandType.AUTO,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandLineExecutor(environment, workDirectory, timeout).execute(command, commandType)
}

/** @see CommandLineExecutor.execute */
@Throws(IOException::class, InterruptedException::class, CommandLineExecutionException::class)
inline fun executeCommandLine(
    commands: List<String>,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandLineExecutor(environment, workDirectory, timeout).execute(commands)
}

package icu.windea.pls.core.console

import icu.windea.pls.core.*
import io.ktor.utils.io.charsets.Charset
import java.io.*
import java.util.concurrent.*
import kotlin.text.Charsets
import kotlin.text.isNotEmpty
import kotlin.text.trim

class CommandExecutor(
    val environment: Map<String, String> = emptyMap(),
    val directory: File? = null,
    val timeout: Long? = null
) {
    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(commands: List<String>): String {
        return doExecute(commands, Charsets.UTF_8)
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(command: String, commandType: CommandType?): String {
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
        if (result.isNotEmpty() || process.exitValue() == 0) return result
        val errorResult = process.errorStream.bufferedReader(outputCharset).readText().trim()
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

package icu.windea.pls.core.console

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.core.util.*
import io.ktor.utils.io.charsets.Charset
import java.io.*
import java.util.concurrent.*
import kotlin.text.Charsets

class CommandExecutor(
    val environment: Map<String, String> = emptyMap(),
    val directory: File? = null,
    val timeout: Long? = null
) {
    companion object {
        private val logger = logger<CommandExecutor>()
    }

    @Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
    fun execute(commands: List<String>): String {
        logger.info("Executing commands: $commands")
        return doExecute(commands, Charsets.UTF_8)
    }

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

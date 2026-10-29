package icu.windea.pls.integrations.lints.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.integrations.lints.LintToolSettings
import icu.windea.pls.integrations.lints.TigerLintRequest
import icu.windea.pls.integrations.lints.TigerLintResult
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

/**
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * 目前仅适用于模组目录。
 */
abstract class TigerLintToolProvider : CommandBasedLintToolProvider() {
    abstract val name: String
    abstract val forGameType: ParadoxGameType

    protected abstract val executablePathText: String?
    protected abstract val confPathText: String?

    final override fun isEnabled(): Boolean {
        return LintToolSettings.getInstance().enableTiger
    }

    final override fun isSupported(gameType: ParadoxGameType?): Boolean {
        return gameType == forGameType
    }

    final override fun getExecutablePath(): Path? {
        executablePathText?.toPathOrNull()?.let { return it }
        return null // by default null
    }

    final override fun validateExecutablePath(path: Path): Boolean {
        // NOTE 3.0.5 出于轻量化的目的，不再通过执行命令行来检测工具是否可用
        if (!path.nameWithoutExtension.equals(name, true)) return false
        return Files.isRegularFile(path)
    }

    fun getConfPath(): Path? {
        confPathText?.toPathOrNull()?.let { return it }
        return null // by default null
    }

    fun validateConfPath(path: Path): Boolean {
        if (!path.extension.equals("conf", true)) return false
        return Files.isRegularFile(path)
    }

    fun validateRootDirectory(request: TigerLintRequest): TigerLintResult {
        val commands = buildList {
            add(request.executablePath.toString())
            add("--json")
            if (request.confPath != null) {
                add("--config")
                add(request.confPath.toString())
            }
            if (request.gamePath != null) {
                add("--game")
                add(request.gamePath)
            }
            add(request.rootPath)
        }
        val commandLine = GeneralCommandLine(commands)
            .withWorkingDirectory(request.workingDirectory)
            .withCharset(Charsets.UTF_8)

        ProgressManager.checkCanceled()
        val output = ProcessExecutor(commandLine)
            .withTimeout(ExecutionTimeouts.veryLong)
            .execute()

        if (output.isCancelled) {
            ProgressManager.checkCanceled() // 如果的确被取消，则抛出 `ProcessCanceledException`
            val message = "Tiger command was cancelled." +
                "\nCommand: ${commandLine.commandLineString}"
            throw IllegalStateException(message)
        }
        if (output.isTimeout) {
            val message = "Tiger command timed out." +
                "\nCommand: ${commandLine.commandLineString}"
            throw IllegalStateException(message)
        }
        if (output.exitCode != 0) {
            val message = "Tiger command failed (exitCode=${output.exitCode})." +
                "\nCommand: ${commandLine.commandLineString}" +
                "\nError: ${output.stderr.trim()}"
            throw IllegalStateException(message)
        }

        request.outputPath.parent?.createDirectories()
        Files.writeString(request.outputPath, output.stdout, Charsets.UTF_8)
        return TigerLintResult.parse(name, request.outputPath.toFile()) // 如果无法解析 json，这里会直接报错
    }

    class Ck3 : TigerLintToolProvider() {
        override val name = "ck3-tiger"
        override val forGameType = ParadoxGameType.Ck3
        override val executablePathText get() = LintToolSettings.getInstance().ck3TigerPath?.trim()?.orNull()
        override val confPathText get() = LintToolSettings.getInstance().ck3TigerConfPath?.trim()?.orNull()
    }

    class Eu5 : TigerLintToolProvider() {
        override val name: String = "eu5-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Eu5
        override val executablePathText get() = LintToolSettings.getInstance().eu5TigerPath?.trim()?.orNull()
        override val confPathText get() = LintToolSettings.getInstance().eu5TigerConfPath?.trim()?.orNull()
    }

    class Hoi4 : TigerLintToolProvider() {
        override val name: String = "hoi4-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Hoi4
        override val executablePathText get() = LintToolSettings.getInstance().hoi4TigerPath?.trim()?.orNull()
        override val confPathText get() = LintToolSettings.getInstance().hoi4TigerConfPath?.trim()?.orNull()
    }

    class Ir : TigerLintToolProvider() {
        override val name: String = "imperator-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ir
        override val executablePathText get() = LintToolSettings.getInstance().irTigerPath?.trim()?.orNull()
        override val confPathText get() = LintToolSettings.getInstance().irTigerConfPath?.trim()?.orNull()
    }

    class Vic3 : TigerLintToolProvider() {
        override val name: String = "vic3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Vic3
        override val executablePathText get() = LintToolSettings.getInstance().vic3TigerPath?.trim()?.orNull()
        override val confPathText get() = LintToolSettings.getInstance().vic3TigerConfPath?.trim()?.orNull()
    }
}

package icu.windea.pls.integrations.lints.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.integrations.lints.TigerLintRequest
import icu.windea.pls.integrations.lints.TigerLintResult
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension

/**
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * 目前仅适用于模组目录。
 */
abstract class TigerLintToolProvider : CommandBasedLintToolProvider() {
    abstract val name: String
    abstract val forGameType: ParadoxGameType
    abstract val exePath: String?
    abstract val confPath: String?

    final override fun isEnabled(): Boolean {
        return ChronicleIntegrationsSettings.getInstance().state.lint.enableTiger
    }

    final override fun isSupported(gameType: ParadoxGameType?): Boolean {
        return gameType == forGameType
    }

    override fun isValid(): Boolean {
        val path = exePath?.trim()
        if (path.isNullOrEmpty()) return false
        return isValidExePath(path)
    }

    override fun isValidExePath(path: String): Boolean {
        val fullExePath = path.toPathOrNull() ?: return false
        if (fullExePath.nameWithoutExtension != name) return false
        return Files.isRegularFile(fullExePath)
    }

    fun validateRootDirectory(request: TigerLintRequest): TigerLintResult? {
        val commandLine = GeneralCommandLine(buildList {
            add(request.exePath.toString())
            add("--json")
            if (request.gamePath != null) {
                add("--game")
                add(request.gamePath)
            }
            if (request.configPath != null) {
                add("--config")
                add(request.configPath)
            }
            add(request.rootPath)
        }).withWorkingDirectory(request.workingDirectory).withCharset(Charsets.UTF_8)

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
        override val name: String = "ck3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ck3
        override val exePath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.ck3TigerPath
        override val confPath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.ck3TigerConfPath
    }

    class Eu5 : TigerLintToolProvider() {
        override val name: String = "eu5-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Eu5
        override val exePath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.eu5TigerPath
        override val confPath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.eu5TigerConfPath
    }

    class Hoi4 : TigerLintToolProvider() {
        override val name: String = "hoi4-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Hoi4
        override val exePath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.hoi4TigerPath
        override val confPath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.hoi4TigerConfPath
    }

    class Ir : TigerLintToolProvider() {
        override val name: String = "imperator-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ir
        override val exePath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.irTigerPath
        override val confPath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.irTigerConfPath
    }

    class Vic3 : TigerLintToolProvider() {
        override val name: String = "vic3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Vic3
        override val exePath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.vic3TigerPath
        override val confPath: String? get() = ChronicleIntegrationsSettings.getInstance().state.lint.vic3TigerConfPath
    }
}

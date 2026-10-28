package icu.windea.pls.integrations.lints.providers

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.base.settings.ChronicleProfilesSettings
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProgressExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toUuidString
import icu.windea.pls.integrations.lints.TigerLintResult
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Files
import kotlin.io.path.nameWithoutExtension
import kotlin.text.Charsets

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

    final override fun validateFile(file: VirtualFile): TigerLintResult? {
        return null // unsupported
    }

    final override fun validateRootDirectory(rootDirectory: VirtualFile): TigerLintResult? {
        return runCatchingCancelable { doValidateRootDirectory(rootDirectory) }
            .onFailure { thisLogger().warn(it) }.getOrElse { TigerLintResult(name, error = it) }
    }

    private fun doValidateRootDirectory(rootDirectory: VirtualFile): TigerLintResult? {
        val tigerPath = exePath?.trim() ?: return null
        val fullExePath = tigerPath.toPathOrNull() ?: return null
        if (fullExePath.nameWithoutExtension != name) return null

        val rootInfo = rootDirectory.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        val rootPath = rootFile.path
        val modSettings = ChronicleProfilesSettings.getInstance().state.modSettings.get(rootPath)
        val gamePath = modSettings?.finalGameDirectory?.orNull()
        val configPath = confPath?.orNull()

        // 必须指定输出的 json 文件，然后从中读取检查结果（之后不删除这个临时文件）
        val lintResultsPath = ChronicleDataPathService.getInstance().lintResultsPath
        val outputPath = lintResultsPath.resolve("$name-result@${rootDirectory.url.toUuidString()}.json")

        val commandLine = GeneralCommandLine(buildList {
            add(fullExePath.toString())
            add("--json")
            if (gamePath != null) {
                add("--game")
                add(gamePath)
            }
            if (configPath != null) {
                add("--config")
                add(configPath)
            }
            add(rootPath)
        }).withWorkingDirectory(fullExePath.parent).withCharset(Charsets.UTF_8)

        ProgressManager.checkCanceled()
        val output = ProgressExecutor(commandLine)
            .withTimeout(ExecutionTimeouts.veryLong)
            .execute()

        if (output.isCancelled) {
            ProgressManager.checkCanceled() // 如果的确被取消，则抛出 `ProcessCanceledException`
            throw IllegalStateException("Tiger command was cancelled.\nCommand: ${commandLine.commandLineString}")
        }
        if (output.isTimeout) {
            throw IllegalStateException("Tiger command timed out.\nCommand: ${commandLine.commandLineString}")
        }
        if (output.exitCode != 0) {
            throw IllegalStateException(
                "Tiger command failed (exitCode=${output.exitCode}).\n" +
                    "Command: ${commandLine.commandLineString}\n" +
                    "Error: ${output.stderr.trim()}"
            )
        }

        Files.writeString(outputPath, output.stdout, Charsets.UTF_8)
        return TigerLintResult.parse(name, outputPath.toFile()) // 如果无法解析 json，这里会直接报错
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

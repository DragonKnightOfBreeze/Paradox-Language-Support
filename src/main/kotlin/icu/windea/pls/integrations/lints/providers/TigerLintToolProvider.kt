package icu.windea.pls.integrations.lints.providers

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.executeCommandLine
import icu.windea.pls.core.orNull
import icu.windea.pls.core.quote
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toUuidString
import icu.windea.pls.integrations.lints.TigerLintResult
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.PlsPaths
import kotlin.io.path.createDirectories
import kotlin.io.path.name
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
        return PlsIntegrationsSettings.getInstance().state.lint.enableTiger
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
        return runCatchingCancelable { checkExePath(path) }.getOrDefault(false)
    }

    private fun checkExePath(path: String): Boolean {
        val fullExePath = path.toPath()
        if (fullExePath.nameWithoutExtension != name) return false
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name

        ProgressManager.checkCanceled() // 在执行命令前检查进度是否被取消
        val command = "./$exe --version"
        executeCommandLine(command, workDirectory = wd) // 尽可能地先转到工作目录，再执行可执行文件
        return true
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
        val fullExePath = tigerPath.toPath()
        if (fullExePath.nameWithoutExtension != name) return null
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name

        val rootInfo = rootDirectory.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        val rootPath = rootFile.path
        val modSettings = PlsProfilesSettings.getInstance().state.modSettings.get(rootPath)
        val argGamePath = modSettings?.finalGameDirectory?.orNull()?.quote('\'')
        val argConfPath = confPath?.orNull()?.quote('\'')
        val argPath = rootPath.quote('\'') // 这里应该都可以直接输入模组目录

        // 必须指定输出的 json 文件，然后从中读取检查结果（之后不删除这个临时文件）
        val lintResultsPath = PlsPaths.lintResults
        lintResultsPath.createDirectories()
        val outputPath = lintResultsPath.resolve("$name-result@${rootDirectory.url.toUuidString()}.json")
        val argOutputPath = outputPath.toString().quote('\'')

        ProgressManager.checkCanceled() // 在执行命令前检查进度是否被取消
        val command = buildString {
            append("./$exe --json")
            if (argGamePath != null) append(" --game ").append(argGamePath)
            if (argConfPath != null) append(" --config ").append(argConfPath)
            append(" ").append(argPath)
            append(" > ").append(argOutputPath)
        }
        executeCommandLine(command, workDirectory = wd) // 尽可能地先转到工作目录，再执行可执行文件

        return TigerLintResult.parse(name, outputPath.toFile()) // 如果无法解析 json，这里会直接报错
    }

    class Ck3 : TigerLintToolProvider() {
        override val name: String = "ck3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ck3
        override val exePath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.ck3TigerPath
        override val confPath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.ck3TigerConfPath
    }

    class Ir : TigerLintToolProvider() {
        override val name: String = "imperator-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ir
        override val exePath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.irTigerPath
        override val confPath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.irTigerConfPath
    }

    class Vic3 : TigerLintToolProvider() {
        override val name: String = "vic3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Vic3
        override val exePath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.vic3TigerPath
        override val confPath: String? get() = PlsIntegrationsSettings.getInstance().state.lint.vic3TigerConfPath
    }
}

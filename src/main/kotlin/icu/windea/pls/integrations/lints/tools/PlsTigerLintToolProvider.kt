package icu.windea.pls.integrations.lints.tools

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.orNull
import icu.windea.pls.core.quote
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toUuidString
import icu.windea.pls.integrations.lints.PlsTigerLintResult
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.PlsPathConstants
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * 目前仅适用于模组目录。
 */
abstract class PlsTigerLintToolProvider : PlsCommandBasedLintToolProvider() {
    abstract val name: String
    abstract val forGameType: ParadoxGameType
    abstract val exePath: String?
    abstract val confPath: String?

    final override fun isEnabled(): Boolean {
        return PlsFacade.getIntegrationsSettings().lint.enableTiger
    }

    final override fun isSupported(gameType: ParadoxGameType?): Boolean {
        return gameType == forGameType
    }

    final override fun isValid(): Boolean {
        val path = exePath?.trim()
        if (path.isNullOrEmpty()) return false
        return validatePath(path)
    }

    fun validatePath(path: String): Boolean {
        return runCatchingCancelable { doValidatePath(path) }.getOrDefault(false)
    }

    private fun doValidatePath(path: String): Boolean {
        val fullExePath = path.toPath()
        if (fullExePath.nameWithoutExtension != name) return false
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name

        val command = "./$exe --version"
        executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件
        return true
    }

    final override fun validateFile(file: VirtualFile): PlsTigerLintResult? {
        return null //unsupported
    }

    final override fun validateRootDirectory(rootDirectory: VirtualFile): PlsTigerLintResult? {
        return runCatchingCancelable { doValidateRootDirectory(rootDirectory) }
            .onFailure { thisLogger().warn(it) }.getOrElse { PlsTigerLintResult(name, error = it) }
    }

    private fun doValidateRootDirectory(rootDirectory: VirtualFile): PlsTigerLintResult? {
        val tigerPath = exePath?.trim() ?: return null
        val fullExePath = tigerPath.toPath()
        if (fullExePath.nameWithoutExtension != name) return null
        val wd = fullExePath.parent?.toFile()
        val exe = fullExePath.name

        val rootInfo = rootDirectory.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        val rootPath = rootFile.path
        val modSettings = PlsFacade.getProfilesSettings().modSettings.get(rootPath)
        val argGamePath = modSettings?.gameDirectory?.orNull()?.quote('\'')
        val argConfPath = confPath?.orNull()?.quote('\'')
        val argPath = rootPath.quote('\'') //这里应该都可以直接输入模组目录

        //必须指定输出的json文件，然后从中读取检查结果（之后不删除这个临时文件）
        val lintResultsPath = PlsPathConstants.lintResults
        lintResultsPath.createDirectories()
        val outputPath = lintResultsPath.resolve("$name-result@${rootDirectory.url.toUuidString()}.json")
        val argOutputPath = outputPath.toString().quote('\'')

        val command = buildString {
            append("./$exe --json")
            if (argGamePath != null) append(" --game ").append(argGamePath)
            if (argConfPath != null) append(" --config ").append(argConfPath)
            append(" ").append(argPath)
            append(" > ").append(argOutputPath)
        }
        executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件

        return PlsTigerLintResult.parse(name, outputPath.toFile()) //如果无法解析json，这里会直接报错
    }

    class Ck3 : PlsTigerLintToolProvider() {
        override val name: String = "ck3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ck3
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.ck3TigerPath
        override val confPath: String? get() = PlsFacade.getIntegrationsSettings().lint.ck3TigerConfPath
    }

    class Ir : PlsTigerLintToolProvider() {
        override val name: String = "imperator-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ir
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.irTigerPath
        override val confPath: String? get() = PlsFacade.getIntegrationsSettings().lint.irTigerConfPath
    }

    class Vic3 : PlsTigerLintToolProvider() {
        override val name: String = "vic3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Vic3
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.vic3TigerPath
        override val confPath: String? get() = PlsFacade.getIntegrationsSettings().lint.vic3TigerConfPath
    }
}

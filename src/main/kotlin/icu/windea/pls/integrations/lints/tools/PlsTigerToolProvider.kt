package icu.windea.pls.integrations.lints.tools

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import kotlin.io.path.*

/**
 * 参见：[Tiger](https://github.com/amtep/tiger)
 *
 * 仅适用于模组目录。
 */
abstract class PlsTigerToolProvider : PlsCommandBasedLintToolProvider() {
    abstract val name: String
    abstract val forGameType: ParadoxGameType
    abstract val exePath: String?

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
            .onFailure { thisLogger().warn(it) }.getOrThrow()
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
        val argGame = modSettings?.gameDirectory?.orNull()?.quote('\'')
        val argPath = rootPath.quote('\'') //这里应该都可以直接输入模组目录

        //TODO 2.0.0-dev 支持额外的参数
        val command = buildString {
            append("./$exe --json")
            if (argGame != null) append(" --game").append(argGame)
            //append(" --config").append(" some-conf-file.conf")
            append(" ").append(argPath)
        }
        val result = executeCommand(command, workDirectory = wd) //尽可能地先转到工作目录，再执行可执行文件

        return PlsTigerLintResult.parse(result) //如果无法解析json，这里会直接报错
    }

    class Ck3 : PlsTigerToolProvider() {
        override val name: String = "ck3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ck3
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.ck3TigerPath
    }

    class Ir : PlsTigerToolProvider() {
        override val name: String = "imperator-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Ir
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.irTigerPath
    }

    class Vic3 : PlsTigerToolProvider() {
        override val name: String = "vic3-tiger"
        override val forGameType: ParadoxGameType get() = ParadoxGameType.Vic3
        override val exePath: String? get() = PlsFacade.getIntegrationsSettings().lint.vic3TigerPath
    }
}

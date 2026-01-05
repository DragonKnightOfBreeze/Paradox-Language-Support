package icu.windea.pls.lang.util

import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.io.createDirectories
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.formatted
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.ParadoxEntryInfo
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.ParadoxScriptFileType
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Suppress("unused")
object ParadoxFileManager {
    private val logger = logger<ParadoxFileManager>()

    const val scriptedVariablesPath = "common/scripted_variables"

    fun getScriptedVariablesDirectory(contextFile: VirtualFile, createIfMissing: Boolean = true): VirtualFile? {
        val fileInfo = contextFile.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val entryPath = fileInfo.entryPath ?: return null
        val path = entryPath.resolve(scriptedVariablesPath)
        return PlsFileManager.findDirectory(path)
    }

    /**
     * 将输入路径视为相对于游戏的主要入口目录的路径，得到规范化后的绝对路径。
     */
    fun getPathInGameDirectory(path: String, gameType: ParadoxGameType): Path? {
        val gamePath = PlsPathService.getInstance().getSteamGamePath(gameType.id, gameType.title) ?: return null
        val mainEntryName = gameType.entryInfo.gameMain.firstOrNull()
        val mainEntryPath = if (mainEntryName != null) gamePath.resolve(mainEntryName) else gamePath
        val resultPath = mainEntryPath.resolve(path)
        return resultPath.formatted()
    }

    /**
     * 判断指定的文件能否引用另一个文件中的内容。
     *
     * 主要入口目录中的文件不能引用次要入口目录中的文件中的内容。
     *
     * @see ParadoxEntryInfo
     */
    fun canReference(file: VirtualFile?, otherFile: VirtualFile?): Boolean {
        val target = file?.fileInfo ?: return true
        val other = otherFile?.fileInfo ?: return true
        if (target.inMainEntry && !other.inMainEntry) return false
        return true
    }

    fun canOverrideFile(file: PsiFile, fileType: ParadoxFileGroup): Boolean {
        return when (fileType) {
            ParadoxFileGroup.Script -> true
            ParadoxFileGroup.Localisation -> true
            ParadoxFileGroup.Csv -> true
            ParadoxFileGroup.ModDescriptor -> false
            ParadoxFileGroup.Other -> ParadoxImageManager.isImageFile(file) // currently only accept generic images
        }
    }

    /**
     * 基于文件信息，判断指定的文件与另一个文件是否是等同的。
     */
    fun isEquivalentFile(file: VirtualFile, otherFile: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val otherFileInfo = otherFile.fileInfo ?: return false
        if (fileInfo != otherFileInfo) return false
        if (file.fileType != otherFile.fileType) return false // 这里的判断会更慢，因此在最后判断
        return true
    }

    /**
     * 基于文件信息，判断指定的文件与另一个文件是否是等同的。
     */
    fun isEquivalentFile(file: PsiFile, otherFile: PsiFile): Boolean {
        if (file.fileType != otherFile.fileType) return false
        val fileInfo = file.fileInfo ?: return false
        val otherFileInfo = otherFile.fileInfo ?: return false
        if (fileInfo != otherFileInfo) return false
        return true
    }

    fun isIgnoredFile(fileName: String): Boolean {
        return PlsSettings.getInstance().state.ignoredFileNameSet.contains(fileName)
    }

    fun isTestDataFile(file: VirtualFile): Boolean {
        if (!PlsFacade.isUnitTestMode()) return false
        val name = file.nameWithoutExtension
        return name.split('_', '.').any { it == "test" }
    }

    fun getFileType(fileType: ParadoxFileGroup): FileType? {
        return when (fileType) {
            ParadoxFileGroup.Script -> ParadoxScriptFileType
            ParadoxFileGroup.Localisation -> ParadoxLocalisationFileType
            ParadoxFileGroup.Csv -> ParadoxCsvFileType
            ParadoxFileGroup.ModDescriptor -> ParadoxScriptFileType
            else -> null
        }
    }

    fun createTempFile(file: VirtualFile, directoryPath: Path): VirtualFile? {
        try {
            directoryPath.createDirectories()
            val fileName = UUID.randomUUID().toString()
            val diffDirFile = directoryPath.toVirtualFile() ?: return null
            val tempFile = VfsUtil.copyFile(ParadoxFileManager, file, diffDirFile, fileName)
            ParadoxAnalysisInjector.injectFileInfo(tempFile, file.fileInfo)
            return tempFile
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }

    fun createTempFile(text: CharSequence, directoryPath: Path, fileInfo: ParadoxFileInfo): VirtualFile? {
        try {
            directoryPath.createDirectories()
            val fileName = UUID.randomUUID().toString()
            val path = directoryPath.resolve(fileName)
            Files.writeString(path, text)
            val tempFile = path.toVirtualFile() ?: return null
            ParadoxAnalysisInjector.injectFileInfo(tempFile, fileInfo)
            return tempFile
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }

    fun createLightFile(name: String, file: VirtualFile, project: Project): VirtualFile {
        // 为了兼容不同的 `lineSeparator`，这里不能直接使用 `document.charSequence`
        val text = file.toPsiFile(project)?.text ?: throw IllegalStateException()
        val lightFile = LightVirtualFile(name, text)
        ParadoxAnalysisInjector.injectFileInfo(lightFile, file.fileInfo)
        return lightFile
    }

    fun createLightFile(name: String, text: CharSequence): VirtualFile {
        val lightFile = LightVirtualFile(name, text)
        return lightFile
    }

    fun createLightFile(name: String, text: CharSequence, language: Language): VirtualFile {
        val lightFile = LightVirtualFile(name, language, text)
        return lightFile
    }

    fun createLightFile(name: String, text: CharSequence, fileInfo: ParadoxFileInfo): VirtualFile {
        val lightFile = LightVirtualFile(name, text)
        ParadoxAnalysisInjector.injectFileInfo(lightFile, fileInfo)
        return lightFile
    }
}

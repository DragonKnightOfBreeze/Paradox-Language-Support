@file:Suppress("unused")

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
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.findOption
import icu.windea.pls.config.config.getOptionValueOrValues
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.inMainEntry
import icu.windea.pls.script.ParadoxScriptFileType
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object ParadoxFileManager {
    object Keys : KeyRegistry() {
        val fileExtensions by createKey<Set<String>>(Keys)
    }

    private val logger = logger<ParadoxFileManager>()

    const val scriptedVariablesPath = "common/scripted_variables"

    fun getScriptedVariablesDirectory(contextFile: VirtualFile): VirtualFile? {
        val rootInfo = contextFile.fileInfo?.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val entryFile = rootInfo.entryFile
        val path = scriptedVariablesPath
        VfsUtil.createDirectoryIfMissing(entryFile, path)
        return entryFile.findFileByRelativePath(path)
    }

    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.fileExtensions) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }

    /**
     * 判断指定的文件能否引用另一个文件中的内容。
     *
     * 游戏目录下可以存在多个入口目录，
     * 插件认为模组目录以及游戏目录的主要入口目录（游戏目录或其game子目录）不能引用游戏目录的次要入口目录下的文件中的内容。
     */
    fun canReference(file: VirtualFile?, otherFile: VirtualFile?): Boolean {
        val target = file?.fileInfo ?: return true
        val other = otherFile?.fileInfo ?: return true
        if (target.inMainEntry() && !other.inMainEntry()) return false
        return true
    }

    /**
     * 基于文件信息，判断指定的文件与另一个文件是否是等同的。
     */
    fun isEquivalentFile(file: VirtualFile, otherFile: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val otherFileInfo = otherFile.fileInfo ?: return false
        if (fileInfo != otherFileInfo) return false
        if (file.fileType != otherFile.fileType) return false //这里的判断会更慢，因此在最后判断
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

    /**
     * 基于指定的虚拟文件创建一个临时文件。
     */
    @Deprecated("Use createLightFile()")
    fun createTempFile(file: VirtualFile, directoryPath: Path): VirtualFile? {
        try {
            directoryPath.createDirectories()
            val fileName = UUID.randomUUID().toString()
            val diffDirFile = VfsUtil.findFile(directoryPath, false) ?: return null
            val tempFile = VfsUtil.copyFile(ParadoxFileManager, file, diffDirFile, fileName)
            tempFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
            return tempFile
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }

    /**
     * 基于指定的文本和文件信息创建一个临时文件。
     */
    @Deprecated("Use createLightFile()")
    fun createTempFile(text: String, fileInfo: ParadoxFileInfo, directoryPath: Path): VirtualFile? {
        try {
            directoryPath.createDirectories()
            val fileName = UUID.randomUUID().toString()
            val path = directoryPath.resolve(fileName)
            Files.writeString(path, text)
            val tempFile = VfsUtil.findFile(path, true) ?: return null
            tempFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
            return tempFile
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }

    /**
     * 基于指定的虚拟文件创建一个内存中的临时文件。
     */
    fun createLightFile(name: String, file: VirtualFile, project: Project): VirtualFile {
        //为了兼容不同的lineSeparator，这里不能直接使用document.charSequence
        val text = file.toPsiFile(project)?.text ?: throw IllegalStateException()
        val lightFile = LightVirtualFile(name, text)
        lightFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
        return lightFile
    }

    /**
     * 基于指定的文本和文件信息创建一个内存中的临时文件。
     */
    fun createLightFile(name: String, text: CharSequence, fileInfo: ParadoxFileInfo): VirtualFile {
        val lightFile = LightVirtualFile(name, text)
        lightFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        return lightFile
    }
    /**
     * 基于指定的文本和文件信息创建一个内存中的临时文件。
     */
    fun createLightFile(name: String, text: CharSequence, language: Language): VirtualFile {
        val lightFile = LightVirtualFile(name, language, text)
        return lightFile
    }

    fun isIgnoredFile(fileName: String): Boolean {
        return PlsFacade.getSettings().ignoredFileNameSet.contains(fileName)
    }

    fun getFileType(fileType: ParadoxFileType): FileType? {
        return when (fileType) {
            ParadoxFileType.Script -> ParadoxScriptFileType
            ParadoxFileType.Localisation -> ParadoxLocalisationFileType
            ParadoxFileType.Csv -> ParadoxCsvFileType
            ParadoxFileType.ModDescriptor -> ParadoxScriptFileType
            else -> null
        }
    }

    fun canOverrideFile(file: PsiFile, fileType: ParadoxFileType): Boolean {
        return when (fileType) {
            ParadoxFileType.Script -> true
            ParadoxFileType.Localisation -> true
            ParadoxFileType.Csv -> true
            ParadoxFileType.ModDescriptor -> false
            ParadoxFileType.Other -> ParadoxImageManager.isImageFile(file) // currently only accept generic images
        }
    }

    fun isTestDataFile(file: VirtualFile): Boolean {
        if (!PlsFacade.isUnitTestMode()) return false
        val name = file.nameWithoutExtension
        return name.split('_', '.').any { it == "test" }
    }

    fun getInjectedGameTypeForTestDataFile(file: VirtualFile): ParadoxGameType? {
        if (!isTestDataFile(file)) return null
        val name = file.nameWithoutExtension
        val injectedGameType = name.split('_', '.').firstNotNullOfOrNull { ParadoxGameType.get(it) }
        file.putUserData(PlsKeys.injectedGameType, injectedGameType)
        return injectedGameType
    }
}

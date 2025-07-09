package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import com.intellij.testFramework.*
import com.intellij.util.io.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import java.nio.file.*
import java.util.*

object ParadoxFileManager {
    object Keys : KeyRegistry() {
        val fileExtensions by createKey<Set<String>>(this)
    }

    private val LOGGER = logger<ParadoxFileManager>()

    const val scriptedVariablesPath = "common/scripted_variables"

    fun getScriptedVariablesDirectory(contextFile: VirtualFile): VirtualFile? {
        val rootInfo = contextFile.fileInfo?.rootInfo ?: return null
        val entryFile = rootInfo.entryFile
        val path = scriptedVariablesPath
        VfsUtil.createDirectoryIfMissing(entryFile, path)
        return entryFile.findFileByRelativePath(path)
    }

    fun canBeScriptOrLocalisationFile(filePath: FilePath): Boolean {
        //val fileName = filePath.name.lowercase()
        val fileExtension = filePath.name.substringAfterLast('.').orNull()?.lowercase() ?: return false
        return when {
            fileExtension == "mod" -> true
            fileExtension in PlsConstants.scriptFileExtensions -> true
            fileExtension in PlsConstants.localisationFileExtensions -> true
            else -> false
        }
    }

    fun canBeScriptOrLocalisationFile(file: VirtualFile): Boolean {
        //require pre-check from user data
        //require further check for VirtualFileWindow (injected PSI)

        if (file is VirtualFileWithoutContent) return false
        if (file is VirtualFileWindow) return true
        //val fileName = file.name.lowercase()
        val fileExtension = file.extension?.lowercase() ?: return false
        return when {
            fileExtension == "mod" -> true
            fileExtension in PlsConstants.scriptFileExtensions -> true
            fileExtension in PlsConstants.localisationFileExtensions -> true
            else -> false
        }
    }

    fun canBeScriptFilePath(path: ParadoxPath): Boolean {
        if (inLocalisationPath(path)) return false
        val fileExtension = path.fileExtension?.lowercase() ?: return false
        if (fileExtension !in PlsConstants.scriptFileExtensions) return false
        return true
    }

    fun canBeLocalisationFilePath(path: ParadoxPath): Boolean {
        if (!inLocalisationPath(path)) return false
        val fileExtension = path.fileExtension?.lowercase() ?: return false
        if (fileExtension !in PlsConstants.localisationFileExtensions) return false
        return true
    }

    fun inLocalisationPath(path: ParadoxPath, synced: Boolean? = null): Boolean {
        val root = path.root
        if (synced != true) {
            if (root == "localisation" || root == "localization") return true
        }
        if (synced != false) {
            if (root == "localisation_synced" || root == "localization_synced") return true
        }
        return false
    }

    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.fileExtensions) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }

    /**
     * 判断目标文件能否引用另一个文件中的内容。
     *
     * 游戏目录下可以存在多个入口目录，
     * 插件认为模组目录以及游戏目录的主要入口目录（游戏目录或其game子目录）不能引用游戏目录的次要入口目录下的文件中的内容。
     */
    fun canReference(targetFile: VirtualFile?, otherFile: VirtualFile?): Boolean {
        val target = targetFile?.fileInfo ?: return true
        val other = otherFile?.fileInfo ?: return true
        if (target.inMainEntry() && !other.inMainEntry()) return false
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
            LOGGER.error(e.message, e)
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
            LOGGER.error(e.message, e)
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
}

package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.testFramework.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.ParadoxFileManager.Keys
import icu.windea.pls.model.*
import java.lang.invoke.*
import java.nio.file.*
import java.util.*

object ParadoxFileManager {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    object Keys: KeyRegistry() {
        val fileExtensions by createKey<Set<String>>(this)
    }
    
    const val scriptedVariablesPath = "common/scripted_variables"
    
    fun getRootDirectory(context: VirtualFile): VirtualFile? {
        return context.fileInfo?.rootInfo?.gameRootFile
    }
    
    fun getScriptedVariablesDirectory(context: VirtualFile): VirtualFile? {
        val root = getRootDirectory(context) ?: return null
        VfsUtil.createDirectoryIfMissing(root, scriptedVariablesPath)
        return root.findFileByRelativePath(scriptedVariablesPath)
    }
    
    /**
     * 判断目标文件能否引用另一个文件中的内容。
     *
     * 对于某些蠢驴游戏来说，游戏目录下可以存在多个入口目录（entryPaths）。
     * 认为模组目录以及主要入口目录（根目录或者game目录）不能引用次要入口目录（非根目录或者game目录）下的文件中的内容。
     */
    fun canReference(targetFile: VirtualFile?, otherFile: VirtualFile?): Boolean {
        val target = targetFile?.fileInfo ?: return true
        val other = otherFile?.fileInfo ?: return true
        if(target.isMainEntry()) {
            if(!other.isMainEntry()) return false
        }
        return true
    }
    
    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>) : Set<String> {
        return config.getOrPutUserData(Keys.fileExtensions) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }
    
    /**
     * 基于指定的虚拟文件创建一个临时文件。
     */
    @Deprecated("Use createLightFile()")
    @JvmStatic
    fun createTempFile(file: VirtualFile): VirtualFile? {
        try {
            val diffDirPath = PlsConstants.Paths.diffDirectoryPath
            val fileName = UUID.randomUUID().toString()
            Files.createDirectories(diffDirPath)
            val diffDirFile = VfsUtil.findFile(diffDirPath, false) ?: return null
            val tempFile = VfsUtil.copyFile(ParadoxFileManager, file, diffDirFile, fileName)
            tempFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
            return tempFile
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }
    
    /**
     * 基于指定的文本和文件信息创建一个临时文件。
     */
    @Deprecated("Use createLightFile()")
    @JvmStatic
    fun createTempFile(text: String, fileInfo: ParadoxFileInfo): VirtualFile? {
        try {
            val diffDirPath = PlsConstants.Paths.diffDirectoryPath
            val fileName = UUID.randomUUID().toString()
            val path = diffDirPath.resolve(fileName)
            Files.writeString(path, text)
            val tempFile = VfsUtil.findFile(path, true) ?: return null
            tempFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
            return tempFile
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }
    
    /**
     * 基于指定的虚拟文件创建一个临时文件。
     */
    @JvmStatic
    fun createLightFile(name: String, file: VirtualFile, project: Project): VirtualFile {
        //为了兼容不同的lineSeparator，这里不能直接使用document.charSequence
        val text = file.toPsiFile(project)?.text ?: throw IllegalStateException()
        val lightFile = LightVirtualFile(name, text)
        lightFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
        return lightFile
    }
    
    /**
     * 基于指定的文本和文件信息创建一个临时文件。
     */
    @JvmStatic
    fun createLightFile(name: String, text: CharSequence, fileInfo: ParadoxFileInfo): VirtualFile {
        val lightFile = LightVirtualFile(name, text)
        lightFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        return lightFile
    }
    
    @JvmStatic
    fun createLightFile(name: String, text: CharSequence, language: Language): VirtualFile {
        val lightFile = LightVirtualFile(name, language, text)
        return lightFile
    }
    
    @JvmStatic
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFile
    }
    
    @JvmStatic
    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }
}

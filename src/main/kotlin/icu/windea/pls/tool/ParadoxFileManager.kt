package icu.windea.pls.tool

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import java.lang.invoke.*
import java.nio.file.*
import java.util.*

object ParadoxFileManager {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    /**
     * 基于指定的文件信息和模版创建一个临时文件。
     */
    @JvmStatic
    fun createTempFile(text: String, fileInfo: ParadoxFileInfo): VirtualFile? {
        try {
            val diffDirPath = PlsPaths.tmpDirectoryPath
            val fileName = UUID.randomUUID().toString()
            val path = diffDirPath.resolve(fileName)
            Files.writeString(path, text)
            val tempFile = VfsUtil.findFile(path, true) ?: return null
            tempFile.putUserData(PlsKeys.injectedFileInfoKey, fileInfo)
            return tempFile
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }
    
    /**
     * 基于指定的文件信息和模版创建一个临时文件。
     */
    @JvmStatic
    fun createTempFile(file: VirtualFile): VirtualFile? {
        try {
            val diffDirPath = PlsPaths.tmpDirectoryPath
            val fileName = UUID.randomUUID().toString()
            Files.createDirectories(diffDirPath)
            val diffDirFile = VfsUtil.findFile(diffDirPath, false) ?: return null
            val tempFile = VfsUtil.copyFile(ParadoxFileManager, file, diffDirFile, fileName)
            tempFile.putUserData(PlsKeys.injectedFileInfoKey, file.fileInfo)
            tempFile.putUserData(PlsKeys.injectedFileTypeKey, file.fileType)
            return tempFile
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e.message, e)
            return null
        }
    }
}
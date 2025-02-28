package icu.windea.pls.core.io

import com.intellij.openapi.vfs.*
import com.intellij.util.io.*
import java.nio.file.*

class FileSynchronizer(
    val filePath: Path,
    val sourceFile: VirtualFile,
) {
    private var file: VirtualFile? = VfsUtil.findFile(filePath, true)
    private var timeStamp = -1L
    private val sourceFileSize by lazy { calculateFileSize(sourceFile) }

    fun synced(): VirtualFile {
        val file = file
        if(file == null || !file.exists()) {
            filePath.createParentDirectories()
            Files.copy(sourceFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            this.file = VfsUtil.findFile(filePath, true) ?: throw IllegalStateException()
            this.timeStamp = this.file!!.timeStamp
            return this.file!!
        }

        if (timeStamp != -1L && timeStamp < file.timeStamp) {
            Files.copy(sourceFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            timeStamp = file.timeStamp
        } else if(timeStamp == -1L) {
            //check whether is same file only if timestamp is not set
            if(!isSameFile()) {
                Files.copy(sourceFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            timeStamp = file.timeStamp
        } else {
            //do nothing
        }
        return file
    }

    private fun isSameFile(): Boolean {
        //currently, check by file size, rather than file hash or file content
        return calculateFileSize(file!!) == sourceFileSize
    }

    private fun calculateFileSize(file: VirtualFile) : Long {
        return file.length
    }
}

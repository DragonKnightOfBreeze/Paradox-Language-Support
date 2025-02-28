package icu.windea.pls.core.io

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*

class FileSynchronizer(
    val file: VirtualFile,
    val sourceFile: VirtualFile,
) {
    init {
        if(file.isDirectory || sourceFile.isDirectory) throw UnsupportedOperationException()
    }

    private var timeStamp = -1L
    private val sourceFileSize by lazy { calculateFileSize(sourceFile) }

    fun synced(): VirtualFile {
        if (!file.exists()) {
            runWriteAction {
                file.delete(this)
                sourceFile.inputStream.copyTo(file.getOutputStream(this))
            }
            timeStamp = file.timeStamp
        } else if (timeStamp != -1L && timeStamp < file.timeStamp) {
            runWriteAction {
                sourceFile.inputStream.copyTo(file.getOutputStream(this))
            }
            timeStamp = file.timeStamp
        } else if(timeStamp == -1L) {
            //check whether is same file only if timestamp is not set
            if(!isSameFile()) {
                runWriteAction {
                    file.delete(this)
                    sourceFile.inputStream.copyTo(file.getOutputStream(this))
                }
            }
            timeStamp = file.timeStamp
        } else {
            //do nothing
        }
        return file
    }

    private fun isSameFile(): Boolean {
        return calculateFileSize(file) == sourceFileSize
    }

    private fun calculateFileSize(file: VirtualFile) : Long {
        return file.length
    }
}

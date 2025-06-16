package icu.windea.pls.core.io

import com.intellij.openapi.application.ex.*
import com.intellij.openapi.vfs.*
import com.intellij.util.io.*
import java.nio.file.*
import kotlin.reflect.*

@Deprecated("")
class VirtualFileProvider(
    val filePath: Path,
    val sourceFileProvider: () -> VirtualFile,
) {
    private var file: VirtualFile? = null
    private var timeStamp = -1L
    private val sourceFile by lazy { sourceFileProvider() }
    private val sourceFileSize by lazy { calculateFileSize(sourceFile) }

    fun get(): VirtualFile {
        return synchronized(this) { doGet() }
    }

    private fun doGet(): VirtualFile {
        //NOTE: Do not perform a synchronous refresh under read lock (causes deadlocks if there are events to fire)
        val refresh = ApplicationManagerEx.getApplicationEx().isDispatchThread || !ApplicationManagerEx.getApplicationEx().holdsReadLock()

        var file = file
        if (file == null) {
            file = VfsUtil.findFile(filePath, refresh)
            this.file = file
        }
        if (file == null || !file.exists()) {
            filePath.createParentDirectories()
            Files.copy(sourceFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            file = VfsUtil.findFile(filePath, refresh)
            if (file == null) throw IllegalStateException()
            this.file = file
            this.timeStamp = file.timeStamp
            return file
        }
        if (timeStamp != -1L && timeStamp < file.timeStamp) {
            Files.copy(sourceFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            timeStamp = file.timeStamp
        } else if (timeStamp == -1L) {
            //check whether is same file only if timestamp is not set
            if (!isSameFile()) {
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

    private fun calculateFileSize(file: VirtualFile): Long {
        return file.length
    }

    @Suppress("NOTHING_TO_INLINE")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): VirtualFile = get()
}

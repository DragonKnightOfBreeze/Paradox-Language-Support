package icu.windea.pls.core.io

import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createParentDirectories
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.reflect.KProperty

@Deprecated("")
/**
 * 基于源 `VirtualFile` 懒加载/同步目标路径的提供器。
 *
 * - 首次访问或目标缺失时，从 [sourceFileProvider] 拷贝到 [filePath] 并返回对应的 `VirtualFile`。
 * - 之后根据时间戳/大小判断是否需要刷新拷贝。
 * - 注意：该实现已废弃，仅保留兼容用途。
 */
class VirtualFileProvider(
    val filePath: Path,
    val sourceFileProvider: () -> VirtualFile,
) {
    private var file: VirtualFile? = null
    private var timeStamp = -1L
    private val sourceFile by lazy { sourceFileProvider() }
    private val sourceFileSize by lazy { calculateFileSize(sourceFile) }

    /** 线程安全地获取（并必要时刷新）目标 `VirtualFile`。 */
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

    /** 以文件大小近似判断是否与源文件一致（不计算哈希以避免开销）。 */
    private fun isSameFile(): Boolean {
        //currently, check by file size, rather than file hash or file content
        return calculateFileSize(file!!) == sourceFileSize
    }

    private fun calculateFileSize(file: VirtualFile): Long {
        return file.length
    }

    @Suppress("NOTHING_TO_INLINE")
    /** 属性委托：惰性获取目标 `VirtualFile`。 */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): VirtualFile = get()
}

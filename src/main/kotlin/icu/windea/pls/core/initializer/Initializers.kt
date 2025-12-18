package icu.windea.pls.core.initializer

import com.intellij.openapi.application.UI
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createDirectories
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** 收集一个“创建目录”的初始化任务并返回对应的懒值。*/
fun Initializer.awaitDirectory(value: Path): Lazy<Path> {
    return await(value) { withContext(Dispatchers.IO) { it.createDirectories() } }
}

/** 收集一个“从 VirtualFile 复制到 Path 并返回 VirtualFile” 的初始化任务并返回懒值。*/
fun Initializer.awaitFileFromVirtualFile(value: Path, sourceUrl: URL): Lazy<VirtualFile> {
    return await(value, { it.createFileFromVirtualFile(VfsUtil.findFileByURL(sourceUrl)!!) }, { VfsUtil.findFile(it, false)!! })
}

/** 将给定 [virtualFile] 的内容复制到当前 [Path]，并刷新 VFS 返回对应的 `VirtualFile`。*/
private suspend fun Path.createFileFromVirtualFile(virtualFile: VirtualFile): VirtualFile? {
    val path = this
    this.createParentDirectories()
    withContext(Dispatchers.IO) {
        virtualFile.inputStream.use { Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING) }
    }
    val file = withContext(Dispatchers.UI) {
        VfsUtil.findFile(path, true)
    }
    return file
}

package icu.windea.pls.core

import com.intellij.openapi.application.UI
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createDirectories
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * 支持聚合异步初始化任务的轻量工具：
 * - 通过 [await] 注册初始化动作并返回延迟值
 * - 调用 [initialize] 并发执行已注册动作，完成后统一解锁延迟值
 */
class SmartInitializer(
    private val completableDeferred: CompletableDeferred<Unit> = CompletableDeferred(),
    private val initializeActions: MutableList<suspend () -> Unit> = mutableListOf()
) {
    /** 并发执行已注册的初始化动作，完成后解锁所有等待的延迟值。 */
    suspend fun initialize() {
        if (initializeActions.isEmpty()) return
        val logger = thisLogger()
        coroutineScope {
            initializeActions.map { action ->
                async {
                    runCatchingCancelable { action() }.onFailure { logger.warn(it) }
                }
            }.awaitAll()
            initializeActions.clear() //这里可以直接清空
            completableDeferred.complete(Unit)
        }
    }

    /** 直接返回给定值的延迟包装（不注册初始化动作）。 */
    fun <T> await(value: T): Lazy<T> {
        return lazyOf(value)
    }

    /** 注册初始化动作，延迟返回给定值；在 [initialize] 完成后才可获取。 */
    fun <T> await(value: T, initializeAction: suspend (T) -> Unit): Lazy<T> {
        initializeActions.add { initializeAction(value) }
        return lazy {
            if (!completableDeferred.isCompleted) {
                runBlocking {
                    completableDeferred.await()
                }
            }
            value
        }
    }

    /** 注册初始化动作并在解锁后通过 [transform] 变换后返回。 */
    fun <T, R> await(value: T, initializeAction: suspend (T) -> Unit, transform: (T) -> R): Lazy<R> {
        initializeActions.add { initializeAction(value) }
        return lazy {
            if (!completableDeferred.isCompleted) {
                runBlocking {
                    completableDeferred.await()
                }
            }
            transform(value)
        }
    }
}

/** 确保目录存在的延迟值，依赖 [initialize] 创建目录。 */
fun SmartInitializer.awaitDirectory(value: Path): Lazy<Path> {
    return await(value) { withContext(Dispatchers.IO) { it.createDirectories() } }
}

/** 复制虚拟文件到给定路径并返回对应的 VirtualFile（延迟）。 */
fun SmartInitializer.awaitFileFromVirtualFile(value: Path, sourceUrl: URL): Lazy<VirtualFile> {
    return await(value, { it.createFileFromVirtualFile(VfsUtil.findFileByURL(sourceUrl)!!) }, { VfsUtil.findFile(it, false)!! })
}

private suspend fun Path.createFileFromVirtualFile(virtualFile: VirtualFile): VirtualFile? {
    val path = this
    this.createParentDirectories()
    withContext(Dispatchers.IO) {
        Files.copy(virtualFile.inputStream, path, StandardCopyOption.REPLACE_EXISTING)
    }
    val file = withContext(Dispatchers.UI) {
        VfsUtil.findFile(path, true)
    }
    return file
}


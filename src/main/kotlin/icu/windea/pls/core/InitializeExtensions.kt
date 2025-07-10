package icu.windea.pls.core

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import com.intellij.util.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.*
import java.nio.file.*

class SmartInitializer(
    private val completableDeferred: CompletableDeferred<Unit> = CompletableDeferred(),
    private val initializeActions: MutableList<suspend () -> Unit> = mutableListOf()
) {
    suspend fun initialize() {
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

    fun <T> await(value: T): Lazy<T> {
        return lazyOf(value)
    }

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

fun SmartInitializer.awaitDirectory(value: Path): Lazy<Path> {
    return await(value) { withContext(Dispatchers.IO) { it.createDirectories() } }
}

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


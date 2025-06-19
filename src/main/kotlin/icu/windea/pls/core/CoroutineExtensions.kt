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

/**
 * 输入的[Flow]经过转换[inputTransform]后会发射不定长度的字符串，
 * 输出的[Flow]在转换[outputTransform]前会发射整行的字符串。
 */
fun <T, R> Flow<T>.toLineFlow(
    inputTransform: (T) -> String,
    outputTransform: (String) -> R
): Flow<R> = flow {
    val buffer = StringBuilder()
    collect { input ->
        val input0 = inputTransform(input)
        if (input0.isEmpty()) return@collect
        buffer.append(input0)
        var lineEnd: Int
        while (buffer.indexOf('\n').also { lineEnd = it } != -1) {
            val line = buffer.substring(0, lineEnd)
            val output0 = outputTransform(line)
            emit(output0)
            buffer.delete(0, lineEnd + 1)
        }
    }
    // 处理最后可能剩余的不完整行（如果没有换行符结尾）
    if (buffer.isNotEmpty()) {
        val output0 = outputTransform(buffer.toString())
        emit(output0)
    }
}

/**
 * 输入的[Flow]会发射不定长度的字符串，
 * 输出的[Flow]会发射整行的字符串。
 */
fun Flow<String>.toLineFlow(): Flow<String> {
    return toLineFlow({ it }, { it })
}

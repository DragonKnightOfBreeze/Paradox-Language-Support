package icu.windea.pls.integrations.images.providers

import com.intellij.execution.ExecutionException
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.runBlockingCancellable
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.cache.CacheBuilder
import icu.windea.pls.core.util.io.ImageInputStreamAdapter
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.core.util.io.IoService
import icu.windea.pls.core.util.io.deleteQuietly
import icu.windea.pls.core.util.io.moveToAtomically
import icu.windea.pls.integrations.images.ImageToolProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.days

/**
 * 基于命令行工具的图片工具提供者。
 *
 * 备注：
 * - 对于流式输入，会比较输入内容的 SHA-256，并把外部工具的转换结果缓存到工具缓存目录，从而对相同内容只执行一次外部命令。
 * - 缓存的写入遵循"临时文件校验 + 原子落位"，避免留下残缺的缓存文件。
 * - 显式指定落盘路径的 [convertImageFormat] 不会经过该隐式缓存。
 */
abstract class CommandBasedImageToolProvider : ImageToolProvider {
    private companion object {
        const val CACHE_BUFFER_SIZE = 8 * 1024
        val CACHE_TOUCH_INTERVAL = 1.days.inWholeMilliseconds
    }

    // 以缓存路径为粒度的并发锁，避免并发请求同一新流时重复拉起外部子进程
    private val cachePathLocks = CacheBuilder("maximumSize=512,expireAfterAccess=10m").build<Path, Mutex>()

    final override fun isAvailable(): Boolean {
        return isEnabled() && isSupported() && isValid()
    }

    abstract fun isEnabled(): Boolean

    abstract fun isSupported(): Boolean

    open fun isValid(): Boolean {
        val executablePath = getExecutablePath() ?: return false
        return validateExecutablePath(executablePath)
    }

    abstract fun getExecutablePath(): Path?

    abstract fun validateExecutablePath(path: Path): Boolean

    /** 临时目录，用于存放中转文件。可在测试中覆写以完全隔离磁盘目录。 */
    open fun getTempDirectory(): Path {
        return ChronicleDataPathService.getInstance().imagesTempPath
    }

    /** 工具缓存目录，用于存放基于输入内容 SHA-256 的确定性转换结果。可在测试中覆写以完全隔离磁盘目录。 */
    open fun getToolCacheDirectory(): Path {
        return ChronicleDataPathService.getInstance().imagesToolCachePath
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage {
        val sourceFormat = normalizeImageFormat(sourceFormat)
        val targetFormat = normalizeImageFormat(targetFormat)
        val startPos = stream.streamPosition
        val sha256 = stream.computeSha256()
        val cachePath = getCachePath(sha256, sourceFormat, targetFormat)

        // 命中确定性缓存时直接解码；若解码失败则删除损坏的缓存，并回退到重建流程
        decodeCachedImage(cachePath, targetFormat)?.let { return it }

        return ProgressManager.getInstance().runProcess<BufferedImage>({
            runBlockingCancellable {
                val mutex = getCachePathMutex(cachePath)
                mutex.withLock {
                    // double-check：可能已被并发请求写入（此时无需落盘源临时文件）
                    decodeCachedImage(cachePath, targetFormat)?.let { return@withLock it }

                    val sourceTempPath = getTempDirectory().resolve("${UUID.randomUUID()}.$sourceFormat")
                    val targetTempPath = getTempDirectory().resolve("${UUID.randomUUID()}.$targetFormat")
                    try {
                        stream.seek(startPos)
                        sourceTempPath.outputStream().use { outputStream ->
                            IoService.copy(ImageInputStreamAdapter(stream), outputStream)
                        }
                        doConvertImageFormat(sourceTempPath, targetTempPath, sourceFormat, targetFormat)
                        if (!ImageIoService.isValidImageFile(targetTempPath, targetFormat)) {
                            throw IllegalStateException("Invalid image file after conversion: $targetTempPath")
                        }
                        targetTempPath.moveToAtomically(cachePath)
                        cachePath.inputStream().use { inputStream -> ImageIoService.readImage(inputStream) }
                    } finally {
                        sourceTempPath.deleteQuietly() // 确保删除源临时文件
                        targetTempPath.deleteQuietly() // 确保删除未移动成功的临时输出文件
                    }
                }
            }
        }, EmptyProgressIndicator())
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override suspend fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return
        // implementation
        val sourceFormat = normalizeImageFormat(sourceFormat)
        val targetFormat = normalizeImageFormat(targetFormat)
        val sourceTempPath = getTempDirectory().resolve("${UUID.randomUUID()}.$sourceFormat")
        val targetTempPath = getTempDirectory().resolve("${UUID.randomUUID()}.$targetFormat")
        try {
            // 边拷贝到源临时文件，边计算输入内容的 SHA-256
            val digest = MessageDigest.getInstance("SHA-256")
            sourceTempPath.outputStream().use { fileOutputStream ->
                DigestOutputStream(fileOutputStream, digest).use { digestOutputStream ->
                    IoService.copy(inputStream, digestOutputStream)
                }
            }
            val sha256 = digest.digest().toHexString()
            val cachePath = getCachePath(sha256, sourceFormat, targetFormat)

            // 命中确定性缓存时直接返回缓存内容，跳过外部命令
            getValidCachePath(cachePath, targetFormat)?.let { path ->
                path.inputStream().use { inputStream0 -> IoService.copy(inputStream0, outputStream) }
                return
            }

            val mutex = getCachePathMutex(cachePath)
            mutex.withLock {
                // double-check：可能已被并发请求写入
                getValidCachePath(cachePath, targetFormat)?.let { path ->
                    path.inputStream().use { inputStream0 -> IoService.copy(inputStream0, outputStream) }
                    return@withLock
                }
                doConvertImageFormat(sourceTempPath, targetTempPath, sourceFormat, targetFormat)
                if (!ImageIoService.isValidImageFile(targetTempPath, targetFormat)) {
                    throw IllegalStateException("Invalid image file after conversion: $targetTempPath")
                }
                targetTempPath.moveToAtomically(cachePath)
                cachePath.inputStream().use { inputStream0 -> IoService.copy(inputStream0, outputStream) }
            }
        } finally {
            sourceTempPath.deleteQuietly() // 确保删除源临时文件
            targetTempPath.deleteQuietly() // 确保删除未移动成功的临时输出文件
        }
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override suspend fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return
        // implementation
        try {
            doConvertImageFormat(path, targetPath, sourceFormat, targetFormat)
        } catch (e: Exception) {
            targetPath.deleteQuietly()
            throw e
        }
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    protected abstract suspend fun doConvertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String)

    private fun normalizeImageFormat(format: String): String {
        return ImageIoService.normalizeImageFormat(format).lowercase()
    }

    private fun getCachePath(sha256: String, sourceFormat: String, targetFormat: String): Path {
        val cachePath = getToolCacheDirectory()
            .resolve(name.lowercase())
            .resolve("$sha256.$sourceFormat.$targetFormat")
        cachePath.parent?.createDirectories()
        return cachePath
    }

    private fun getCachePathMutex(cachePath: Path): Mutex {
        return cachePathLocks.get(cachePath) { Mutex() }
    }

    /**
     * 获取已校验的缓存路径。
     *
     * 备注：
     * - 先轻量校验缓存文件，不合法时安全删除并返回 `null`，以便调用方重建。
     * - 合法时若距上次修改超过 1 天，则节流更新其修改时间。
     */
    private fun getValidCachePath(cachePath: Path, targetFormat: String): Path? {
        if (!ImageIoService.isValidImageFile(cachePath, targetFormat)) {
            cachePath.deleteQuietly()
            return null
        }
        touchCacheFile(cachePath)
        return cachePath
    }

    private fun decodeCachedImage(cachePath: Path, targetFormat: String): BufferedImage? {
        val path = getValidCachePath(cachePath, targetFormat) ?: return null
        return try {
            path.inputStream().use { inputStream -> ImageIoService.readImage(inputStream) }
        } catch (e: Throwable) {
            checkCancellation(e)
            path.deleteQuietly()
            null
        }
    }

    /**
     * 节流更新缓存的修改时间：仅当距上次修改超过 1 天时才刷新。
     */
    private fun touchCacheFile(path: Path) {
        runCatching {
            val now = System.currentTimeMillis()
            val lastModified = Files.getLastModifiedTime(path).toMillis()
            if (now - lastModified > CACHE_TOUCH_INTERVAL) {
                Files.setLastModifiedTime(path, FileTime.fromMillis(now))
            }
        }
    }

    private fun ImageInputStream.computeSha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(CACHE_BUFFER_SIZE)
        while (true) {
            val length = read(buffer)
            if (length < 0) break
            digest.update(buffer, 0, length)
        }
        return digest.digest().toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}

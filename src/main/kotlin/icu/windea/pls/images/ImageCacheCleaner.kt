package icu.windea.pls.images

import com.intellij.openapi.diagnostic.logger
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.images.policies.ImageCacheCleanupPolicy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.time.Duration.Companion.days

/**
 * 图片磁盘缓存的清理器。
 *
 * 备注：当前只提供基础策略；更细粒度的策略后续可以通过 [ImageCacheCleanupPolicy] 扩展。
 */
class ImageCacheCleaner(
    private val imagesPath: Path = ChronicleDataPathService.getInstance().imagesPath
) {
    companion object {
        private val logger = logger<ImageCacheCleaner>()

        private val ttl = 30.days
        private const val maxSize = 1L * 1024 * 1024 * 1024
        private const val maxFiles = 10_000
    }

    fun cleanup(policy: ImageCacheCleanupPolicy = ImageCacheCleanupPolicy.ByTtlAndSize): Int {
        if (policy == ImageCacheCleanupPolicy.None) return 0
        val entries = collectEntries() ?: return 0
        if (entries.isEmpty()) return 0

        var deleted = 0
        val remaining = entries.toMutableList()
        if (policy == ImageCacheCleanupPolicy.ByTtl || policy == ImageCacheCleanupPolicy.ByTtlAndSize) {
            val expireBefore = System.currentTimeMillis() - ttl.inWholeMilliseconds
            remaining.removeAll { entry ->
                if (entry.modifiedAt >= expireBefore) return@removeAll false
                val result = delete(entry.path)
                if (result) deleted++
                result
            }
        }
        if (policy == ImageCacheCleanupPolicy.BySize || policy == ImageCacheCleanupPolicy.ByTtlAndSize) {
            var totalSize = remaining.sumOf { it.size }
            var totalFiles = remaining.size
            if (totalSize > maxSize || totalFiles > maxFiles) {
                val iterator = remaining.sortedBy { it.modifiedAt }.iterator()
                while (iterator.hasNext() && (totalSize > maxSize || totalFiles > maxFiles)) {
                    val entry = iterator.next()
                    if (delete(entry.path)) {
                        deleted++
                        totalSize -= entry.size
                        totalFiles--
                    }
                }
            }
        }
        if (deleted > 0) logger.info("Cleaned up $deleted image cache file(s).")
        return deleted
    }

    private fun collectEntries(): List<Entry>? {
        if (!Files.isDirectory(imagesPath)) return emptyList()
        val tempPath = imagesPath.resolve("_tmp")
        return try {
            Files.walk(imagesPath).use { stream ->
                stream.filter { path ->
                    Files.isRegularFile(path) && path.extension.equals("png", ignoreCase = true) && !path.startsWith(tempPath)
                }.map { path ->
                    runCatching {
                        Entry(path, Files.size(path), Files.getLastModifiedTime(path).toMillis())
                    }.getOrNull()
                }.toList().filterNotNull()
            }
        } catch (e: Exception) {
            logger.warn("Failed to collect image cache files.", e)
            null
        }
    }

    private fun delete(path: Path): Boolean {
        return runCatching { Files.deleteIfExists(path) }.getOrDefault(false)
    }

    private data class Entry(
        val path: Path,
        val size: Long,
        val modifiedAt: Long,
    )
}

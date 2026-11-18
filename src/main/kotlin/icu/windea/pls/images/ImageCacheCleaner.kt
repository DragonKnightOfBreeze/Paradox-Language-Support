package icu.windea.pls.images

import com.intellij.openapi.diagnostic.logger
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.util.io.deleteQuietly
import icu.windea.pls.model.policies.ImageCacheCleanupPolicy
import org.apache.commons.io.file.PathUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * 图片磁盘缓存的清理器。
 *
 * 支持通过 [ImageCacheCleanupPolicy] 选择不清理、按 TTL、按大小或同时按 TTL 和大小清理。
 *
 * 备注：
 * - `_tmp` 目录作为临时区，不参与常规清理，但会清理其中存在超过 1 天的孤儿临时文件。
 * - `_tool` 目录参与常规清理。
 * - `_placeholder.png` 不会被清理。
 * - 清理结束后会自底向上删除已清空的子目录，但保留 `imagesPath` 根目录以及 `_tmp`、`_tool` 目录本身。
 */
class ImageCacheCleaner(
    private val imagesPath: Path = ChronicleDataPathService.getInstance().imagesPath,
    private val ttl: Duration = ChronicleImageSettings.DEFAULT_CACHE_TTL_DAYS.days,
    private val maxSize: Long = ChronicleImageSettings.DEFAULT_CACHE_MAX_SIZE_MB.toLong() * 1024 * 1024,
    private val maxFiles: Int = ChronicleImageSettings.DEFAULT_CACHE_MAX_FILES,
) {
    companion object {
        private val logger = logger<ImageCacheCleaner>()

        private const val TEMP_DIR_NAME = "_tmp"
        private const val TOOL_DIR_NAME = "_tool"
        private const val PLACEHOLDER_FILE_NAME = "_placeholder.png"

        private val orphanTempTtl = 1.days
        private val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp", "dds", "tga")
    }

    fun cleanup(policy: ImageCacheCleanupPolicy = ImageCacheCleanupPolicy.ByTtlAndSize): Int {
        if (policy == ImageCacheCleanupPolicy.None) return 0

        val tempPath = imagesPath.resolve(TEMP_DIR_NAME)
        var deleted = cleanupOrphanTempFiles(tempPath)

        val entries = collectEntries(tempPath)
        if (!entries.isNullOrEmpty()) {
            deleted += cleanupEntries(entries, policy)
        }

        cleanupEmptyDirectories()
        if (deleted > 0) logger.info("Cleaned up $deleted image cache file(s).")
        return deleted
    }

    private fun cleanupEntries(entries: List<Entry>, policy: ImageCacheCleanupPolicy): Int {
        var deleted = 0
        val remaining = entries.toMutableList()
        if (policy == ImageCacheCleanupPolicy.ByTtl || policy == ImageCacheCleanupPolicy.ByTtlAndSize) {
            val expireBefore = System.currentTimeMillis() - ttl.inWholeMilliseconds
            remaining.removeAll { entry ->
                if (entry.modifiedAt >= expireBefore) return@removeAll false
                val result = entry.path.deleteQuietly()
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
                    if (entry.path.deleteQuietly()) {
                        deleted++
                        totalSize -= entry.size
                        totalFiles--
                    }
                }
            }
        }
        return deleted
    }

    /**
     * 清理 `_tmp` 目录下存在超过 1 天的孤儿临时文件。
     */
    private fun cleanupOrphanTempFiles(tempPath: Path): Int {
        if (!Files.isDirectory(tempPath)) return 0
        val expireBefore = System.currentTimeMillis() - orphanTempTtl.inWholeMilliseconds
        return try {
            var deleted = 0
            val paths = walkAll(tempPath).filter { it.isRegularFile() }
            for (path in paths) {
                val modifiedAt = runCatching { Files.getLastModifiedTime(path).toMillis() }.getOrNull() ?: continue
                if (modifiedAt >= expireBefore) continue
                if (path.deleteQuietly()) deleted++
            }
            deleted
        } catch (e: Exception) {
            logger.warn("Failed to clean up orphan image temp files.", e)
            0
        }
    }

    private fun collectEntries(tempPath: Path): List<Entry>? {
        if (!Files.isDirectory(imagesPath)) return emptyList()
        return try {
            walkAll(imagesPath)
                .filter { path ->
                    path.isRegularFile() &&
                        isImageFile(path) &&
                        !path.fileName.toString().equals(PLACEHOLDER_FILE_NAME, ignoreCase = true) &&
                        !path.startsWith(tempPath)
                }
                .mapNotNull { path -> createEntry(path) }
        } catch (e: Exception) {
            logger.warn("Failed to collect image cache files.", e)
            null
        }
    }

    private fun isImageFile(path: Path): Boolean {
        return path.extension.lowercase() in imageExtensions
    }

    private fun createEntry(path: Path): Entry? {
        return runCatching { Entry(path, Files.size(path), Files.getLastModifiedTime(path).toMillis()) }.getOrNull()
    }

    /**
     * 自底向上删除已清空的子目录。
     */
    private fun cleanupEmptyDirectories() {
        if (!Files.isDirectory(imagesPath)) return
        try {
            val tempPath = imagesPath.resolve(TEMP_DIR_NAME)
            val toolPath = imagesPath.resolve(TOOL_DIR_NAME)
            val directories = walkAll(imagesPath)
                .filter { Files.isDirectory(it) }
                .sortedByDescending { it.nameCount }
            for (directory in directories) {
                if (directory == imagesPath || directory == tempPath || directory == toolPath) continue
                if (!PathUtils.isEmptyDirectory(directory)) continue
                directory.deleteQuietly()
            }
        } catch (e: Exception) {
            logger.warn("Failed to clean up empty image cache directories.", e)
        }
    }

    /**
     * 收集根目录下的所有路径。
     *
     * 备注：
     * - 使用 [Files.walk] 并在返回前关闭底层流，避免在 Windows 上因目录句柄未释放而无法删除空目录。
     */
    private fun walkAll(root: Path): List<Path> {
        val stream = Files.walk(root)
        stream.use { stream ->
            val paths = mutableListOf<Path>()
            stream.forEach { paths.add(it) }
            return paths
        }
    }

    private data class Entry(
        val path: Path,
        val size: Long,
        val modifiedAt: Long,
    )
}

package icu.windea.pls.images

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import icu.windea.pls.core.runCatchingCancellable
import icu.windea.pls.core.util.cache.CacheBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * 统一获取图片的原始元数据。
 */
@Service
class ImageMetadataService : Disposable {
    private data class CacheKey(
        val path: String,
        val length: Long,
        val modificationStamp: Long,
    )

    // TODO 3.0.5 后续可以按需调整为更细粒度的缓存策略
    private val cache = CacheBuilder("maximumSize=4096,expireAfterAccess=10m")
        .build<CacheKey, ImageMetadata>()

    /**
     * 获取图片的原始元数据。无法读取时抛出异常。
     */
    @RequiresReadLock(generateAssertion = false)
    fun getMetadata(file: VirtualFile, project: Project? = null): ImageMetadata {
        return getMetadataOrNull(file, project) ?: error("Cannot read image metadata: ${file.path}")
    }

    /**
     * 获取图片的原始元数据。无法读取时返回 `null`。
     */
    @RequiresReadLock(generateAssertion = false)
    fun getMetadataOrNull(file: VirtualFile, project: Project? = null): ImageMetadata? {
        val key = CacheKey(file.path, file.length, file.modificationStamp)
        cache.getIfPresent(key)?.let { return it }
        val metadata = runCatchingCancellable { ImageMetadataReader.read(file, project) }
            .onFailure { thisLogger().warn(it) }
            .getOrNull()
        if (metadata != null) cache.put(key, metadata)
        return metadata
    }

    /**
     * 获取图片的原始元数据。无法读取时抛出异常。
     */
    fun getMetadata(path: Path): ImageMetadata {
        return getMetadataOrNull(path) ?: error("Cannot read image metadata: $path")
    }

    /**
     * 获取图片的原始元数据。无法读取时返回 `null`。
     */
    fun getMetadataOrNull(path: Path): ImageMetadata? {
        val key = runCatchingCancellable {
            CacheKey(path.toAbsolutePath().normalize().toString(), Files.size(path), Files.getLastModifiedTime(path).toMillis())
        }.getOrNull() ?: return null
        cache.getIfPresent(key)?.let { return it }
        val metadata = runCatchingCancellable { ImageMetadataReader.read(path) }
            .onFailure { thisLogger().warn(it) }
            .getOrNull()
        if (metadata != null) cache.put(key, metadata)
        return metadata
    }

    override fun dispose() {
        cache.invalidateAll()
        cache.cleanUp()
    }

    companion object {
        @JvmStatic
        fun getInstance(): ImageMetadataService = service()
    }
}

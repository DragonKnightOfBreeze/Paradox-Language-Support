package icu.windea.pls.lang.util.image

import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.dds.DdsManager
import icu.windea.pls.model.*
import java.nio.file.*
import javax.imageio.*
import kotlin.io.path.*

object ParadoxDdsImageResolver {
    private val ddsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //absPath - pngPath

    /**
     * 将DDS文件转化为PNG文件，然后返回PNG文件的绝对路径。如果发生异常，则返回null。
     * @param absPath DDS文件的绝对路径。
     * @param relPath DDS文件相对于游戏或模组目录的路径（如果可以获取）。
     * @param frameInfo 帧数信息，用于切分图片。
     */
    fun resolveUrl(absPath: String, relPath: String? = null, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果存在基于DDS文件绝对路径的缓存数据，则使用缓存的PNG文件绝对路径
            val finalAbsPath = absPath.normalizePath()
            val pngAbsPath = getPngAbsPath(finalAbsPath, relPath, frameInfo)
            if (pngAbsPath.notExists()) {
                doConvertDdsToPng(finalAbsPath.toPath(), pngAbsPath, frameInfo)
            }
            return pngAbsPath.toString()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Resolve url for dds image failed. (dds absolute path: $absPath, dds relative path: $relPath, frame info: $frameInfo)", e)
            return null
        }
    }

    private fun getPngAbsPath(absPath: String, relPath: String?, frameInfo: ImageFrameInfo?): Path {
        val cacheKey = getCacheKey(absPath, frameInfo)
        return ddsCache.get(cacheKey) { doGetPngAbsPath(absPath, relPath, frameInfo) }
    }

    private fun getCacheKey(absPath: String, frameInfo: ImageFrameInfo?): String {
        if (frameInfo != null) {
            return "$absPath@${frameInfo.frame}_${frameInfo.frames}"
        } else {
            return absPath
        }
    }

    private fun doGetPngAbsPath(absPath: String, relPath: String?, frameInfo: ImageFrameInfo?): Path {
        val pngAbsPath = doResolvePngAbsPath(absPath, relPath, frameInfo)
        doConvertDdsToPng(absPath.toPath(), pngAbsPath, frameInfo)
        return pngAbsPath
    }

    private fun doResolvePngAbsPath(absPath: String, relPath: String?, frameInfo: ImageFrameInfo?): Path {
        val imagesPath = PlsConstants.Paths.images
        imagesPath.createDirectories()
        if (relPath != null) {
            //路径：~/.pls/images/${relPathWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于游戏或模组目录的绝对路径
            val relPathWithoutExtension = relPath.substringBeforeLast('.')
            val uuid = absPath.removeSuffix(relPath).trim('/').toUUID().toString()
            val frameText = if (frameInfo != null) "@${frameInfo.frame}_${frameInfo.frames}" else ""
            val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        } else {
            //路径：~/.pls/images/_external/${fileNameWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于DDS文件所在目录
            val index = absPath.lastIndexOf('/')
            val parent = if (index == -1) "" else absPath.substring(0, index)
            val fileName = if (index == -1) absPath else absPath.substring(index + 1)
            val fileNameWithoutExtension = fileName.substringBeforeLast('.')
            val uuid = if (parent.isEmpty()) "" else parent.toUUID().toString()
            val frameText = if (frameInfo != null) "@${frameInfo.frame}_${frameInfo.frames}" else ""
            val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        }
    }

    private fun doConvertDdsToPng(absPath: Path, pngAbsPath: Path, frameInfo: ImageFrameInfo?) {
        pngAbsPath.deleteIfExists()
        pngAbsPath.create()
        Files.newOutputStream(pngAbsPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { outputStream ->
            val inputStream = absPath.inputStream()
            DdsManager.convertImageFormat(inputStream, outputStream, "dds", "png")
            outputStream.flush()
        }
        if(frameInfo == null) return
        val image = ImageIO.read(pngAbsPath.toFile())
        val newImage = image.sliceBy(frameInfo) ?: return
        ImageIO.write(newImage, "png", pngAbsPath.toFile())
    }

    /**
     * 移除DDS文件缓存，以便重新转化。
     * @param absPath DDS文件的绝对路径。
     * @param frameInfo 帧数信息，用于切分图片。
     */
    fun invalidateUrl(absPath: String, frameInfo: ImageFrameInfo? = null) {
        val finalAbsPath = absPath.normalizePath()
        val cacheKey = getCacheKey(finalAbsPath, frameInfo)
        ddsCache.invalidate(cacheKey)
    }
}


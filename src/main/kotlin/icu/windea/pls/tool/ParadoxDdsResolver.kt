package icu.windea.pls.tool

import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import java.lang.invoke.*
import java.nio.file.*
import kotlin.io.path.*

object ParadoxDdsResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    private val ddsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //absPath - pngAbsPath
    private val externalDdsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //absPath - pngAbsPath
    
    /**
     * 将DDS文件转化为PNG文件，然后返回PNG文件的绝对路径。如果发生异常，则返回null。
     * @param absPath DDS文件的绝对路径。
     * @param relPath DDS文件相对于游戏或模组根路径的路径（如果可以获取）。
     * @param frameInfo 帧数信息，用于切分图片。
     */
    fun resolveUrl(absPath: String, relPath: String? = null, frameInfo: FrameInfo? = null): String? {
        try {
            //如果存在基于DDS文件绝对路径的缓存数据，则使用缓存的PNG文件绝对路径
            val pngAbsPath = getPngAbsPath(absPath.replace('\\', '/'), relPath, frameInfo)
            if(pngAbsPath.notExists()) {
                doConvertDdsToPng(absPath, pngAbsPath, frameInfo)
            }
            return pngAbsPath.absolutePathString()
        } catch(e: Exception) {
            logger.warn("Convert dds image to png image failed. (dds absolute path: $absPath, dds relative path: $relPath, frame info: $frameInfo)", e)
            return null
        }
    }
    
    private fun getPngAbsPath(absPath: String, relPath: String?, frameInfo: FrameInfo?): Path {
        val cache = if(relPath != null) ddsCache else externalDdsCache
        val cacheKey = getCacheKey(absPath, frameInfo)
        return cache.get(cacheKey) { doGetPngAbsPath(absPath, relPath, frameInfo) }
    }
    
    private fun getCacheKey(absPath: String, frameInfo: FrameInfo?): String {
        if(frameInfo != null) {
            return "$absPath@${frameInfo.frame}"
        } else {
            return absPath
        }
    }
    
    private fun doGetPngAbsPath(absPath: String, relPath: String?, frameInfo: FrameInfo?): Path {
        val pngAbsPath = doGetRelatedPngPath(absPath, relPath, frameInfo)
        doConvertDdsToPng(absPath, pngAbsPath, frameInfo)
        return pngAbsPath
    }
    
    private fun doGetRelatedPngPath(absPath: String, relPath: String?, frameInfo: FrameInfo?): Path {
        if(relPath != null) {
            //路径：~/.pls/images/${relPathWithoutExtension}@${frame}@${uuid}.png
            //UUID：基于游戏或模组目录的绝对路径
            val relPathWithoutExtension = relPath.substringBeforeLast('.')
            val uuid = absPath.removeSuffix(relPath).trim('/').toUUID().toString()
            val frameText = if(frameInfo != null) "@${frameInfo.frame}" else ""
            val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
            return PlsConstants.Paths.imagesDirectoryPath.resolve(finalPath)
        } else {
            //路径：~/.pls/images/_external/{fileNameWithoutExtension}@${frame}@${uuid}.png
            //UUID：基于DDS文件所在目录
            val index = absPath.lastIndexOf('/')
            val parent = if(index == -1) "" else absPath.substring(0, index)
            val fileName = if(index == -1) absPath else absPath.substring(index + 1)
            val fileNameWithoutExtension = fileName.substringBeforeLast('.')
            val uuid = if(parent.isEmpty()) "" else parent.toUUID().toString()
            val frameText = if(frameInfo != null) "@${frameInfo.frame}" else ""
            val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
            return PlsConstants.Paths.imagesDirectoryPath.resolve(finalPath)
        }
    }
    
    private fun doConvertDdsToPng(absPath: String, pngAbsPath: Path, frameInfo: FrameInfo?) {
        pngAbsPath.deleteIfExists()
        pngAbsPath.create()
        Files.newOutputStream(pngAbsPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { outputStream ->
            val inputStream = absPath.toPath().inputStream()
            ImageManager.convertDdsToPng(inputStream, outputStream, frameInfo)
            outputStream.flush()
        }
    }
    
    /**
     * 移除DDS文件缓存，以便重新转化。
     * @param absPath DDS文件的绝对路径。
     * @param frameInfo 帧数信息，用于切分图片。
     */
    fun invalidateUrl(absPath: String, frameInfo: FrameInfo? = null) {
        val cacheKey = getCacheKey(absPath.replace('\\', '/'), frameInfo)
        ddsCache.invalidate(cacheKey)
        externalDdsCache.invalidate(cacheKey)
    }
    
    fun getUnknownPngUrl(): String {
        if(PlsConstants.Paths.unknownPngPath.notExists()) {
            PlsConstants.Paths.unknownPngClasspathUrl.openStream().use { inputStream ->
                Files.copy(inputStream, PlsConstants.Paths.unknownPngPath) //将jar包中的unknown.png复制到~/.pls/images中
            }
        }
        return PlsConstants.Paths.unknownPngPath.toString()
    }
}


package icu.windea.pls.tool

import co.phoenixlab.dds.*
import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import java.lang.invoke.*
import java.nio.file.*
import kotlin.io.path.*

/**
 * 基于[DDS4J](https://github.com/vincentzhang96/DDS4J)实现的DDS转化器。用于将DDS图片转化为PNG图片并得到对应的图片路径。
 */
object DdsConverter {
	private val logger =  Logger.getInstance(MethodHandles.lookup().lookupClass())
	
	private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }
	
	private val ddsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //absPath - pngAbsPath
	private val externalDdsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //absPath - pngAbsPath
	
	/**
	 * 将DDS文件转化为PNG文件，返回PNG文件的绝对路径。如果发生异常，则返回null。
	 * @param absPath DDS文件的绝对路径。
	 * @param relPath DDS文件相对于游戏或模组根路径的路径（如果可以获取）。
	 * @param frame 帧数，用于切割DDS图片。默认为0表示不切割。
	 */
	fun convertUrl(absPath: String, relPath: String? = null, frame: Int = 0): String? {
		try {
			//如果存在基于DDS文件绝对路径的缓存数据，则使用缓存的PNG文件绝对路径
			val pngAbsPath = getPngAbsPath(absPath.replace('\\', '/'), relPath, frame)
			if(pngAbsPath.notExists()) {
				doConvertDdsToPng(absPath, pngAbsPath, frame)
			}
			return pngAbsPath.absolutePathString()
		} catch(e: Exception) {
			logger.warn("Convert dds image to png image failed. (dds absolute path: $absPath, dds relative path: $relPath, frame: $frame)", e)
			return null
		}
	}
	
	private fun getPngAbsPath(absPath: String, relPath: String?, frame: Int): Path {
		val cache = if(relPath != null) ddsCache else externalDdsCache
		val cacheKey = getCacheKey(absPath, frame)
		return cache.get(cacheKey) { doGetPngAbsPath(absPath, relPath, frame) }
	}
	
	private fun doGetPngAbsPath(absPath: String, relPath: String?, frame: Int): Path {
		val pngAbsPath = doGetRelatedPngPath(absPath, relPath, frame)
		doConvertDdsToPng(absPath, pngAbsPath, frame)
		return pngAbsPath
	}
	
	private fun doGetRelatedPngPath(absPath: String, relPath: String?, frame: Int): Path {
		if(relPath != null) {
			//路径：~/.pls/images/${relPathWithoutExtension}@${frame}@${uuid}.png
			//UUID：基于游戏或模组目录的绝对路径
			val relPathWithoutExtension = relPath.substringBeforeLast('.')
			val uuid = absPath.removeSuffix(relPath).trim('/').toUUID().toString()
			val frameText = if(frame > 0) "@$frame" else ""
			val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
			return PlsPaths.imagesDirectoryPath.resolve(finalPath) 
		} else {
			//路径：~/.pls/images/_external/{fileNameWithoutExtension}@${frame}@${uuid}.png
			//UUID：基于DDS文件所在目录
			val index = absPath.lastIndexOf('/')
			val parent = if(index == -1) "" else absPath.substring(0, index)
			val fileName = if(index == -1) absPath else absPath.substring(index + 1)
			val fileNameWithoutExtension = fileName.substringBeforeLast('.')
			val uuid = if(parent.isEmpty()) "" else parent.toUUID().toString() 
			val frameText = if(frame > 0) "@$frame" else ""
			val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
			return PlsPaths.imagesDirectoryPath.resolve(finalPath)
		}
	}
	
	private fun doConvertDdsToPng(absPath: String, pngAbsPath: Path, frame: Int) {
		val dds = Dds()
		dds.read(Files.newByteChannel(absPath.toPath(), StandardOpenOption.READ))
		pngAbsPath.deleteIfExists()
		pngAbsPath.create()
		Files.newOutputStream(pngAbsPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { outputStream ->
			if(frame > 0) {
				ddsImageDecoder.convertToPNG(dds, outputStream, frame)
			} else {
				ddsImageDecoder.convertToPNG(dds, outputStream)
			}
			outputStream.flush()
		}
	}
	
	/**
	 * 移除DDS文件缓存，以便重新转化。
	 * @param absPath DDS文件的绝对路径。
	 * @param frame 帧数，用于切割DDS图片。默认为0表示不切割。
	 */
	fun invalidateUrl(absPath: String, frame: Int = 0) {
		val cacheKey = getCacheKey(absPath.replace('\\', '/'), frame)
		ddsCache.invalidate(cacheKey)
		externalDdsCache.invalidate(cacheKey)
	}
	
	private fun getCacheKey(absPath: String, frame: Int): String {
		if(frame > 0) {
			return "$absPath@$frame"
		} else {
			return absPath
		}
	}
	
	fun getUnknownPngUrl(): String {
		if(PlsPaths.unknownPngPath.notExists()) {
			PlsPaths.unknownPngClasspathUrl.openStream().use { inputStream ->
				Files.copy(inputStream, PlsPaths.unknownPngPath) //将jar包中的unknown.png复制到~/.pls/images中
			}
		}
		return PlsPaths.unknownPngPath.toString()
	}
	
	fun convertBytes(file: VirtualFile): ByteArray? {
		val dds = Dds()
		dds.read(file.inputStream)
		return ddsImageDecoder.convertToPNG(dds)
	}
}
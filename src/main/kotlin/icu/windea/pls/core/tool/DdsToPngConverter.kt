package icu.windea.pls.core.tool

import co.phoenixlab.dds.*
import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.lang.invoke.*
import java.nio.file.*
import kotlin.io.path.*

/**
 * DDS文件到PNG文件的转化器。
 *
 * 基于[DDS4J](https://github.com/vincentzhang96/DDS4J)。
 */
@Suppress("unused")
object DdsToPngConverter {
	private val logger =  Logger.getInstance(MethodHandles.lookup().lookupClass())
	
	private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }
	private val ddsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //ddsAbsPath - pngAbsPath
	private val externalDdsCache: Cache<String, Path> by lazy { CacheBuilder.newBuilder().buildCache() } //ddsAbsPath - pngAbsPath
	
	/**
	 * 将DDS文件转化为PNG文件，返回PNG文件的绝对路径。如果发生异常，则返回null。
	 * @param ddsAbsPath DDS文件的绝对路径。
	 * @param ddsRelPath DDS文件相对于游戏或模组根路径的路径（如果可以获取）。
	 * @param frame 帧数，用于切割DDS图片。默认为0表示不切割。
	 */
	fun convert(ddsAbsPath: String, ddsRelPath: String? = null, frame: Int = 0): String? {
		try {
			//如果存在基于DDS文件绝对路径的缓存数据，则使用缓存的PNG文件绝对路径
			val pngAbsPath = getPngAbsPath(ddsAbsPath, ddsRelPath, frame)
			if(pngAbsPath.notExists()) {
				doConvertDdsToPng(ddsAbsPath, pngAbsPath, frame)
			}
			return pngAbsPath.absolutePathString()
		} catch(e: Exception) {
			logger.warn("Convert dds image to png image failed. (dds absolute path: $ddsAbsPath, dds relative path: $ddsRelPath, frame: $frame)", e)
			return null
		}
	}
	
	private fun getPngAbsPath(ddsAbsPath: String, ddsRelPath: String?, frame: Int): Path {
		val cache = if(ddsRelPath != null) ddsCache else externalDdsCache
		val cacheKey = getCacheKey(ddsAbsPath, frame)
		return cache.get(cacheKey) { doGetPngAbsPath(ddsAbsPath, ddsRelPath, frame) }
	}
	
	private fun doGetPngAbsPath(ddsAbsPath: String, ddsRelPath: String?, frame: Int): Path {
		val pngAbsPath = doGetRelatedPngPath(ddsAbsPath, ddsRelPath, frame)
		doConvertDdsToPng(ddsAbsPath, pngAbsPath, frame)
		return pngAbsPath
	}
	
	private fun doGetRelatedPngPath(ddsAbsPath: String, ddsRelPath: String?, frame: Int): Path {
		if(ddsRelPath != null) {
			//路径：~/.pls/images/${uuid}/${ddsRelPath}.png
			val uuid = ddsAbsPath.removeSuffix(ddsRelPath).toUUID().toString() //得到基于游戏或模组目录的绝对路径的UUID
			val frameText = if(frame > 0) "@$frame" else ""
			return PlsPaths.imagesDirectoryPath.resolve("$uuid/$ddsRelPath$frameText.png") //直接在包括扩展名的DDS文件名后面加上".png"
		} else {
			//路径：~/.pls/images/external/${uuid}/${ddsFileName}.png
			val index = ddsAbsPath.lastIndexOf('/')
			val parent = if(index == -1) "" else ddsAbsPath.substring(0, index)
			val fileName = if(index == -1) ddsAbsPath else ddsAbsPath.substring(index + 1)
			val uuid = if(parent.isEmpty()) "" else parent.toUUID().toString() //得到基于DDS文件所在目录的UUID
			val frameText = if(frame > 0) "@$frame" else ""
			return PlsPaths.imagesDirectoryPath.resolve("external/$uuid.$fileName$frameText.png") //直接在包括扩展名的DDS文件名后面加上".png"
		}
	}
	
	private fun doConvertDdsToPng(ddsAbsPath: String, pngAbsPath: Path, frame: Int) {
		val dds = Dds()
		dds.read(Files.newByteChannel(ddsAbsPath.toPath(), StandardOpenOption.READ))
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
	 * @param ddsAbsPath DDS文件的绝对路径。
	 * @param frame 帧数，用于切割DDS图片。默认为0表示不切割。
	 */
	fun invalidate(ddsAbsPath: String, frame: Int = 0) {
		val cacheKey = getCacheKey(ddsAbsPath, frame)
		ddsCache.invalidate(cacheKey)
		externalDdsCache.invalidate(cacheKey)
	}
	
	private fun getCacheKey(ddsAbsPath: String, frame: Int): String {
		if(frame > 0) {
			return "$ddsAbsPath@$frame"
		} else {
			return ddsAbsPath
		}
	}
	
	fun getUnknownPngPath(): String {
		if(PlsPaths.unknownPngPath.notExists()) {
			PlsPaths.unknownPngClasspathUrl.openStream().use { inputStream ->
				Files.copy(inputStream, PlsPaths.unknownPngPath) //将jar包中的unknown.png复制到~/.pls/images中
			}
		}
		return PlsPaths.unknownPngPath.toString()
	}
}
package icu.windea.pls.util

import com.intellij.util.io.*
import icu.windea.pls.*
import org.slf4j.*
import java.nio.file.*

/**
 * DDS格式文件转PNG格式文件的转换器。
 */
object DdsToPngConverter{
	private val logger = LoggerFactory.getLogger(DdsToPngConverter::class.java)
	
	private const val dds2PngDirName = "dds2png"
	private const val dds2PngExeName = "dds2png.exe"
	private const val dds2PngZipName = "dds2png.zip"
	private const val tmpDirName = "tmp"
	private const val unknownPngName = "unknown.png"
	
	private val userHome = System.getProperty("user.home")
	private val userHomePath = Path.of(userHome)
	private val dds2PngZipPath = userHomePath.resolve(dds2PngZipName)
	private val dds2PngDirPath = userHomePath.resolve(dds2PngDirName)
	private val dds2PngDirFile = dds2PngDirPath.toFile()
	private val dds2PngExePath = dds2PngDirPath.resolve(dds2PngExeName)
	private val tmpDirPath = dds2PngDirPath.resolve(tmpDirName)
	private val unknownPngPath = tmpDirPath.resolve(unknownPngName)
	private val rawUnknownPngUrl = "/$unknownPngName".toUrl(locationClass)
	private val rawDds2PngZipUrl = "/$dds2PngZipName".toUrl(locationClass)
	private val ddsExtensionRegex = "\\.dds".toRegex(RegexOption.IGNORE_CASE)
	
	/**
	 * 将dds文件转化为png文件，返回png文件的绝对路径。如果发生异常，则返回null
	 */
	fun convert(ddsAbsPath:String,ddsRelPath:String):String?{
		try {
			ensureDirsAndFilesExist()
			return doConvert(ddsAbsPath,ddsRelPath)	
		}catch(e:Exception){
			logger.warn("Convert dds image to png image failed.",e)
			return null
		}
	}
	
	/**
	 * * 检查目录`~/dds2png`和`~/dds2png/tmp`是否存在，如果不存在则创建。
	 * * 检查执行文件`~/dds2png/dds2png.exe`是否存在，如果不存在，则将jar包中的`dds2png.zip`提取并解压到用户目录。
	 */
	private fun ensureDirsAndFilesExist(){
		dds2PngDirPath.tryCreateDirectory()
		tmpDirPath.tryCreateDirectory()
		if(unknownPngPath.notExists()){
			Files.copy(rawUnknownPngUrl.openStream(), unknownPngPath) //将jar包中的unknown.png复制到~/dds2png/tmp下
			if(dds2PngExePath.notExists()){
				throw IllegalStateException("File '$unknownPngPath' is not exist after trying to extract '$unknownPngName' to tmp dir.")
			}
		}
		if(dds2PngExePath.notExists()) {
			Files.copy(rawDds2PngZipUrl.openStream(), dds2PngZipPath) //将jar包中的zip复制到用户目录
			ZipUtil.extract(dds2PngZipPath, dds2PngDirPath, null, true) //将zip解压到~/dds2png
			if(dds2PngExePath.notExists()){
				throw IllegalStateException("File '$dds2PngExePath' is not exist after trying to extract '$dds2PngZipName' to user home.")
			}
		}
	}
	
	private fun doConvert(ddsAbsPath: String,ddsRelPath: String):String{
		val pngAbsPath = tmpDirPath.resolve(ddsRelPath.replace(ddsExtensionRegex,".png"))
		pngAbsPath.parent.tryCreateDirectory() //确保png文件的父目录已经创建
		//~/dds2png/dds2png.exe -y <dds_name> <png_name>
		val command = ("dds2png -y ${ddsAbsPath.quote()} ${pngAbsPath.toString().quote()}")
		execBlocking(command, dds2PngDirFile)
		return pngAbsPath.toAbsolutePath().toString()
	}
	
	fun getUnknownPngPath():String{
		return unknownPngPath.toString()
	}
}
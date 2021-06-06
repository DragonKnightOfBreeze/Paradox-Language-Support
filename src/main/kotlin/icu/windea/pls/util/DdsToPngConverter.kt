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
	
	const val dds2PngDirName = "dds2png"
	const val dds2PngExeName = "dds2png.exe"
	const val dds2PngZipName = "dds2png.zip"
	const val tmpDirName = "tmp"
	
	val userHome = System.getProperty("user.home")
	val userHomePath = Path.of(userHome)
	val dds2PngDirPath = userHomePath.resolve(dds2PngDirName)
	val dds2PngDirFile = dds2PngDirPath.toFile()
	val dds2PngExePath = dds2PngDirPath.resolve(dds2PngExeName)
	val tmpDirPath = dds2PngDirPath.resolve(tmpDirName)
	val dds2PngZipUrl = dds2PngZipName.toUrl(locationClass)
	val dds2PngZipPath = dds2PngZipUrl.toPath()
	
	//使用在线图片
	const val unknownPngPath = "https://windea.icu/Paradox-Language-Support/assets/img/unknown.png"
	
	val ddsExtensionRegex = "\\.dds".toRegex(RegexOption.IGNORE_CASE) 
	
	/**
	 * 将dds文件转化为png文件，返回png文件的绝对路径。如果发生异常，则返回`unknown.png`的在线路径。
	 */
	fun convert(ddsAbsPath:String,ddsRelPath:String):String{
		try {
			ensureDirsAndFilesExist()
			return doConvert(ddsAbsPath,ddsRelPath)
		}catch(e:Exception){
			logger.error("Resolve dds image by convert it to png image failed, fallback to 'unknown.png'.",e.message)
			return unknownPngPath
		}
	}
	
	/**
	 * * 检查目录`~/dds2png`和`~/dds2png/tmp`是否存在，如果不存在则创建。
	 * * 检查执行文件`~/dds2png/dds2png.exe`是否存在，如果不存在，则将jar包中的`dds2png.zip`提取并解压到用户目录。
	 */
	private fun ensureDirsAndFilesExist(){
		dds2PngDirPath.tryCreateDirectory()
		tmpDirPath.tryCreateDirectory()
		if(!dds2PngExePath.exists()) {
			Files.createDirectories(dds2PngDirPath) //确保这个目录已经创建
			//TODO 可能需要一定时间，考虑并发执行
			ZipUtil.extract(dds2PngZipPath, dds2PngDirPath, null, true) //将zip解压到~/dds2png
			if(!dds2PngExePath.exists()){
				throw IllegalStateException("File $dds2PngExePath is not exist after trying to extract '$dds2PngZipName' to user home.")
			}
		}
	}
	
	private fun doConvert(ddsAbsPath: String,ddsRelPath: String):String{
		val pngAbsPath = tmpDirPath.resolve(ddsRelPath.replace(ddsExtensionRegex,".png"))
		val command = arrayOf(dds2PngExeName,"-y",ddsAbsPath.quote(),pngAbsPath.toString().quote())
		//TODO 可能需要一定时间，考虑并发执行
		execBlocking(command, dds2PngDirFile)
		return pngAbsPath.toAbsolutePath().toString()
	}
}
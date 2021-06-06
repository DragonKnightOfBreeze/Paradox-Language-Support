package icu.windea.pls.util

import com.intellij.util.io.*
import icu.windea.pls.*
import java.nio.file.*

/**
 * DDS格式文件转PNG格式文件的转换器。
 */
object DdsToPngConverter{
	val userHome = System.getProperty("user.home")
	val userHomePath = Path.of(userHome)
	val dds2PngDirPath = userHomePath.resolve("dds2png")
	val dds2PngExePath = dds2PngDirPath.resolve("dds2png.exe")
	val tmpPath = dds2PngDirPath.resolve("tmp")
	val unknownPngPath = ""
	val dds2PngZipUrl = "dds2png.zip".toUrl(locationClass)
	val dds2PngZipPath = dds2PngZipUrl.toPath()
	
	/**
	 * 将dds文件转化为png文件，返回png文件的绝对路径
	 */
	fun convert(ddsAbsPath:String):String{
		return ""
	}
	
	/**
	 * * 检查目录`~/dds2png`和`~/dds2png/tmp`是否存在，如果不存在则创建。
	 * * 检查文件`~/dds2png/tmp/unknown.png`是否存在，如果不存在则创建。
	 * * 检查执行文件`~/dds2png/dds2png.exe`是否存在，如果不存在，则将jar包中的`dds2png.zip`提取并解压到用户目录。
	 */
	private fun ensureDirsAndFilesExist(){
		dds2PngDirPath.tryCreateDirectory()
		tmpPath.tryCreateDirectory()
		if(!unknownPngPath.exists()){
			
		}
		if(!dds2PngExePath.exists()) {
			Files.createDirectories(dds2PngDirPath) //确保这个目录已经创建
			//TODO 可能需要一定时间，考虑并发执行
			ZipUtil.extract(dds2PngZipPath, dds2PngDirPath, null, true) //将zip解压到~/dds2png
			val nowExists = dds2PngExePath.exists()
		}
	}
}
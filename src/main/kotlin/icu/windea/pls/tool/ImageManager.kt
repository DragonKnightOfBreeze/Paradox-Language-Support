package icu.windea.pls.tool

import co.phoenixlab.dds.*
import java.io.*

object ImageManager {
	//可选方案：
	//DDS4J (https://github.com/vincentzhang96/DDS4J)
	//JOGL (https://jogamp.org/jogl/www/)
	//OPENRNDR (https://github.com/openrndr/openrndr/tree/master/openrndr-dds)
	
	//目前选用：
	//DDS4J - 简单易用
	
	private val ddsImageDecoder: DdsImageDecoder by lazy { DdsImageDecoder() }
	
	/**
	 * @param frame 帧数，用于切割DDS图片。默认为0，表示不切割。
	 */
	fun convertDdsToPng(inputStream: InputStream, frame: Int = 0): ByteArray? {
		val dds = Dds()
		dds.read(inputStream)
		return ddsImageDecoder.convertToPNG(dds, frame)
	}
	
	/**
	 * @param frame 帧数，用于切割DDS图片。默认为0，表示不切割。
	 */
	fun convertDdsToPng(inputStream: InputStream, outputStream: OutputStream, frame: Int = 0) {
		val dds = Dds()
		dds.read(inputStream)
		ddsImageDecoder.convertToPNG(dds, outputStream, frame)
	}
}
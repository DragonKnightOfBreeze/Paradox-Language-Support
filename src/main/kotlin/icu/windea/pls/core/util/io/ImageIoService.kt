package icu.windea.pls.core.util.io

import icu.windea.pls.core.util.ImageFrameInfo
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.min
import kotlin.math.roundToInt

object ImageIoService {
    fun isSameImageFormat(sourceFormat: String, targetFormat: String): Boolean {
        val sourceFormat = normalizeImageFormat(sourceFormat)
        val targetFormat = normalizeImageFormat(targetFormat)
        return sourceFormat.equals(targetFormat, ignoreCase = true)
    }

    fun normalizeImageFormat(sourceFormat: String): String {
        return sourceFormat.trim().removePrefix(".")
    }

    fun copyIfSameFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        if (!isSameImageFormat(sourceFormat, targetFormat)) return false
        IoService.copy(path, targetPath)
        return true
    }

    fun copyIfSameFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        if (!isSameImageFormat(sourceFormat, targetFormat)) return false
        IoService.copy(inputStream, outputStream)
        return true
    }

    /**
     * 读取图片（并进行额外的处理）。
     *
     * 备注：
     * - 会在读取的字节数达到阈值时进行取消检查。
     */
    @Suppress("unused")
    fun readImage(path: Path): BufferedImage {
        return path.inputStream().use { inputStream ->
            readImage(inputStream)
        }
    }

    /**
     * 读取图片（并进行额外的处理）。
     *
     * 备注：
     * - 会在读取的字节数达到阈值时进行取消检查。
     * - [inputStream] 不会被自动关闭。
     */
    fun readImage(inputStream: InputStream): BufferedImage {
        val inputStream = inputStream.asCancellable()
        return ImageIO.read(inputStream)
    }

    /**
     * 写入图片（并进行额外的处理）。
     *
     * 备注：
     * - 会在写入的字节数达到阈值时进行取消检查。
     * - 如果 [path] 的父目录未创建，则尝试创建。
     */
    fun writeImage(image: BufferedImage, format: String, path: Path): Boolean {
        path.parent?.createDirectories()
        return path.outputStream().use { outputStream -> writeImage(image, format, outputStream) }
    }

    /**
     * 写入图片（并进行额外的处理）。
     *
     * 备注：
     * - 会在写入的字节数达到阈值时进行取消检查。
     * - [outputStream] 不会被自动关闭。
     */
    fun writeImage(image: BufferedImage, format: String, outputStream: OutputStream): Boolean {
        val outputStream = outputStream.asCancellable()
        val result = ImageIO.write(image, format, outputStream)
        outputStream.flush() // it's better to flush explicitly here
        return result
    }

    /**
     * 转换图片格式（并进行额外的处理）。
     *
     * 备注：
     * - 会在读取/写入的字节数达到阈值时进行取消检查。
     */
    fun convertImageFormat(path: Path, targetPath: Path, targetFormat: String) {
        return path.inputStream().use { inputStream ->
            targetPath.outputStream().use { outputStream ->
                convertImageFormat(inputStream, outputStream, targetFormat)
            }
        }
    }

    /**
     * 转换图片格式（并进行额外的处理）。
     *
     * 备注：
     * - 会在读取/写入的字节数达到阈值时进行取消检查。
     * - [inputStream] 和 [outputStream] 不会被自动关闭。
     */
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, targetFormat: String) {
        val inputStream = inputStream.asCancellable()
        val outputStream = outputStream.asCancellable()
        ImageIO.createImageInputStream(inputStream)?.use { imageInputStream ->
            val imageReader = ImageIO.getImageReaders(imageInputStream).next()
            try {
                imageReader.input = imageInputStream
                val numImages = imageReader.getCompatibleNumImages()
                repeat(numImages) { i ->
                    val image0 = imageReader.read(i)
                    val image = image0.toCompatibleImage(targetFormat)
                    val written = writeImage(image, targetFormat, outputStream)
                    if (!written) throw IllegalStateException("No image writer found for format: $targetFormat")
                    outputStream.flush() // it's better to flush explicitly here
                }
            } finally {
                imageReader.dispose()
            }
        }
    }

    private fun ImageReader.getCompatibleNumImages(): Int {
        // NOTE 3.0.5 某些图片读取器（如 JPEG）在无法预先确定帧数时会返回 -1，此时至少转换第一帧
        return this.getNumImages(false).takeIf { it > 0 } ?: 1
    }

    private fun BufferedImage.toCompatibleImage(targetFormat: String? = null): BufferedImage {
        // NOTE 3.0.5 将图片转换为标准颜色模型，避免 JPEG 的 CMYK 等自定义颜色模型导致 `ImageIO.write` 静默失败。
        val targetWithoutAlpha = targetFormat.equals("jpg", true) || targetFormat.equals("jpeg", true)
        val type = if (!targetWithoutAlpha && this.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        if (this.type == type) return this
        val width = this.width
        val height = this.height
        @Suppress("UndesirableClassUsage") // NOTE 3.0.5 （目前认为）这里应当直接构造 `BufferedImage`，不进行缩放
        val result = BufferedImage(width, height, type)
        // val result = ImageUtil.createImage(width, height, type)
        val graphics = result.createGraphics()
        try {
            if (type == BufferedImage.TYPE_INT_RGB) {
                // 避免透明像素在转换为不支持 alpha 的格式时变成黑色
                @Suppress("UseJBColor")
                graphics.color = Color.WHITE
                graphics.fillRect(0, 0, width, height)
            }
            graphics.drawImage(this, 0, 0, null)
        } finally {
            graphics.dispose()
        }
        return result
    }

    fun scaleImage(image: BufferedImage, maxWidth: Int?, maxHeight: Int?): BufferedImage {
        if (maxWidth == null && maxHeight == null) return image
        val widthScale = maxWidth?.let { it.toDouble() / image.width } ?: 1.0
        val heightScale = maxHeight?.let { it.toDouble() / image.height } ?: 1.0
        val scale = min(1.0, min(widthScale, heightScale))
        if (scale >= 1.0) return image
        val type = BufferedImage.TYPE_INT_ARGB
        val width = (image.width * scale).roundToInt().coerceAtLeast(1)
        val height = (image.height * scale).roundToInt().coerceAtLeast(1)
        @Suppress("UndesirableClassUsage") // NOTE 3.0.5 （目前认为）这里应当直接构造 `BufferedImage`，不进行缩放
        val result = BufferedImage(width, height, type)
        // val result = ImageUtil.createImage(width, height, type)
        val graphics = result.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.drawImage(image, 0, 0, width, height, null)
        } finally {
            graphics.dispose()
        }
        return result
    }

    fun sliceImage(image: BufferedImage, frameInfo: ImageFrameInfo): BufferedImage? {
        if (!frameInfo.canApply()) return null
        val width = image.width
        val height = image.height
        val finalFrames = if (frameInfo.frames > 0) frameInfo.frames else width / height
        val finalFrame = if (frameInfo.frame > finalFrames) finalFrames else frameInfo.frame
        val frameWidth = width / finalFrames
        val startX = (finalFrame - 1) * frameWidth
        return image.getSubimage(startX, 0, frameWidth, height)
    }
}

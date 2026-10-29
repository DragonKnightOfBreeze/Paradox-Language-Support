package icu.windea.pls.core.util.io

import com.intellij.openapi.progress.ProgressManager
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

/**
 * 图片 IO 中进行取消检查的默认字节阈值。
 */
const val IMAGE_IO_CANCELLATION_CHECK_THRESHOLD = 64L * 1024

fun isSameImageFormat(sourceFormat: String, targetFormat: String): Boolean {
    return sourceFormat.trim().removePrefix(".").equals(targetFormat.trim().removePrefix("."), ignoreCase = true)
}

fun copyImageFile(sourcePath: Path, targetPath: Path) {
    val source = sourcePath.toAbsolutePath().normalize()
    val target = targetPath.toAbsolutePath().normalize()
    if (source == target) return
    target.parent?.createDirectories()
    Files.newInputStream(source).use { inputStream ->
        target.outputStream().use { outputStream ->
            copyImageStream(inputStream, outputStream)
        }
    }
}

fun copyImageStream(inputStream: InputStream, outputStream: OutputStream) {
    val progressInput = ProgressCheckingInputStream(inputStream)
    val progressOutput = ProgressCheckingOutputStream(outputStream)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val n = progressInput.read(buffer)
        if (n < 0) break
        progressOutput.write(buffer, 0, n)
    }
    progressOutput.flush()
}

fun ImageReader.getCompatibleNumImages(): Int {
    // NOTE 3.0.5 某些图片读取器（如 JPEG）在无法预先确定帧数时会返回 -1，此时至少转换第一帧
    return this.getNumImages(false).takeIf { it > 0 } ?: 1
}

/**
 * 读取图片，并在读取的字节数达到阈值时进行取消检查。
 */
fun readImage(path: Path, threshold: Long = IMAGE_IO_CANCELLATION_CHECK_THRESHOLD): BufferedImage? {
    Files.newInputStream(path).use { inputStream ->
        ProgressCheckingInputStream(inputStream, threshold).use { progressInput ->
            ImageIO.createImageInputStream(progressInput)?.use { imageInputStream ->
                val imageReader = ImageIO.getImageReaders(imageInputStream).asSequence().firstOrNull() ?: return null
                try {
                    imageReader.input = imageInputStream
                    return imageReader.read(0)
                } finally {
                    imageReader.dispose()
                }
            }
        }
    }
    return null
}

/**
 * 写入图片，并在写入的字节数达到阈值时进行取消检查。
 */
fun writeImage(
    image: BufferedImage,
    format: String,
    path: Path,
    threshold: Long = IMAGE_IO_CANCELLATION_CHECK_THRESHOLD,
): Boolean {
    path.parent?.createDirectories()
    Files.newOutputStream(path).use { outputStream ->
        ProgressCheckingOutputStream(outputStream, threshold).use { progressOutput ->
            val imageWriter = ImageIO.getImageWritersByFormatName(format).asSequence().firstOrNull() ?: return false
            try {
                ImageIO.createImageOutputStream(progressOutput)?.use { imageOutputStream ->
                    imageWriter.output = imageOutputStream
                    imageWriter.write(image)
                    return true
                }
            } finally {
                imageWriter.dispose()
            }
        }
    }
    return false
}

fun BufferedImage.toCompatibleImage(targetFormat: String? = null): BufferedImage {
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
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)
        }
        graphics.drawImage(this, 0, 0, null)
    } finally {
        graphics.dispose()
    }
    return result
}

/**
 * 在读取的字节数达到阈值时进行取消检查。
 */
internal class ProgressCheckingInputStream(
    inputStream: InputStream,
    private val threshold: Long = IMAGE_IO_CANCELLATION_CHECK_THRESHOLD,
    private val checkCanceled: () -> Unit = { ProgressManager.checkCanceled() },
) : FilterInputStream(inputStream) {
    private var checkedBytes = 0L

    override fun read(): Int {
        val result = super.read()
        if (result >= 0) checkAfterRead(1)
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = super.read(b, off, len)
        if (result > 0) checkAfterRead(result.toLong())
        return result
    }

    override fun skip(n: Long): Long {
        val result = super.skip(n)
        if (result > 0) checkAfterRead(result)
        return result
    }

    private fun checkAfterRead(count: Long) {
        checkedBytes += count
        if (checkedBytes >= threshold.coerceAtLeast(1)) {
            checkedBytes = 0
            checkCanceled()
        }
    }
}

/**
 * 在写入的字节数达到阈值时进行取消检查。
 */
internal class ProgressCheckingOutputStream(
    outputStream: OutputStream,
    private val threshold: Long = IMAGE_IO_CANCELLATION_CHECK_THRESHOLD,
    private val checkCanceled: () -> Unit = { ProgressManager.checkCanceled() },
) : FilterOutputStream(outputStream) {
    private var checkedBytes = 0L

    override fun write(b: Int) {
        out.write(b)
        checkAfterWrite(1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        checkAfterWrite(len.toLong())
    }

    private fun checkAfterWrite(count: Long) {
        checkedBytes += count
        if (checkedBytes >= threshold.coerceAtLeast(1)) {
            checkedBytes = 0
            checkCanceled()
        }
    }
}

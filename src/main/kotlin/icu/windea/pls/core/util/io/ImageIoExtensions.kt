package icu.windea.pls.core.util.io

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageReader
import kotlin.io.path.createDirectories

fun isSameImageFormat(sourceFormat: String, targetFormat: String): Boolean {
    return sourceFormat.trim().removePrefix(".").equals(targetFormat.trim().removePrefix("."), ignoreCase = true)
}

fun copyImageFile(sourcePath: Path, targetPath: Path) {
    val source = sourcePath.toAbsolutePath().normalize()
    val target = targetPath.toAbsolutePath().normalize()
    if (source == target) return
    target.parent?.createDirectories()
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
}

fun copyImageStream(inputStream: InputStream, outputStream: OutputStream) {
    inputStream.copyTo(outputStream)
    outputStream.flush()
}

fun ImageReader.getCompatibleNumImages(): Int {
    // NOTE 3.0.5 某些图片读取器（如 JPEG）在无法预先确定帧数时会返回 -1，此时至少转换第一帧
    return this.getNumImages(false).takeIf { it > 0 } ?: 1
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

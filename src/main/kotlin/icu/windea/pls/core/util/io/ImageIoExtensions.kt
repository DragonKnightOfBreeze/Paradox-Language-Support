package icu.windea.pls.core.util.io

import java.awt.image.BufferedImage
import javax.imageio.ImageReader

fun ImageReader.getCompatibleNumImages(): Int {
    // NOTE 3.0.5 某些图片读取器（如 JPEG）在无法预先确定帧数时会返回 -1，此时至少转换第一帧
    return this.getNumImages(false).takeIf { it > 0 } ?: 1
}

fun BufferedImage.toCompatibleImage(): BufferedImage {
    // NOTE 3.0.5 将图片转换为标准颜色模型，避免 JPEG 的 CMYK 等自定义颜色模型导致 `ImageIO.write` 静默失败。
    val type = if (this.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
    if (this.type == type) return this
    val width = this.width
    val height = this.height
    @Suppress("UndesirableClassUsage") // NOTE 3.0.5 （目前认为）这里应当直接构造 `BufferedImage`，不进行缩放
    val result = BufferedImage(width, height, type)
    // val result = ImageUtil.createImage(width, height, type)
    val graphics = result.createGraphics()
    try {
        graphics.drawImage(this, 0, 0, null)
    } finally {
        graphics.dispose()
    }
    return result
}

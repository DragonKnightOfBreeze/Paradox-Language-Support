package icu.windea.pls.images

import com.intellij.openapi.project.Project
import icu.windea.pls.core.util.ImageFrameInfo
import java.awt.Image
import java.nio.file.Path
import javax.swing.Icon

/**
 * 图片处理策略。
 */
enum class ImageStrategy {
    /** 直接使用源图片，无需切分、缩放或格式转换。 */
    Direct,

    /** 需要经由渲染管道派生到目标缓存路径（切分、缩放或格式转换）。 */
    Delegated,
}

/**
 * 图片描述符。
 *
 * 统一描述图片的来源、预期的缓存路径以及渲染所需的附加信息。
 * 该描述符只描述图片来源与预期的缓存路径，不会写入文件或执行外部工具；
 * 实际的渲染与 URL 决策由 [ImageRenderService] 负责。
 *
 * @property sourcePath 源图片路径。
 * @property targetPath 渲染后的缓存路径；不需要渲染时等于 [sourcePath]。
 * @property frameInfo 实际生效的帧数信息，用于切分图片。
 * @property maxWidth 目标最大宽度；为空时不限制。
 * @property maxHeight 目标最大高度；为空时不限制。
 */
data class ImageDescriptor(
    val sourcePath: Path,
    val targetPath: Path = sourcePath,
    val frameInfo: ImageFrameInfo? = null,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null,
) {
    /**
     * 图片处理策略，由 [targetPath] 是否与 [sourcePath] 一致推导得到。
     */
    val strategy: ImageStrategy
        get() = if (targetPath == sourcePath) ImageStrategy.Direct else ImageStrategy.Delegated

    /**
     * 是否需要执行渲染（切分、缩放或格式转换）。
     */
    val renderRequired: Boolean
        get() = strategy == ImageStrategy.Delegated

    /**
     * 源图片的格式（文件扩展名，统一为小写），不包含点号。
     */
    val sourceFormat: String
        get() = sourcePath.fileName.toString().substringAfterLast('.', "").lowercase()

    /**
     * 目标图片的格式。直接使用源图片时为 [sourceFormat]；否则渲染结果固定为 `png`。
     */
    val targetFormat: String
        get() = if (strategy == ImageStrategy.Direct) sourceFormat else "png"

    /**
     * @see ImageRenderService.canResolve
     */
    fun canResolve(): Boolean = ImageRenderService.getInstance().canResolve(this)

    /**
     * @see ImageRenderService.resolveUrl
     */
    fun resolveUrl(project: Project): String? = ImageRenderService.getInstance().resolveUrl(this, project)

    /**
     * @see ImageRenderService.createImage
     */
    fun createImage(heightLimit: Int? = null): Image? = ImageRenderService.getInstance().createImage(this, heightLimit)

    /**
     * @see ImageRenderService.createImageIcon
     */
    fun createImageIcon(heightLimit: Int? = null): Icon? = ImageRenderService.getInstance().createImageIcon(this, heightLimit)
}

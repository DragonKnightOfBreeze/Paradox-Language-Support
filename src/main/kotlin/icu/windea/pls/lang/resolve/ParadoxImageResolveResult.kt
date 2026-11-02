@file:Suppress("unused")

package icu.windea.pls.lang.resolve

import com.intellij.openapi.project.Project
import icu.windea.pls.core.util.ImageFrameInfo
import icu.windea.pls.images.ImageRenderRequest
import icu.windea.pls.images.ImageRenderService
import java.awt.Image
import java.nio.file.Path
import javax.swing.Icon

/**
 * 语义层的图片解析结果。
 *
 * 该结果只描述图片来源与预期的缓存路径，不会写入文件或执行外部工具。
 *
 * @property sourcePath 源图片路径。
 * @property targetPath 渲染后的缓存路径；不需要渲染时等于 [sourcePath]。
 * @property frameInfo 实际生效的帧数信息。
 * @property maxWidth 目标最大宽度；为空时不限制。
 * @property maxHeight 目标最大高度；为空时不限制。
 */
class ParadoxImageResolveResult(
    val sourcePath: Path,
    val targetPath: Path,
    val frameInfo: ImageFrameInfo? = null,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null,
) {
    /**
     * 是否需要执行渲染（切分、缩放或格式转换）。
     */
    val renderRequired: Boolean
        get() = targetPath != sourcePath

    /**
     * 转换为渲染请求。
     */
    fun toRenderRequest(): ImageRenderRequest {
        return ImageRenderRequest(sourcePath, targetPath, frameInfo, maxWidth, maxHeight)
    }

    /**
     * @see ParadoxImageService.canResolve
     */
    fun canResolve(): Boolean {
        return ParadoxImageService.canResolve(this)
    }

    /**
     * @see ParadoxImageService.resolveUrl
     */
    fun resolveUrl(project: Project): String? {
        return ParadoxImageService.resolveUrl(this, project)
    }

    /**
     * @see ImageRenderService.createImage
     */
    fun createImage(heightLimit: Int? = null): Image? {
        return ImageRenderService.getInstance().createImage(toRenderRequest(), heightLimit)
    }

    /**
     * @see ImageRenderService.createImageIcon
     */
    fun createImageIcon(heightLimit: Int? = null): Icon? {
        return ImageRenderService.getInstance().createImageIcon(toRenderRequest(), heightLimit)
    }
}

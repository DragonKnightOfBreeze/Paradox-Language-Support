package icu.windea.pls.lang.util

import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.images.ImageRenderRequest
import java.nio.file.Path

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
     * 转换为基础设施层的渲染请求。
     */
    fun toRenderRequest(): ImageRenderRequest {
        return ImageRenderRequest(sourcePath, targetPath, frameInfo, maxWidth, maxHeight)
    }
}

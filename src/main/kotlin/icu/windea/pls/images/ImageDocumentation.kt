package icu.windea.pls.images

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * 构造扩展图片文件（DDS/TGA）在快速文档中展示的尺寸提示（tailText），格式为 ` ({width}x{height})`。
 *
 * 仅适用于插件扩展的图片类型；PNG 等普通图片由 IntelliJ 官方的图片支持处理，因此不重复追加。
 * 这里复用 [ImageMetadataService] 只读取图片的轻量元数据，不做无谓的全图解码。
 *
 * @see ImageService.isExtendedImageFileType
 */
fun VirtualFile.getExtendedImageDimensionTailText(project: Project? = null): String? {
    // 参考官方实现：`com.intellij.psi.impl.source.resolve.reference.impl.providers.FileInfoManager` - 以 ` ({width}x{height})` 作为 tailText
    // 参考官方实现：`org.intellij.images.completion.ImageLookupInfoProvider` - 提供 `{width}x{height}` 形式的尺寸信息
    if (!ImageService.getInstance().isExtendedImageFileType(fileType)) return null
    val metadata = ImageMetadataService.getInstance().getMetadataOrNull(this, project) ?: return null
    return " (${metadata.width}x${metadata.height})"
}

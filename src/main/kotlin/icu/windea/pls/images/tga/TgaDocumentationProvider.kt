package icu.windea.pls.images.tga

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import icu.windea.pls.base.settings.ChroniclePresentationSettings
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.core.codeInsight.documentation.buildDocumentation
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.images.getExtendedImageDimensionTailText

// org.intellij.images.fileTypes.ImageDocumentationProvider

class TgaDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element !is PsiFileSystemItem || element.isDirectory) return null
        val project = element.project
        if (DumbService.isDumb(project)) return null
        val file = element.virtualFile
        val metadata = runSmartReadAction { TgaMetadataIndex.getInstance().getMetadata(file, project) }
        if (metadata == null) return null
        val tailText = runSmartReadAction { file.getExtendedImageDimensionTailText(project) }
        return buildDocumentation { buildDoc(file, metadata, tailText) }.toString()
    }

    private fun DocumentationBuilder.buildDoc(file: VirtualFile, metadata: TgaMetadata, tailText: String?) {
        // 加入图片文件名及尺寸提示（tailText，格式为 ` ({width}x{height})`）
        run {
            definition {
                append(file.name.escapeXml())
                if (tailText != null) append(tailText)
            }
        }
        // 加入用于渲染的图片的标签
        run {
            val maxSize = maxOf(metadata.width, metadata.height)
            val maxImageSize = ChroniclePresentationSettings.getInstance().state.quickDocMaxImageDisplaySize
            val scaleFactor = if (maxSize > maxImageSize) maxImageSize.toDouble() / maxSize.toDouble() else 1.0
            val imageWidth = (metadata.width * scaleFactor).toInt()
            val imageHeight = (metadata.height * scaleFactor).toInt()
            val url = file.toNioPath().toUri().toString()
            val imgTag = HtmlChunk.tag("img").attr("src", url).attr("width", imageWidth).attr("height", imageHeight)
            append(imgTag)
        }
        // 加入图片的元数据信息
        run {
            val message = buildString {
                append(metadata.width).append("\u00D7").append(metadata.height)
                append(", ").append(metadata.bpp).append("bpp")
            }
            append(HtmlChunk.p().addText(message))
        }
    }
}

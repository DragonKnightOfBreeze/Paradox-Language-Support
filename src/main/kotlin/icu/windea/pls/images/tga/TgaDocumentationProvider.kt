package icu.windea.pls.images.tga

import com.intellij.lang.documentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.lang.settings.PlsInternalSettings

//org.intellij.images.fileTypes.ImageDocumentationProvider

class TgaDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element !is PsiFileSystemItem || element.isDirectory) return null
        val project = element.project
        if (DumbService.isDumb(project)) return null
        val file = element.virtualFile
        val metadata = runReadAction { service<TgaMetadataIndex>().getMetadata(file, project) }
        if (metadata == null) return null

        return buildDocumentation {
            //加入用于渲染的图片的标签
            run {
                val maxSize = maxOf(metadata.width, metadata.height)
                val maxImageSize = PlsInternalSettings.maxImageSizeInDocumentation
                val scaleFactor = if (maxSize > maxImageSize) maxImageSize.toDouble() / maxSize.toDouble() else 1.0
                val imageWidth = (metadata.width * scaleFactor).toInt()
                val imageHeight = (metadata.height * scaleFactor).toInt()
                val url = file.toNioPath().toUri().toString()
                val imgTag = HtmlChunk.tag("img").attr("src", url).attr("width", imageWidth).attr("height", imageHeight)
                append(imgTag)
            }
            //加入图片的元数据信息
            run {
                val message = buildString {
                    append(metadata.width).append("x").append(metadata.height)
                    append(", ").append(metadata.bpp)
                }
                append(HtmlChunk.p().addText(message))
            }
        }
    }
}

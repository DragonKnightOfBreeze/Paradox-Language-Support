package icu.windea.pls.images.dds

import com.intellij.lang.documentation.*
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.image.*

//org.intellij.images.fileTypes.ImageDocumentationProvider

class DdsDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element !is PsiFileSystemItem || element.isDirectory) return null
        val project = element.project
        if (DumbService.isDumb(project)) return null
        val file = element.virtualFile
        val metadata = runReadAction {  service<DdsMetadataIndex>().getMetadata(file, project) }
        if (metadata == null) return null

        return buildDocumentation {
            //加入用于渲染的图片标签
            run {
                val pngUrl = ParadoxImageResolver.resolveUrlByFile(file, project)
                if (pngUrl == null) return@run
                val maxSize = maxOf(metadata.width, metadata.height)
                val maxImageSize = PlsInternalSettings.maxImageSizeInDocumentation
                val scaleFactor = if (maxSize > maxImageSize) maxImageSize.toDouble() / maxSize.toDouble() else 1.0
                val imageWidth = (metadata.width * scaleFactor).toInt()
                val imageHeight = (metadata.height * scaleFactor).toInt()
                val url = pngUrl.toFileUrl().toString()
                val imgTag = HtmlChunk.tag("img").attr("src", url).attr("width", imageWidth).attr("height", imageHeight)
                append(imgTag)
            }
            //加入图片元数据信息
            run {
                val info = buildString {
                    append(metadata.width).append("x").append(metadata.height)
                    append(", ").append(metadata.d3dFormat?.orNull() ?: "UNKNOWN")
                    append(", ").append(metadata.dxgiFormat?.orNull() ?: "UNKNOWN")
                }
                append(HtmlChunk.p().addText(info))
            }
        }
    }
}

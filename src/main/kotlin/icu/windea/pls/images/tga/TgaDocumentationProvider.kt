package icu.windea.pls.images.tga

import com.intellij.lang.documentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import icu.windea.pls.core.documentation.*
import org.intellij.images.*

//org.intellij.images.fileTypes.ImageDocumentationProvider

class TgaDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element !is PsiFileSystemItem || element.isDirectory) return null
        val project = element.project
        if (DumbService.isDumb(project)) return null
        val file = element.virtualFile

        return buildDocumentation {
            //加入图片元数据信息
            run {
                val metadata = runReadAction { service<TgaMetadataIndex>().getMetadata(file, project) }
                if (metadata == null) return@run
                val message = ImagesBundle.message("image.description", metadata.width, metadata.height, metadata.bpp)
                append(HtmlChunk.p().addText(message))
            }
        }
    }
}

package icu.windea.pls.dds

import com.intellij.lang.documentation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.lang.util.image.*

//org.intellij.images.fileTypes.ImageDocumentationProvider

class DdsDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element !is PsiFileSystemItem || element.isDirectory) return null
        if (DumbService.isDumb(element.project)) return null

        val file = element.virtualFile
        val project = element.project
        val pngUrl = ParadoxImageResolver.resolveUrlByFile(file, project) ?: return null //无法将DDS转换成PNG时直接返回
        val metadata = DdsMetadataIndex.getMetadata(file, project)
        return buildDocumentation {
            append(HtmlChunk.tag("img").attr("src", pngUrl.toFileUrl().toString()))
            if (metadata != null) {
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

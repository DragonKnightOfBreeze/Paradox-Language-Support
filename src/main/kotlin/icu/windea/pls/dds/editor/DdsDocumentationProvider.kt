package icu.windea.pls.dds.editor

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.dds.*
import icu.windea.pls.util.*

//org.intellij.images.fileTypes.ImageDocumentationProvider

class DdsDocumentationProvider : DocumentationProvider {
	override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
		if(element is PsiFileSystemItem && !element.isDirectory) {
			val file = element.virtualFile
			if(!DumbService.isDumb(element.project)) {
				val info = DdsInfoIndex.getInfo(file, element.project)
				val width = info?.width ?: 0
				val height = info?.height ?: 0
				try {
					val url = ParadoxDdsUrlResolver.resolveByFile(file)
					if(url.isEmpty()) return null //无法将DDS转换成PNG时直接返回
					//如果能获取图片大小就显示出来，否则不显示
					val canGetInfo = width != 0 && height != 0
					val message = if(canGetInfo) PlsBundle.message("dds.description", width, height) else null
					val img = HtmlChunk.tag("img").attr("src", url.toFileUrl().toString())
					val builder = HtmlBuilder().append(img)
					if(message != null) builder.append(HtmlChunk.p().addText(message))
					return builder.toString()
				} catch(e: Exception) {
					// nothing
				}
			}
		}
		return null
	}
}
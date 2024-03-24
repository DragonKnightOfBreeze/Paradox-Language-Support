package icu.windea.pls.dds.editor

import com.intellij.lang.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.util.image.*

//org.intellij.images.fileTypes.ImageDocumentationProvider

class DdsDocumentationProvider : AbstractDocumentationProvider() {
	override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
		if(element is PsiFileSystemItem && !element.isDirectory) {
			val file = element.virtualFile
			if(DumbService.isDumb(element.project)) return null
			val info = DdsInfoIndex.getInfo(file, element.project)
			val width = info?.width ?: 0
			val height = info?.height ?: 0
			try {
				val url = ParadoxImageResolver.resolveUrlByFile(file) ?: return null //无法将DDS转换成PNG时直接返回
				//如果能获取图片大小就显示出来，否则不显示
				val canGetInfo = width != 0 && height != 0
				val message = if(canGetInfo) PlsBundle.message("dds.description", width, height) else null
				val img = HtmlChunk.tag("img").attr("src", url.toFileUrl().toString())
				val builder = HtmlBuilder().append(img)
				if(message != null) builder.append(HtmlChunk.p().addText(message))
				return builder.toString()
			} catch(e: Exception) {
				if(e is ProcessCanceledException) throw e
				// nothing
			}
		}
		return null
	}
}
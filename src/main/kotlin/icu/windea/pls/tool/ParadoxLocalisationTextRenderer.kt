package icu.windea.pls.tool

import com.intellij.psi.PsiFile
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 本地化文本的渲染器。
 */
@Suppress("unused")
object ParadoxLocalisationTextRenderer {
	fun render(element: ParadoxLocalisationProperty): String {
		return buildString { renderTo(element, this) }
	}
	
	fun renderTo(element: ParadoxLocalisationProperty, builder: StringBuilder) {
		element.propertyValue?.richTextList?.forEach { renderTo(it, builder) }
	}
	
	private fun renderTo(element: ParadoxLocalisationRichText, builder: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, builder)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, builder)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, builder)
			is ParadoxLocalisationIcon -> renderIconTo(element, builder)
			is ParadoxLocalisationSequentialNumber -> renderSequentialNumberTo(element, builder)
			is ParadoxLocalisationCommand -> renderCodeTo(element, builder)
			is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, builder)
		}
	}
	
	private fun renderStringTo(element: ParadoxLocalisationString, builder: StringBuilder) {
		builder.append(element.text.escapeXml())
	}
	
	private fun renderEscapeTo(element: ParadoxLocalisationEscape, builder: StringBuilder) {
		val elementText = element.text
		when {
			elementText == "\\n" -> builder.append("<br>\n")
			elementText == "\\t" -> builder.append("&emsp;")
			elementText.length > 1 -> builder.append(elementText[1])
		}
	}
	
	private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, builder: StringBuilder) {
		val reference = element.reference
		val rgbText = element.colorConfig?.colorText
		if(reference != null) {
			val property = reference.resolve() as? ParadoxLocalisationProperty
			if(property != null) {
				if(rgbText != null) builder.append("<span style=\"color: ").append(rgbText).append("\">")
				renderTo(property, builder)
				if(rgbText != null) builder.append("</span>")
				return
			}
		}
		//如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
		if(rgbText != null) {
			builder.append("<code style=\"color: ").append(rgbText).append("\">").append(element.text).append("</code>")
		} else {
			builder.append("<code>").append(element.text).append("</code>")
		}
	}
	
	private fun renderIconTo(element: ParadoxLocalisationIcon, builder: StringBuilder) {
		val resolved = element.reference?.resolve() ?: return
		val iconUrl = when {
			resolved is ParadoxDefinitionProperty -> ParadoxDdsUrlResolver.resolveBySprite(resolved, defaultToUnknown = true)
			resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, defaultToUnknown = true)
			else -> return
		}
		if(iconUrl.isNotEmpty()) {
			builder.appendImgTag(iconUrl, fontSize) //指定图标大小为fontSize
		}
	}
	
	private fun renderSequentialNumberTo(element: ParadoxLocalisationSequentialNumber, builder: StringBuilder) {
		val placeholderText = element.sequentialNumberConfig?.placeholderText
		if(placeholderText != null) {
			builder.append(placeholderText)
			return
		}
		//如果处理文本失败，则直接使用原始文本
		builder.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderCodeTo(element: ParadoxLocalisationCommand, builder: StringBuilder) {
		//使用原始文本
		builder.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, builder: StringBuilder) {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val rgbText = element.colorConfig?.colorText
		if(rgbText != null) builder.append("<span style=\"color: ").append(rgbText).append("\">")
		for(v in element.richTextList) {
			renderTo(v, builder)
		}
		if(rgbText != null) builder.append("</span>")
	}
}

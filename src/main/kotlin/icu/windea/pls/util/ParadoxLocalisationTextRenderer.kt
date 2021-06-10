package icu.windea.pls.util

import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化文本的渲染器。
 */
object ParadoxLocalisationTextRenderer {
	fun render(element: ParadoxLocalisationProperty): String {
		return buildString { renderTo(element, this) }
	}
	
	fun renderTo(element: ParadoxLocalisationProperty, buffer: StringBuilder) {
		element.propertyValue?.richTextList?.forEach { renderTo(it, buffer) }
	}
	
	private fun renderTo(element: ParadoxLocalisationRichText, buffer: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, buffer)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, buffer)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, buffer)
			is ParadoxLocalisationIcon -> renderIconTo(element, buffer)
			is ParadoxLocalisationSequentialNumber -> renderSequentialNumberTo(element, buffer)
			is ParadoxLocalisationCommand -> renderCodeTo(element, buffer)
			is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, buffer)
		}
	}
	
	private fun renderStringTo(element: ParadoxLocalisationString, buffer: StringBuilder) {
		buffer.append(element.text.escapeXml())
	}
	
	private fun renderEscapeTo(element: ParadoxLocalisationEscape, buffer: StringBuilder) {
		val elementText = element.text
		when {
			elementText == "\\n" -> buffer.append("<br>\n")
			elementText == "\\t" -> buffer.append("&emsp;")
			elementText.length > 1 -> buffer.append(elementText[1])
		}
	}
	
	private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, buffer: StringBuilder) {
		val reference = element.reference
		val rgbText = element.paradoxColor?.colorText
		if(reference != null) {
			val property = reference.resolve() as? ParadoxLocalisationProperty
			if(property != null) {
				if(rgbText != null) buffer.append("<span style=\"color: ").append(rgbText).append("\">")
				renderTo(property, buffer)
				if(rgbText != null) buffer.append("</span>")
				return
			}
		}
		//如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，保留颜色码
		if(rgbText != null) {
			buffer.append("<code style=\"color: ").append(rgbText).append("\">").append(element.text).append("</code>")
		} else {
			buffer.append("<code>").append(element.text).append("</code>")
		}
	}
	
	private fun renderIconTo(element: ParadoxLocalisationIcon, buffer: StringBuilder) {
		val name = element.name
		val iconUrl = name.resolveIconUrl(element.project)
		if(iconUrl.isNotEmpty()) {
			buffer.appendIconTag(iconUrl)
		}
	}
	
	private fun renderSequentialNumberTo(element: ParadoxLocalisationSequentialNumber, buffer: StringBuilder) {
		val placeholderText = element.paradoxSequentialNumber?.placeholderText
		if(placeholderText != null) {
			buffer.append(placeholderText)
			return
		}
		//如果处理文本失败，则直接使用原始文本
		buffer.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderCodeTo(element: ParadoxLocalisationCommand, buffer: StringBuilder) {
		//使用原始文本
		buffer.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, buffer: StringBuilder) {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val rgbText = element.paradoxColor?.colorText
		if(rgbText != null) buffer.append("<span style=\"color: ").append(rgbText).append("\">")
		for(v in element.richTextList) {
			renderTo(v, buffer)
		}
		if(rgbText != null) buffer.append("</span>")
	}
}

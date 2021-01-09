package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

/**
 * 富文本的渲染器。
 *
 * 基于类型和颜色渲染富文本。
 */
object ParadoxRichTextRenderer {
	fun render(element:ParadoxLocalisationPropertyValue):String{
		val buffer = StringBuilder()
		renderTo(element,buffer)
		return buffer.toString()
	}
	
	fun renderTo(element: ParadoxLocalisationPropertyValue, buffer: StringBuilder) {
		try {
			element.richTextList.forEach { renderTo(it, buffer) }
		} catch(e: Exception) {
			e.printStackTrace()
			buffer.append("<code>(syntax error)</code>")
		}
	}
	
	private fun renderTo(element: ParadoxLocalisationRichText, buffer: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> renderStringTo(element,buffer)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, buffer)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, buffer)
			is ParadoxLocalisationIcon -> renderIconTo(element,buffer)
			is ParadoxLocalisationSerialNumber -> renderSerialNumberTo(element, buffer)
			is ParadoxLocalisationCommand -> renderCodeTo(element,buffer)
			is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, buffer)
		}
	}
	
	private fun renderStringTo(element:ParadoxLocalisationString,buffer: StringBuilder){
		buffer.append(element.text.escapeXml())
	}
	
	private fun renderEscapeTo(element: ParadoxLocalisationEscape, buffer: StringBuilder) {
		val elementText = element.text
		when {
			elementText == "\\n" -> buffer.append("<br>")
			elementText == "\\t" -> buffer.append("&emsp;")
			elementText.length > 1 -> buffer.append(elementText[1])
		}
	}
	
	private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, buffer: StringBuilder) {
		val reference = element.reference
		if(reference != null) {
			val resolve = reference.resolve() as? ParadoxLocalisationProperty
			if(resolve != null) {
				val propertyValue = resolve.propertyValue
				if(propertyValue != null) {
					val rgbText = element.paradoxColor?.colorText
					if(rgbText != null) buffer.append("<span style='color: $rgbText;'>")
					renderTo(propertyValue, buffer)
					if(rgbText != null) buffer.append("</span>")
					return
				}
			}
		}
		//如果解析引用失败，则直接使用原始文本
		buffer.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderIconTo(element: ParadoxLocalisationIcon, buffer: StringBuilder) {
		val name = element.name
		val iconUrl = name.resolveIconUrl()
		if(iconUrl.isNotEmpty()) {
			buffer.appendIconTag(iconUrl)
		}
	}
	
	private fun renderSerialNumberTo(element: ParadoxLocalisationSerialNumber, buffer: StringBuilder) {
		val placeholderText = element.paradoxSerialNumber?.placeholderText
		if(placeholderText != null) {
			buffer.append(placeholderText)
			return
		}
		//如果解析引用失败，则直接使用原始文本
		buffer.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderCodeTo(element: ParadoxLocalisationCommand, buffer: StringBuilder) {
		//使用原始文本
		buffer.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, buffer: StringBuilder) {
		//如果解析引用失败，则清除非法的标记，直接渲染其中的富文本
		val rgbText = element.paradoxColor?.colorText
		if(rgbText != null) buffer.append("<span style='color: $rgbText;'>")
		for(v in element.richTextList) {
			renderTo(v, buffer)
		}
		if(rgbText != null) buffer.append("</span>")
	}
}

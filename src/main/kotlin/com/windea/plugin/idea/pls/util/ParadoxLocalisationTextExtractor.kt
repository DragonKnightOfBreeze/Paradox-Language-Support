package com.windea.plugin.idea.pls.util

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.localisation.psi.*

/**
 * 本地化文本的提取器。
 */
object ParadoxLocalisationTextExtractor {
	fun extract(element: ParadoxLocalisationProperty): String {
		return buildString { extractTo(element, this) }
	}
	
	fun extractTo(element: ParadoxLocalisationProperty, buffer: StringBuilder) {
		element.propertyValue?.richTextList?.forEach { extractTo(it, buffer) }
	}
	
	private fun extractTo(element: ParadoxLocalisationRichText, buffer: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> extractStringTo(element, buffer)
			is ParadoxLocalisationEscape -> extractEscapeTo(element, buffer)
			is ParadoxLocalisationPropertyReference -> extractPropertyReferenceTo(element, buffer)
			is ParadoxLocalisationIcon -> extractIconTo(element, buffer)
			is ParadoxLocalisationSequentialNumber -> extractSequentialNumberTo(element, buffer)
			is ParadoxLocalisationCommand -> extractCodeTo(element, buffer)
			is ParadoxLocalisationColorfulText -> extractColorfulTextTo(element, buffer)
		}
	}
	
	private fun extractStringTo(element: ParadoxLocalisationString, buffer: StringBuilder) {
		buffer.append(element.text)
	}
	
	private fun extractEscapeTo(element: ParadoxLocalisationEscape, buffer: StringBuilder) {
		val elementText = element.text
		when {
			elementText == "\\n" -> buffer.append("\n")
			elementText == "\\t" -> buffer.append("\t")
			elementText.length > 1 -> buffer.append(elementText[1])
		}
	}
	
	private fun extractPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, buffer: StringBuilder) {
		val reference = element.reference
		if(reference != null) {
			val property = reference.resolve() as? ParadoxLocalisationProperty
			if(property != null) {
				extractTo(property, buffer)
				return
			}
		}
		//如果处理文本失败，则直接使用原始文本
		buffer.append(element.text)
	}
	
	private fun extractIconTo(element: ParadoxLocalisationIcon, buffer: StringBuilder) {
		//不提取到结果中
		//val name = element.name
		//buffer.append(":$name:")
	}
	
	private fun extractSequentialNumberTo(element: ParadoxLocalisationSequentialNumber, buffer: StringBuilder) {
		val placeholderText = element.paradoxSequentialNumber?.placeholderText
		if(placeholderText != null) {
			buffer.append(placeholderText)
			return
		}
		//如果处理文本失败，则直接使用原始文本
		buffer.append(element.text)
	}
	
	private fun extractCodeTo(element: ParadoxLocalisationCommand, buffer: StringBuilder) {
		//使用原始文本
		buffer.append(element.text)
	}
	
	private fun extractColorfulTextTo(element: ParadoxLocalisationColorfulText, buffer: StringBuilder) {
		//直接渲染其中的文本
		for(v in element.richTextList) {
			extractTo(v, buffer)
		}
	}
}
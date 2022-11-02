package icu.windea.pls.core.tool

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化文本的提取器。
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ParadoxLocalisationTextExtractor {
	fun extract(element: ParadoxLocalisationProperty): String {
		return buildString { extractTo(element, this) }
	}
	
	fun extractTo(element: ParadoxLocalisationProperty, builder: StringBuilder) {
		element.propertyValue?.richTextList?.forEach { extractTo(it, builder) }
	}
	
	private fun extractTo(element: ParadoxLocalisationRichText, builder: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> extractStringTo(element, builder)
			is ParadoxLocalisationEscape -> extractEscapeTo(element, builder)
			is ParadoxLocalisationPropertyReference -> extractPropertyReferenceTo(element, builder)
			is ParadoxLocalisationIcon -> extractIconTo(element, builder)
			is ParadoxLocalisationCommand -> extractCodeTo(element, builder)
			is ParadoxLocalisationColorfulText -> extractColorfulTextTo(element, builder)
		}
	}
	
	private fun extractStringTo(element: ParadoxLocalisationString, builder: StringBuilder) {
		builder.append(element.text)
	}
	
	private fun extractEscapeTo(element: ParadoxLocalisationEscape, builder: StringBuilder) {
		val elementText = element.text
		when {
			elementText == "\\n" -> builder.append("\n")
			elementText == "\\t" -> builder.append("\t")
			elementText.length > 1 -> builder.append(elementText[1])
		}
	}
	
	private fun extractPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, builder: StringBuilder) {
		val reference = element.reference
		if(reference != null) {
			val resolved = reference.resolve()
			if(resolved is ParadoxLocalisationProperty) {
				extractTo(resolved, builder)
				return
			} else if(resolved is CwtProperty){
				builder.append(resolved.propertyValue)
				return
			}
		}
		//如果处理文本失败，则直接使用原始文本
		builder.append(element.text)
	}
	
	private fun extractIconTo(element: ParadoxLocalisationIcon, builder: StringBuilder) {
		//NOTE 不提取到结果中
		//builder.append(":${element.name}:")
	}
	
	private fun extractCodeTo(element: ParadoxLocalisationCommand, builder: StringBuilder) {
		//使用原始文本
		builder.append(element.text)
	}
	
	private fun extractColorfulTextTo(element: ParadoxLocalisationColorfulText, builder: StringBuilder) {
		//直接渲染其中的文本
		for(v in element.richTextList) {
			extractTo(v, builder)
		}
	}
}
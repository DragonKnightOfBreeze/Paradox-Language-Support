package icu.windea.pls.tool

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
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
		val richTextList = element.propertyValue?.richTextList
		if(richTextList == null || richTextList.isEmpty()) return
		for(richText in richTextList) {
			renderTo(richText, builder)
		}
	}
	
	private fun renderTo(element: ParadoxLocalisationRichText, builder: StringBuilder) {
		when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, builder)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, builder)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, builder)
			is ParadoxLocalisationIcon -> renderIconTo(element, builder)
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
			elementText == "\\r" -> builder.append("<br>\r")
			elementText == "\\t" -> builder.append("&emsp;")
			elementText.length > 1 -> builder.append(elementText[1])
		}
	}
	
	private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, builder: StringBuilder) {
		val reference = element.reference
		val rgbText = element.colorConfig?.color?.rgbString
		if(reference != null) {
			val property = reference.resolve()
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
		val iconFrame = element.frame
		val iconUrl = when {
			resolved is ParadoxDefinitionProperty -> ParadoxDdsUrlResolver.resolveByDefinition(resolved, iconFrame, defaultToUnknown = true)
			resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, iconFrame, defaultToUnknown = true)
			else -> return
		}
		if(iconUrl.isNotEmpty()) {
			//找不到图标的话就直接跳过
			val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return
			//基于文档的字体大小缩放图标，直到图标宽度等于字体宽度
			val docFontSize = DocumentationComponent.getQuickDocFontSize().size //基于这个得到的图标大小并不完美……
			val iconWidth = icon.iconWidth
			val iconHeight = icon.iconHeight
			val usedWidth = docFontSize * iconWidth / iconHeight
			val usedHeight = docFontSize
			builder.appendImgTag(iconUrl, usedWidth, usedHeight)
		}
	}
	
	private fun renderCodeTo(element: ParadoxLocalisationCommand, builder: StringBuilder) {
		//使用原始文本
		builder.append("<code>").append(element.text).append("</code>")
	}
	
	private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, builder: StringBuilder) {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val richTextList = element.richTextList
		if(richTextList.isEmpty()) return
		val rgbText = element.colorConfig?.color?.rgbString
		if(rgbText != null) builder.append("<span style=\"color: ").append(rgbText).append("\">")
		for(richText in richTextList) {
			renderTo(richText, builder)
		}
		if(rgbText != null) builder.append("</span>")
	}
}

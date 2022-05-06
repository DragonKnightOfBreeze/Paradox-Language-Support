package icu.windea.pls.tool

import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 本地化文本的内嵌提示渲染器。
 */
@Suppress("unused", "UnstableApiUsage", "UNUSED_PARAMETER")
object ParadoxLocalisationTextHintsRenderer {
	fun render(element: ParadoxLocalisationProperty, factory: PresentationFactory, editor: Editor): InlayPresentation? {
		val presentations: MutableList<InlayPresentation> = SmartList()
		renderTo(element, factory, editor, presentations)
		return presentations.mergePresentation()
	}
	
	fun renderTo(element: ParadoxLocalisationProperty, factory: PresentationFactory, editor: Editor, builder: MutableList<InlayPresentation>) {
		factory.renderTo(element, editor, builder)
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationProperty, editor: Editor, builder: MutableList<InlayPresentation>) {
		element.propertyValue?.richTextList?.forEach { renderTo(it, editor, builder) }
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationRichText, editor: Editor, builder: MutableList<InlayPresentation>) {
		when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, editor, builder)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, editor, builder)
			is ParadoxLocalisationPropertyReference -> this.renderPropertyReferenceTo(element, editor, builder)
			is ParadoxLocalisationIcon -> this.renderIconTo(element, editor, builder)
			is ParadoxLocalisationSequentialNumber -> renderSequentialNumberTo(element, editor, builder)
			is ParadoxLocalisationCommand -> renderCodeTo(element, editor, builder)
			is ParadoxLocalisationColorfulText -> this.renderColorfulTextTo(element, editor, builder)
		}
	}
	
	private fun PresentationFactory.renderStringTo(element: ParadoxLocalisationString, editor: Editor, builder: MutableList<InlayPresentation>) {
		builder.add(smallText(element.text))
	}
	
	private fun PresentationFactory.renderEscapeTo(element: ParadoxLocalisationEscape, editor: Editor, builder: MutableList<InlayPresentation>) {
		//使用原始文本（内嵌注释不能换行）
		builder.add(smallText(element.text))
	}
	
	private fun PresentationFactory.renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, editor: Editor, builder: MutableList<InlayPresentation>) {
		//如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
		val rgbText = element.colorConfig?.colorText
		val textAttributesKey = if(rgbText != null) ParadoxLocalisationAttributesKeys.COLOR_ONLY_KEYS[rgbText] else null
		val resolved = element.reference?.resolve().castOrNull<ParadoxLocalisationProperty>()
		val presentation = (if(resolved != null)  render(resolved, this, editor) else smallText(element.text)) ?: return
		val finalPresentation = if(textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, editor) else presentation
		builder.add(finalPresentation)
	}
	
	private fun PresentationFactory.renderIconTo(element: ParadoxLocalisationIcon, editor: Editor, builder: MutableList<InlayPresentation>) {
		val resolved = element.reference?.resolve() ?: return
		val iconUrl = when {
			resolved is ParadoxDefinitionProperty -> ParadoxDdsUrlResolver.resolveBySprite(resolved, defaultToUnknown = true)
			resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, defaultToUnknown = true)
			else -> return
		}
		if(iconUrl.isNotEmpty()) {
			val icon = IconLoader.findIcon(iconUrl, locationClass) ?: return //找不到图标的话就直接跳过
			builder.add(icon(icon))
		}
	}
	
	private fun PresentationFactory.renderSequentialNumberTo(element: ParadoxLocalisationSequentialNumber, editor: Editor, builder: MutableList<InlayPresentation>) {
		val placeholderText = element.sequentialNumberConfig?.placeholderText
		if(placeholderText != null) {
			builder.add(smallText(placeholderText))
			return
		}
		//如果处理文本失败，则直接使用原始文本
		builder.add(smallText(element.text))
	}
	
	private fun PresentationFactory.renderCodeTo(element: ParadoxLocalisationCommand, editor: Editor, builder: MutableList<InlayPresentation>) {
		//使用原始文本
		builder.add(smallText(element.text))
	}
	
	private fun PresentationFactory.renderColorfulTextTo(element: ParadoxLocalisationColorfulText, editor: Editor, builder: MutableList<InlayPresentation>) {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val rgbText = element.colorConfig?.colorText
		val textAttributesKey = if(rgbText != null) ParadoxLocalisationAttributesKeys.COLOR_ONLY_KEYS[rgbText] else null
		val innerBuilder: MutableList<InlayPresentation> = SmartList()
		for(v in element.richTextList) {
			renderTo(v, editor, innerBuilder)
		}
		val presentation = innerBuilder.mergePresentation() ?: return
		val finalPresentation = if(textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, editor) else presentation
		builder.add(finalPresentation)
	}
	
	private fun MutableList<InlayPresentation>.mergePresentation(): InlayPresentation? {
		return when {
			isEmpty() -> null
			size == 1 -> first()
			else -> SequencePresentation(this)
		}
	}
}

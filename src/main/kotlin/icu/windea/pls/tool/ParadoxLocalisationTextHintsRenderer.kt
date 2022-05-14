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
import icu.windea.pls.tool.ParadoxLocalisationTextHintsRenderer.renderTo
import java.util.concurrent.atomic.*

/**
 * 本地化文本的内嵌提示渲染器。
 */
@Suppress("unused", "UnstableApiUsage", "UNUSED_PARAMETER")
object ParadoxLocalisationTextHintsRenderer {
	fun render(element: ParadoxLocalisationProperty, factory: PresentationFactory, editor: Editor): InlayPresentation? {
		return factory.render(element, editor)
	}
	
	private fun PresentationFactory.render(element: ParadoxLocalisationProperty, editor: Editor): InlayPresentation? {
		val truncateRemain = AtomicInteger(getSettings().localisationTruncateLimit)
		val presentations: MutableList<InlayPresentation> = SmartList()
		val r = renderTo(element, editor, presentations, truncateRemain)
		if(!r) {
			presentations.add(smallText("...")) //添加省略号
		}
		return presentations.mergePresentation()
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationProperty, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		val richTextList = element.propertyValue?.richTextList
		if(richTextList == null || richTextList.isEmpty()) return true
		var continueProcess = true
		for(richText in richTextList) {
			val r = renderTo(richText, editor, builder, truncateRemain)
			if(!r) {
				continueProcess = false
				break
			}
		}
		return continueProcess
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationRichText, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		return when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationIcon -> renderIconTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationSequentialNumber -> renderSequentialNumberTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationCommand -> renderCodeTo(element, editor, builder, truncateRemain)
			is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, editor, builder, truncateRemain)
			else -> true
		}
	}
	
	private fun PresentationFactory.renderStringTo(element: ParadoxLocalisationString, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		val elementText = element.text
		builder.add(truncatedSmallText(elementText, truncateRemain))
		return continueProcess(truncateRemain)
	}
	
	private fun PresentationFactory.renderEscapeTo(element: ParadoxLocalisationEscape, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		//使用原始文本（内嵌注释不能换行，这时直接截断）
		val elementText = element.text
		when {
			elementText == "\\n" -> return false
			elementText == "\\r" -> return false
			else -> {
				builder.add(truncatedSmallText(elementText, truncateRemain))
				return continueProcess(truncateRemain)
			}
		}
	}
	
	private fun PresentationFactory.renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		//如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
		val rgbText = element.colorConfig?.colorText
		val textAttributesKey = if(rgbText != null) ParadoxLocalisationAttributesKeys.COLOR_ONLY_KEYS[rgbText] else null
		val resolved = element.reference?.resolve().castOrNull<ParadoxLocalisationProperty>()
		val presentation = when {
			resolved != null -> {
				val presentations: MutableList<InlayPresentation> = SmartList()
				renderTo(resolved, editor, presentations, truncateRemain)
				presentations.mergePresentation()
			}
			else -> truncatedSmallText(element.text, truncateRemain)
		} ?: return true
		val finalPresentation = when {
			textAttributesKey != null -> WithAttributesPresentation(presentation, textAttributesKey, editor)
			else -> presentation
		}
		builder.add(finalPresentation)
		return continueProcess(truncateRemain)
	}
	
	private fun PresentationFactory.renderIconTo(element: ParadoxLocalisationIcon, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		val resolved = element.reference?.resolve() ?: return true
		val iconUrl = when {
			resolved is ParadoxDefinitionProperty -> ParadoxDdsUrlResolver.resolveBySprite(resolved, defaultToUnknown = true)
			resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, defaultToUnknown = true)
			else -> return true
		}
		if(iconUrl.isNotEmpty()) {
			//忽略异常
			runCatching {
				//找不到图标的话就直接跳过
				val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return true
				//基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
				builder.add(smallScaledIcon(icon))
			}
		}
		return true
	}
	
	private fun PresentationFactory.renderSequentialNumberTo(element: ParadoxLocalisationSequentialNumber, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		val placeholderText = element.sequentialNumberConfig?.placeholderText
		if(placeholderText != null) {
			builder.add(truncatedSmallText(placeholderText, truncateRemain))
			return continueProcess(truncateRemain)
		}
		//如果处理文本失败，则直接使用原始文本
		builder.add(truncatedSmallText(element.text, truncateRemain))
		return continueProcess(truncateRemain)
	}
	
	private fun PresentationFactory.renderCodeTo(element: ParadoxLocalisationCommand, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		//使用原始文本
		builder.add(truncatedSmallText(element.text, truncateRemain))
		return continueProcess(truncateRemain)
	}
	
	private fun PresentationFactory.renderColorfulTextTo(element: ParadoxLocalisationColorfulText, editor: Editor, builder: MutableList<InlayPresentation>, truncateRemain: AtomicInteger): Boolean {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val richTextList = element.richTextList
		if(richTextList.isEmpty()) return true
		val colorId = element.colorConfig?.id
		val textAttributesKey = if(colorId != null) ParadoxLocalisationAttributesKeys.COLOR_ONLY_KEYS[colorId] else null
		val innerBuilder: MutableList<InlayPresentation> = SmartList()
		var continueProcess = true
		for(richText in richTextList) {
			val r = renderTo(richText, editor, innerBuilder, truncateRemain)
			if(!r) {
				continueProcess = false
				break
			}
		}
		val presentation = innerBuilder.mergePresentation() ?: return true
		val finalPresentation = if(textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, editor) else presentation
		builder.add(finalPresentation)
		return continueProcess
	}
	
	private fun MutableList<InlayPresentation>.mergePresentation(): InlayPresentation? {
		return when {
			isEmpty() -> null
			size == 1 -> first()
			else -> SequencePresentation(this)
		}
	}
	
	private fun PresentationFactory.truncatedSmallText(text: String, truncateRemain: AtomicInteger): InlayPresentation {
		val result = smallText(text.take(truncateRemain.get()))
		truncateRemain.addAndGet(-text.length)
		return result
	}
	
	private fun continueProcess(truncateRemain: AtomicInteger): Boolean {
		return truncateRemain.get() >= 0
	}
}

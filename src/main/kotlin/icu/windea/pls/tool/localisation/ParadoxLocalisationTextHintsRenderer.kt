package icu.windea.pls.tool.localisation

import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import java.util.concurrent.atomic.*

@Suppress("unused", "UnstableApiUsage", "UNUSED_PARAMETER")
object ParadoxLocalisationTextHintsRenderer {
	/**
	 * 如果[truncateLimit]小于等于0，则渲染首行文本。
	 */
	data class Context(
		val truncateLimit: Int,
		val iconHeightLimit: Int
	) {
		val truncateRemain = AtomicInteger(truncateLimit)
	}
	
	fun render(element: ParadoxLocalisationProperty, factory: PresentationFactory, editor: Editor, truncateLimit: Int, iconHeightLimit: Int): InlayPresentation? {
		//虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
		val context = Context(truncateLimit, iconHeightLimit)
		return factory.render(element, editor, context)
	}
	
	private fun PresentationFactory.render(element: ParadoxLocalisationProperty, editor: Editor, context: Context): InlayPresentation? {
		val presentations: MutableList<InlayPresentation> = SmartList()
		val r = renderTo(element, editor, presentations, context)
		if(!r) {
			presentations.add(smallText("...")) //添加省略号
		}
		return presentations.mergePresentation()
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationProperty, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		val richTextList = element.propertyValue?.richTextList
		if(richTextList == null || richTextList.isEmpty()) return true
		var continueProcess = true
		for(richText in richTextList) {
			val r = renderTo(richText, editor, builder, context)
			if(!r) {
				continueProcess = false
				break
			}
		}
		return continueProcess
	}
	
	private fun PresentationFactory.renderTo(element: ParadoxLocalisationRichText, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		return when(element) {
			is ParadoxLocalisationString -> renderStringTo(element, editor, builder, context)
			is ParadoxLocalisationEscape -> renderEscapeTo(element, editor, builder, context)
			is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, editor, builder, context)
			is ParadoxLocalisationIcon -> renderIconTo(element, editor, builder, context)
			is ParadoxLocalisationCommand -> renderCodeTo(element, editor, builder, context)
			is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, editor, builder, context)
			else -> true
		}
	}
	
	private fun PresentationFactory.renderStringTo(element: ParadoxLocalisationString, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		val elementText = element.text
		builder.add(truncatedSmallText(elementText, context))
		return continueProcess(context)
	}
	
	private fun PresentationFactory.renderEscapeTo(element: ParadoxLocalisationEscape, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		//使用原始文本（内嵌注释不能换行，这时直接截断）
		val elementText = element.text
		val text = when {
			elementText == "\\n" -> return false
			elementText == "\\r" -> return false
			elementText == "\\t" -> "\t"
			else -> elementText
		}
		builder.add(truncatedSmallText(text, context))
		return continueProcess(context)
	}
	
	private fun PresentationFactory.renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		//如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
		val colorConfig = element.colorConfig
		val resolved = element.reference?.resolve()
		val presentation = when {
			resolved is ParadoxLocalisationProperty -> {
				val presentations: MutableList<InlayPresentation> = SmartList()
				renderTo(resolved, editor, presentations, context)
				presentations.mergePresentation()
			}
			resolved is CwtProperty -> {
				smallText(resolved.value ?: PlsConstants.unresolvedString)
			}
			else -> truncatedSmallText(element.text, context)
		} ?: return true
		val textAttributesKey = if(colorConfig != null) ParadoxLocalisationAttributesKeys.getColorOnlyKey(colorConfig.color) else null
		val finalPresentation = when {
			textAttributesKey != null -> WithAttributesPresentation(presentation, textAttributesKey, editor)
			else -> presentation
		}
		builder.add(finalPresentation)
		return continueProcess(context)
	}
	
	private fun PresentationFactory.renderIconTo(element: ParadoxLocalisationIcon, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		val resolved = element.reference?.resolve() ?: return true
		val iconUrl = when {
			resolved is ParadoxScriptDefinitionElement -> ParadoxDdsUrlResolver.resolveByDefinition(resolved, defaultToUnknown = true)
			resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, defaultToUnknown = true)
			else -> return true
		}
		if(iconUrl.isNotEmpty()) {
			//忽略异常
			runCatching {
				//找不到图标的话就直接跳过
				val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return true
				if(icon.iconHeight <= context.iconHeightLimit) {
					//基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
					builder.add(smallScaledIcon(icon))
				} else {
					val unknownIcon = IconLoader.findIcon(PlsPaths.unknownPngUrl) ?: return true
					builder.add(smallScaledIcon(unknownIcon))
				}
			}
		}
		return true
	}
	
	private fun PresentationFactory.renderCodeTo(element: ParadoxLocalisationCommand, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		//使用原始文本
		builder.add(truncatedSmallText(element.text, context))
		return continueProcess(context)
	}
	
	private fun PresentationFactory.renderColorfulTextTo(element: ParadoxLocalisationColorfulText, editor: Editor, builder: MutableList<InlayPresentation>, context: Context): Boolean {
		//如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
		val richTextList = element.richTextList
		if(richTextList.isEmpty()) return true
		val colorConfig = element.colorConfig
		val textAttributesKey = if(colorConfig != null) ParadoxLocalisationAttributesKeys.getColorOnlyKey(colorConfig.color) else null
		val innerBuilder: MutableList<InlayPresentation> = SmartList()
		var continueProcess = true
		for(richText in richTextList) {
			val r = renderTo(richText, editor, innerBuilder, context)
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
	
	private fun PresentationFactory.truncatedSmallText(text: String, context: Context): InlayPresentation {
		if(context.truncateLimit <= 0) {
			val finalText = text
			val result = smallText(finalText)
			return result
		} else {
			val finalText = text.take(context.truncateRemain.get())
			val result = smallText(finalText)
			context.truncateRemain.addAndGet(-text.length)
			return result
		}
	}
	
	private fun continueProcess(context: Context): Boolean {
		return context.truncateLimit <= 0 || context.truncateRemain.get() >= 0
	}
}

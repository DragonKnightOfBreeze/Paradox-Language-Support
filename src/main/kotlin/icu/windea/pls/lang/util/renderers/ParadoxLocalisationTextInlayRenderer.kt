package icu.windea.pls.lang.util.renderers

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.WithAttributesPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.core.util.EscapeType
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.psi.mock.MockPsiElement
import icu.windea.pls.lang.psi.resolveLocalisation
import icu.windea.pls.lang.psi.resolveScriptedVariable
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer.*
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptText
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText
import icu.windea.pls.localisation.psi.ParadoxLocalisationText
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

/**
 * 用于将本地化文本渲染为内嵌提示（[InlayPresentation]）。
 *
 * 说明：
 * - 仅在当前节点不是标识符或图标时，才会适用截断限制。
 * - 仅渲染单行文本，从第二行开始的文本会被截断。
 */
@Suppress("unused", "UnstableApiUsage")
class ParadoxLocalisationTextInlayRenderer(
    val hintsContext: ParadoxHintsContext
) : ParadoxLocalisationTextRendererBase<Context, InlayPresentation?>() {
    data class Context(
        val editor: Editor,
        val settings: ParadoxHintsSettings,
        val factory: PresentationFactory,
        var builder: MutableList<InlayPresentation> = mutableListOf(),
    ) {
        val truncateRemain: AtomicInteger = AtomicInteger(settings.localisationTextLengthLimit) // 记录到需要截断为止所剩余的长度
        var lineEnd: Boolean = false
        var truncated: Boolean = false
        val guardStack: ArrayDeque<String> = ArrayDeque() // 避免 StackOverflow
        val colorStack: ArrayDeque<Color> = ArrayDeque()
    }

    var color: Color? = null

    fun withColor(color: Color?) = apply { this.color = color }

    override fun initContext(): Context {
        return Context(hintsContext.editor, hintsContext.settings, hintsContext.factory)
    }

    override fun getOutput(context: Context): InlayPresentation? {
        // 虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        if (context.truncated) context.builder.add(context.factory.smallText("...")) // 添加省略号
        return context.builder.mergePresentations()
    }

    context(context: Context)
    override fun renderRootProperty(element: ParadoxLocalisationProperty) {
        if (context.truncated) return
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        withGuard(context.guardStack, element) {
            withColorPresentation(color) {
                renderRichTexts(richTextList)
            }
        }
    }

    context(context: Context)
    override fun renderRootRichTexts(elements: List<ParadoxLocalisationRichText>) {
        withColorPresentation(color) {
            super.renderRootRichTexts(elements)
        }
    }

    context(context: Context)
    override fun renderRootRichText(element: ParadoxLocalisationRichText) {
        withColorPresentation(color) {
            super.renderRootRichText(element)
        }
    }

    context(context: Context)
    private inline fun withColorPresentation(color: Color?, action: () -> Unit) {
        if (context.truncated) return
        if (color == null) return action()

        val textAttributesKey = ParadoxLocalisationAttributesKeys.getColorOnlyKey(color)
        val oldBuilder = context.builder
        context.builder = mutableListOf()
        withColor(context.colorStack, color) {
            action()
        }
        val newBuilder = context.builder
        context.builder = oldBuilder
        val presentation = newBuilder.mergePresentations() ?: return
        val finalPresentation = if (textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, context.editor) else presentation
        context.builder.add(finalPresentation)
    }

    context(context: Context)
    override fun renderRichTexts(elements: List<ParadoxLocalisationRichText>) {
        if (elements.isEmpty()) return
        for (richText in elements) {
            ProgressManager.checkCanceled()
            renderRichText(richText)
            if (context.truncated) return
        }
    }

    context(context: Context)
    override fun renderString(element: ParadoxLocalisationText) {
        if (context.truncated) return
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, EscapeType.Inlay)
        context.builder.add(context.factory.truncatedSmallText(text))
        updateTruncationState()
    }

    context(context: Context)
    override fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
        if (context.truncated) return
        // 如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (shouldRenderColurfulText()) element.colorInfo?.color else null
        withColorPresentation(color) {
            renderRichTexts(richTextList)
        }
    }

    context(context: Context)
    override fun renderParameter(element: ParadoxLocalisationParameter) {
        if (context.truncated) return
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        withColorPresentation(color) action@{
            // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            // 本地化
            run {
                if (resolved !is ParadoxLocalisationProperty) return@run
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) return@run // 跳过特殊本地化
                if (checkGuard(context.guardStack, resolved)) return@run // 跳过存在递归的情况
                val richTextList = resolved.propertyValue?.richTextList
                if (richTextList.isNullOrEmpty()) return
                withGuard(context.guardStack, resolved) {
                    renderRichTextsForParameter(richTextList)
                }
                return@action
            }
            // 封装变量
            run {
                if (resolved !is ParadoxScriptScriptedVariable) return@run
                val v = resolved.value ?: PlsStrings.unresolved
                context.builder.add(context.factory.smallText(v))
                updateTruncationState()
                return@action
            }

            // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
            val presentation = getElementPresentation(element)
            if (presentation != null) context.builder.add(presentation)
            updateTruncationState()
        }
    }

    context(context: Context)
    private fun renderRichTextsForParameter(richTextList: List<ParadoxLocalisationRichText>) {
        val oldBuilder = context.builder
        context.builder = mutableListOf()
        renderRichTexts(richTextList)
        val newBuilder = context.builder
        context.builder = oldBuilder
        val presentation = newBuilder.mergePresentations()
        if (presentation != null) context.builder.add(presentation)
        updateTruncationState()
    }

    context(context: Context)
    override fun renderIcon(element: ParadoxLocalisationIcon) {
        if (context.truncated) return
        // 尝试渲染图标
        runCatchingCancelable r@{
            val resolved = element.reference?.resolve() ?: return@r
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageManager.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageManager.resolveUrlByFile(resolved.virtualFile, resolved.project, frameInfo)
                else -> null
            }

            // 如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return@r

            val iconFileUrl = iconUrl.toFileUrl()
            // 找不到图标的话就直接跳过
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            // 这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            if (originalIconHeight > context.settings.iconHeightLimit) return@r // 图标过大，不再尝试渲染
            // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val presentation = context.factory.psiSingleReference(context.factory.smallScaledIcon(icon)) { resolved }
            context.builder.add(presentation)
            return
        }

        // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) context.builder.add(presentation)
        updateTruncationState()
    }

    context(context: Context)
    override fun renderCommand(element: ParadoxLocalisationCommand) {
        if (context.truncated) return
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        withColorPresentation(color) action@{
            // 直接显示命令文本，适用对应的颜色高亮
            // 点击其中的相关文本也能跳转到相关声明（如 `scope` 和 `scripted_loc`）
            val presentations = mutableListOf<InlayPresentation>()
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    getElementPresentation(c)?.let { presentations.add(it) }
                } else {
                    presentations.add(context.factory.smallText(c.text))
                }
            }
            val mergedPresentation = presentations.mergePresentations()
            if (mergedPresentation != null) context.builder.add(mergedPresentation)
            updateTruncationState()
        }
    }

    context(context: Context)
    override fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
        if (context.truncated) return
        // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
        val (referenceElement, resolved) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        // 概念文本
        run {
            if (resolved !is ParadoxLocalisationConceptText) return@run
            val richTextList = resolved.richTextList
            if (richTextList.isEmpty()) return
            renderRichTextsForConceptCommand(richTextList, referenceElement)
            return
        }
        // 本地化
        run {
            if (resolved !is ParadoxLocalisationProperty) return@run
            if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) return@run // 跳过特殊本地化
            if (checkGuard(context.guardStack, resolved)) return@run  // 跳过存在递归的情况
            val richTextList = resolved.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return
            withGuard(context.guardStack, resolved) {
                renderRichTextsForConceptCommand(richTextList, referenceElement)
            }
            return
        }

        // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) context.builder.add(wrapConceptPresentation(presentation, referenceElement))
        updateTruncationState()
    }

    context(context: Context)
    private fun renderRichTextsForConceptCommand(richTextList: List<ParadoxLocalisationRichText>, referenceElement: PsiElement?) {
        val newBuilder = mutableListOf<InlayPresentation>()
        val oldBuilder = context.builder
        context.builder = newBuilder
        renderRichTexts(richTextList)
        context.builder = oldBuilder
        val presentation = newBuilder.mergePresentations()
        if (presentation != null) context.builder.add(wrapConceptPresentation(presentation, referenceElement))
        updateTruncationState()
    }

    context(context: Context)
    private fun wrapConceptPresentation(presentation: InlayPresentation, referenceElement: PsiElement?): InlayPresentation {
        val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
        val attributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
        var result: InlayPresentation = WithAttributesPresentation(presentation, conceptAttributesKey, context.editor, attributesFlags)
        if (referenceElement != null) {
            result = context.factory.psiSingleReference(result) { referenceElement }
        }
        result = context.factory.onHover(result, object : InlayPresentationFactory.HoverListener {
            override fun onHover(event: MouseEvent, translated: Point) {
                attributesFlags.isDefault = true // change foreground
            }

            override fun onHoverFinished() {
                attributesFlags.isDefault = false // reset foreground
            }
        })
        return result
    }

    context(context: Context)
    override fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
        if (context.truncated) return
        // TODO 1.4.1+ 更完善的支持（渲染文本图标）

        // 直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) context.builder.add(presentation)
        updateTruncationState()
    }

    context(context: Context)
    override fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
        if (context.truncated) return
        // TODO 1.4.1+ 更完善的支持（适用文本格式）

        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        renderRichTexts(richTextList)
    }

    context(context: Context)
    private fun PresentationFactory.truncatedSmallText(text: String): InlayPresentation {
        val limit = context.settings.localisationTextLengthLimit
        val truncatedText = if (limit > 0) text.take(context.truncateRemain.get()) else text
        val truncatedTextSingleLine = truncatedText.substringBefore('\n')
        val finalText = truncatedTextSingleLine
        val result = smallText(finalText)
        if (truncatedTextSingleLine.length != truncatedText.length) {
            context.lineEnd = true
        }
        if (limit > 0) {
            context.truncateRemain.getAndAdd(-truncatedText.length)
        }
        updateTruncationState()
        return result
    }

    context(context: Context)
    private fun updateTruncationState() {
        if (context.truncated) return
        val limit = context.settings.localisationTextLengthLimit
        context.truncated = context.lineEnd || (limit > 0 && context.truncateRemain.get() <= 0)
    }

    context(context: Context)
    private fun getElementPresentation(element: PsiElement): InlayPresentation? {
        val presentations = mutableListOf<InlayPresentation>()

        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            presentations.add(context.factory.smallText(element.text))
            return presentations.mergePresentations()
        }
        var i = 0
        for (reference in references) {
            ProgressManager.checkCanceled()
            val startOffset = reference.rangeInElement.startOffset
            if (startOffset != i) {
                val s = text.substring(i, startOffset)
                presentations.add(context.factory.smallText(s))
            }
            i = reference.rangeInElement.endOffset
            val s = reference.rangeInElement.substring(text)
            val resolved = reference.resolve()
            // 不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is MockPsiElement) {
                presentations.add(context.factory.smallText(s))
            } else {
                presentations.add(context.factory.psiSingleReference(context.factory.smallText(s)) { reference.resolve() })
            }
        }
        val endOffset = references.last().rangeInElement.endOffset
        if (endOffset != text.length) {
            val s = text.substring(endOffset)
            presentations.add(context.factory.smallText(s))
        }
        return presentations.mergePresentations()
    }
}

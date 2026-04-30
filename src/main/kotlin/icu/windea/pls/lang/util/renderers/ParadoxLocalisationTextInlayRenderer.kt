@file:Suppress("unused", "UnstableApiUsage")

package icu.windea.pls.lang.util.renderers

import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.WithAttributesPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.letIf
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.core.util.text.EscapeType
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.highlighting.ParadoxAttributesKeysManager
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.PlsHintsUtil
import icu.windea.pls.lang.psi.resolveLocalisation
import icu.windea.pls.lang.psi.resolveScriptedVariable
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
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
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.awt.Color
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

/**
 * 将本地化文本渲染为内嵌提示（[InlayPresentation]）的渲染器。
 *
 * 说明：
 * - 仅在当前节点不是标识符或图标时，才会适用截断限制。
 * - 仅渲染单行文本，从第二行开始的文本会被截断。
 */
class ParadoxLocalisationTextInlayRenderer(
    val hintsContext: ParadoxHintsContext
) : ParadoxLocalisationTextRenderer<InlayPresentation?, ParadoxLocalisationTextInlayRenderContext, ParadoxLocalisationTextInlayRenderSettings>() {
    override val settings = ParadoxLocalisationTextInlayRenderSettings()

    override fun createContext() = ParadoxLocalisationTextInlayRenderContext(settings, hintsContext)
}

class ParadoxLocalisationTextInlayRenderContext(
    private val settings: ParadoxLocalisationTextInlayRenderSettings,
    val hintsContext: ParadoxHintsContext,
    var builder: MutableList<InlayPresentation> = mutableListOf(),
) : ParadoxLocalisationTextRenderContext<InlayPresentation?>() {
    private val truncateRemain: AtomicInteger = AtomicInteger(hintsContext.settings.localisationTextLengthLimit) // 记录到需要截断为止所剩余的长度
    private var lineEnd: Boolean = false
    private var truncated: Boolean = false
    private val guardStack: ArrayDeque<String> = ArrayDeque() // 避免 StackOverflow
    private val colorStack: ArrayDeque<Color> = ArrayDeque()

    val editor: Editor get() = hintsContext.editor
    val factory: PresentationFactory get() = hintsContext.factory

    override fun build(): InlayPresentation? {
        // 虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        if (truncated) builder.add(factory.smallText("...")) // 添加省略号
        return builder.mergePresentations()
    }

    override fun renderRootProperty(element: ParadoxLocalisationProperty) {
        if (truncated) return
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        withGuard(guardStack, element) {
            withColorPresentation(settings.color) {
                renderRichTexts(richTextList)
            }
        }
    }

    override fun renderRootRichTexts(elements: List<ParadoxLocalisationRichText>) {
        withColorPresentation(settings.color) {
            super.renderRootRichTexts(elements)
        }
    }

    override fun renderRootRichText(element: ParadoxLocalisationRichText) {
        withColorPresentation(settings.color) {
            super.renderRootRichText(element)
        }
    }

    private inline fun withColorPresentation(color: Color?, action: () -> Unit) {
        if (truncated) return
        if (color == null) return action()

        val textAttributesKey = ParadoxAttributesKeysManager.getColorOnlyKey(color)
        val oldBuilder = builder
        builder = mutableListOf()
        withColor(colorStack, color) {
            action()
        }
        val newBuilder = builder
        builder = oldBuilder
        val presentation = newBuilder.mergePresentations() ?: return
        val finalPresentation = WithAttributesPresentation(presentation, textAttributesKey, editor)
        builder.add(finalPresentation)
    }

    override fun renderRichTexts(elements: List<ParadoxLocalisationRichText>) {
        if (elements.isEmpty()) return
        for (richText in elements) {
            ProgressManager.checkCanceled()
            renderRichText(richText)
            if (truncated) return
        }
    }

    override fun renderText(element: ParadoxLocalisationText) {
        if (truncated) return
        val text = ParadoxEscapeManager.unescapeLocalisationText(element.text, EscapeType.Inlay)
        builder.add(truncatedSmallText(text))
        updateTruncationState()
    }

    override fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
        if (truncated) return
        // 如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (shouldRenderColorfulText()) element.colorInfo?.color else null
        withColorPresentation(color) {
            renderRichTexts(richTextList)
        }
    }

    override fun renderParameter(element: ParadoxLocalisationParameter) {
        if (truncated) return
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColorfulText()) element.argumentElement?.colorInfo?.color else null
        withColorPresentation(color) action@{
            // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            // 本地化
            run {
                if (resolved !is ParadoxLocalisationProperty) return@run
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) return@run // 跳过特殊本地化
                if (checkGuard(guardStack, resolved)) return@run // 跳过存在递归的情况
                val richTextList = resolved.propertyValue?.richTextList
                if (richTextList.isNullOrEmpty()) return@run
                withGuard(guardStack, resolved) {
                    renderRichTextsForParameter(richTextList)
                }
                return@action
            }
            // 封装变量
            run {
                if (resolved !is ParadoxScriptScriptedVariable) return@run
                val v = resolved.value ?: FallbackStrings.unresolved
                builder.add(factory.smallText(v))
                updateTruncationState()
                return@action
            }

            // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
            val presentation = getElementPresentation(element)
            if (presentation != null) builder.add(presentation)
            updateTruncationState()
        }
    }

    private fun renderRichTextsForParameter(richTextList: List<ParadoxLocalisationRichText>) {
        val oldBuilder = builder
        builder = mutableListOf()
        renderRichTexts(richTextList)
        val newBuilder = builder
        builder = oldBuilder
        val presentation = newBuilder.mergePresentations()
        if (presentation != null) builder.add(presentation)
        updateTruncationState()
    }

    override fun renderIcon(element: ParadoxLocalisationIcon) {
        if (truncated) return
        // 尝试渲染图标
        runCatchingCancelable r@{
            val resolved = element.reference?.resolve() ?: return@r
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxDefinitionElement -> ParadoxImageManager.resolveUrlByDefinition(resolved, frameInfo)
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
            if (originalIconHeight > this@ParadoxLocalisationTextInlayRenderContext.hintsContext.settings.iconHeightLimit) return@r // 图标过大，不再尝试渲染
            // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val presentation = factory.psiSingleReference(factory.smallScaledIcon(icon)) { resolved }
            builder.add(presentation)
            return
        }

        // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) builder.add(presentation)
        updateTruncationState()
    }

    override fun renderCommand(element: ParadoxLocalisationCommand) {
        if (truncated) return
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColorfulText()) element.argumentElement?.colorInfo?.color else null
        withColorPresentation(color) action@{
            // 直接显示命令文本，适用对应的颜色高亮
            // 点击其中的相关文本也能跳转到相关声明（如 `context` 和 `scripted_loc`）
            val presentations = mutableListOf<InlayPresentation>()
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    getElementPresentation(c)?.let { presentations.add(it) }
                } else {
                    presentations.add(factory.smallText(c.text))
                }
            }
            val mergedPresentation = presentations.mergePresentations()
            if (mergedPresentation != null) builder.add(mergedPresentation)
            updateTruncationState()
        }
    }

    override fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
        if (truncated) return
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
            if (checkGuard(guardStack, resolved)) return@run  // 跳过存在递归的情况
            val richTextList = resolved.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return
            withGuard(guardStack, resolved) {
                renderRichTextsForConceptCommand(richTextList, referenceElement)
            }
            return
        }

        // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) builder.add(wrapConceptPresentation(presentation, referenceElement))
        updateTruncationState()
    }

    private fun renderRichTextsForConceptCommand(richTextList: List<ParadoxLocalisationRichText>, referenceElement: PsiElement?) {
        val newBuilder = mutableListOf<InlayPresentation>()
        val oldBuilder = builder
        builder = newBuilder
        renderRichTexts(richTextList)
        builder = oldBuilder
        val presentation = newBuilder.mergePresentations()
        if (presentation != null) builder.add(wrapConceptPresentation(presentation, referenceElement))
        updateTruncationState()
    }

    private fun wrapConceptPresentation(presentation: InlayPresentation, referenceElement: PsiElement?): InlayPresentation {
        // NOTE 2.1.2 `WithAttributesPresentation` 需要作为 `psiSingleReference` 的子节点，从而正确提供配色
        val basePresentation = presentation
            .letIf(referenceElement != null) { factory.psiSingleReference(it) { referenceElement } }
        val attributesKey = ParadoxLocalisationAttributesKeys.CONCEPT
        val attributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
        val finalPresentation = WithAttributesPresentation(basePresentation, attributesKey, editor, attributesFlags)
            .let { factory.withCursorOnHoverWhenControlDown(it, PlsHintsUtil.getHandCursor()) }
        return finalPresentation
    }

    override fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
        if (truncated) return
        // TODO 1.4.1+ 更完善的支持（渲染文本图标）

        // 直接显示原始文本（点击其中的相关文本也能跳转到相关声明）
        val presentation = getElementPresentation(element)
        if (presentation != null) builder.add(presentation)
        updateTruncationState()
    }

    override fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
        if (truncated) return
        // TODO 1.4.1+ 更完善的支持（适用文本格式）

        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        renderRichTexts(richTextList)
    }

    private fun truncatedSmallText(text: String): InlayPresentation {
        val limit = hintsContext.settings.localisationTextLengthLimit
        val truncatedText = if (limit > 0) text.take(truncateRemain.get()) else text
        val truncatedTextSingleLine = truncatedText.substringBefore('\n')
        val finalText = truncatedTextSingleLine
        val result = factory.smallText(finalText)
        if (truncatedTextSingleLine.length != truncatedText.length) {
            lineEnd = true
        }
        if (limit > 0) {
            truncateRemain.getAndAdd(-truncatedText.length)
        }
        updateTruncationState()
        return result
    }

    private fun updateTruncationState() {
        if (truncated) return
        val limit = hintsContext.settings.localisationTextLengthLimit
        truncated = lineEnd || (limit > 0 && truncateRemain.get() <= 0)
    }

    private fun getElementPresentation(element: PsiElement): InlayPresentation? {
        val presentations = mutableListOf<InlayPresentation>()
        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            presentations.add(factory.smallText(element.text))
            return presentations.mergePresentations()
        }
        var i = 0
        for (reference in references) {
            ProgressManager.checkCanceled()
            val startOffset = reference.rangeInElement.startOffset
            if (startOffset != i) {
                val s = text.substring(i, startOffset)
                presentations.add(factory.smallText(s))
            }
            i = reference.rangeInElement.endOffset
            val s = reference.rangeInElement.substring(text)
            val resolved = reference.resolve()
            // 不要尝试跳转到动态值的声明处
            if (resolved == null || resolved is LightElementBase) {
                presentations.add(factory.smallText(s))
            } else {
                presentations.add(factory.psiSingleReference(factory.smallText(s)) { reference.resolve() })
            }
        }
        val endOffset = references.last().rangeInElement.endOffset
        if (endOffset != text.length) {
            val s = text.substring(endOffset)
            presentations.add(factory.smallText(s))
        }
        return presentations.mergePresentations()
    }
}

data class ParadoxLocalisationTextInlayRenderSettings(
    var color: Color? = null,
) : ParadoxLocalisationTextRenderSettings()

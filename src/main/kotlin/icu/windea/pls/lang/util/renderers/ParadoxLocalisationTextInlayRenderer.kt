package icu.windea.pls.lang.util.renderers

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.WithAttributesPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.psi.mock.MockPsiElement
import icu.windea.pls.lang.resolveLocalisation
import icu.windea.pls.lang.resolveScriptedVariable
import icu.windea.pls.lang.settings.PlsSettings
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
import icu.windea.pls.localisation.psi.ParadoxLocalisationString
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.constants.PlsStringConstants
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
) : ParadoxRenderer<PsiElement, Context, InlayPresentation?> {
    data class Context(
        val editor: Editor,
        val settings: ParadoxHintsSettings,
        val factory: PresentationFactory,
        var builder: MutableList<InlayPresentation> = mutableListOf(),
    ) {
        val truncateRemain: AtomicInteger = AtomicInteger(settings.textLengthLimit) // 记录到需要截断为止所剩余的长度
        var lineEnd: Boolean = false
        val guardStack: ArrayDeque<String> = ArrayDeque() // 防止 StackOverflow
    }

    var color: Color? = null

    fun withColor(color: Color?) = apply { this.color = color }

    override fun initContext(): Context {
        return Context(hintsContext.editor, hintsContext.settings, hintsContext.factory)
    }

    override fun render(input: PsiElement, context: Context): InlayPresentation? {
        return when (input) {
            is ParadoxLocalisationProperty -> render(input, context)
            is ParadoxLocalisationRichText -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxLocalisationProperty, context: Context = initContext()): InlayPresentation? {
        // 虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        val r = with(context) { renderProperty(element) }
        if (!r) context.builder.add(context.factory.smallText("...")) // 添加省略号
        return context.builder.mergePresentations()
    }

    fun render(element: ParadoxLocalisationRichText, context: Context = initContext()): InlayPresentation? {
        // 虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        val r = with(context) { renderRichText(element) }
        if (!r) context.builder.add(context.factory.smallText("...")) // 添加省略号
        return context.builder.mergePresentations()
    }

    context(context: Context)
    private fun renderProperty(element: ParadoxLocalisationProperty): Boolean {
        context.guardStack.addLast(element.name)
        try {
            val richTextList = element.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return true
            return renderWithColor(color) r@{
                for (richText in richTextList) {
                    ProgressManager.checkCanceled()
                    val r = renderRichText(richText)
                    if (!r) return@r false
                }
                true
            }
        } finally {
            context.guardStack.removeLast()
        }
    }

    context(context: Context)
    private fun renderRootRichText(element: ParadoxLocalisationRichText): Boolean {
        return renderWithColor(color) {
            renderRichText(element)
        }
    }

    context(context: Context)
    private fun renderWithColor(color: Color?, action: () -> Boolean): Boolean {
        if (color == null) return action()

        val textAttributesKey = ParadoxLocalisationAttributesKeys.getColorOnlyKey(color)
        val oldBuilder = context.builder
        context.builder = mutableListOf()
        val continueProcess = action()
        val newBuilder = context.builder
        context.builder = oldBuilder
        val presentation = newBuilder.mergePresentations() ?: return true
        val finalPresentation = if (textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, context.editor) else presentation
        context.builder.add(finalPresentation)
        return continueProcess
    }

    context(context: Context)
    private fun renderRichText(element: ParadoxLocalisationRichText): Boolean {
        return when (element) {
            is ParadoxLocalisationString -> renderString(element)
            is ParadoxLocalisationColorfulText -> renderColorfulText(element)
            is ParadoxLocalisationParameter -> renderParameter(element)
            is ParadoxLocalisationIcon -> renderIcon(element)
            is ParadoxLocalisationCommand -> renderCommand(element)
            is ParadoxLocalisationConceptCommand -> renderConceptCommand(element)
            is ParadoxLocalisationTextIcon -> renderTextIcon(element)
            is ParadoxLocalisationTextFormat -> renderTextFormat(element)
            else -> throw UnsupportedOperationException()
        }
    }

    context(context: Context)
    private fun renderString(element: ParadoxLocalisationString): Boolean {
        val escapeType = ParadoxEscapeManager.Type.Inlay
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, escapeType)
        context.builder.add(context.factory.truncatedSmallText(text))
        return continueProcess()
    }

    context(context: Context)
    private fun renderColorfulText(element: ParadoxLocalisationColorfulText): Boolean {
        // 如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return true
        val color = if (shouldRenderColurfulText()) element.colorInfo?.color else null
        return renderWithColor(color) r@{
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderRichText(richText)
                if (!r) return@r false
            }
            true
        }
    }

    context(context: Context)
    private fun renderParameter(element: ParadoxLocalisationParameter): Boolean {
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        return renderWithColor(color) r@{
            // 如果处理文本失败，则使用原始文本
            // 直接解析为本地化（或者封装变量）以优化性能
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            val presentation = when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        getElementPresentation(element)
                    } else {
                        val resolvedName = resolved.name
                        if (context.guardStack.contains(resolvedName)) {
                            getElementPresentation(element)
                        } else {
                            val oldBuilder = context.builder
                            context.builder = mutableListOf()
                            renderProperty(resolved)
                            val newBuilder = context.builder
                            context.builder = oldBuilder
                            newBuilder.mergePresentations()
                        }
                    }
                }
                resolved is CwtProperty -> {
                    context.factory.smallText(resolved.value ?: PlsStringConstants.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    context.factory.smallText(resolved.value ?: PlsStringConstants.unresolved)
                }
                else -> {
                    context.factory.truncatedSmallText(element.text)
                }
            }
            if (presentation != null) context.builder.add(presentation)
            continueProcess()
        }
    }

    context(context: Context)
    private fun renderIcon(element: ParadoxLocalisationIcon): Boolean {
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
            if (originalIconHeight > context.settings.iconHeightLimit) return true // 图标过大，不再尝试渲染
            // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val presentation = context.factory.psiSingleReference(context.factory.smallScaledIcon(icon)) { resolved }
            context.builder.add(presentation)
            return true
        }

        // 直接显示原始文本
        // 点击其中的相关文本也能跳转到相关声明
        val presentation = getElementPresentation(element)
        if (presentation != null) context.builder.add(presentation)
        return continueProcess()
    }

    context(context: Context)
    private fun renderCommand(element: ParadoxLocalisationCommand): Boolean {
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        return renderWithColor(color) r@{
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
            continueProcess()
        }
    }

    context(context: Context)
    private fun renderConceptCommand(element: ParadoxLocalisationConceptCommand): Boolean {
        // 尝试渲染概念文本
        val (referenceElement, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r@{
            if (richTextList.isNullOrEmpty()) return@r
            val newBuilder = mutableListOf<InlayPresentation>()
            val oldBuilder = context.builder
            context.builder = newBuilder
            var continueProcess = true
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderRichText(richText)
                if (!r) {
                    continueProcess = false
                    break
                }
            }
            context.builder = oldBuilder
            val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
            var presentation = newBuilder.mergePresentations()
            if (presentation != null) {
                val attributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
                presentation = WithAttributesPresentation(presentation, conceptAttributesKey, context.editor, attributesFlags)
                val referencePresentation = context.factory.psiSingleReference(presentation) { referenceElement }
                presentation = context.factory.onHover(referencePresentation, object : InlayPresentationFactory.HoverListener {
                    override fun onHover(event: MouseEvent, translated: Point) {
                        attributesFlags.isDefault = true // change foreground
                    }

                    override fun onHoverFinished() {
                        attributesFlags.isDefault = false // reset foreground
                    }
                })
                context.builder.add(presentation)
            }
            if (!continueProcess) return false
            return continueProcess()
        }

        context.builder.add(context.factory.smallText(element.name))
        return continueProcess()
    }

    context(context: Context)
    private fun renderTextIcon(element: ParadoxLocalisationTextIcon): Boolean {
        // TODO 1.4.1+ 更完善的支持（渲染文本图标）

        // 直接显示原始文本
        // 点击其中的相关文本也能跳转到相关声明
        val presentation = getElementPresentation(element)
        if (presentation != null) context.builder.add(presentation)
        return continueProcess()
    }

    context(context: Context)
    private fun renderTextFormat(element: ParadoxLocalisationTextFormat): Boolean {
        // TODO 1.4.1+ 更完善的支持（适用文本格式）

        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return true
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            val r = renderRichText(richText)
            if (!r) return false
        }
        return true
    }

    context(context: Context)
    private fun PresentationFactory.truncatedSmallText(text: String): InlayPresentation {
        val textLengthLimit = context.settings.textLengthLimit
        val truncatedText = if (textLengthLimit > 0) text.take(context.truncateRemain.get()) else text
        val truncatedTextSingleLine = truncatedText.substringBefore('\n')
        val finalText = truncatedTextSingleLine
        val result = smallText(finalText)
        if (truncatedTextSingleLine.length != truncatedText.length) {
            context.lineEnd = true
        }
        if (textLengthLimit > 0) {
            context.truncateRemain.getAndAdd(-truncatedText.length)
        }
        return result
    }

    context(context: Context)
    private fun continueProcess(): Boolean {
        val textLengthLimit = context.settings.textLengthLimit
        return !context.lineEnd && (context.truncateRemain.get() > 0 || textLengthLimit <= 0)
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

    private fun shouldRenderColurfulText(): Boolean {
        return PlsSettings.getInstance().state.others.renderLocalisationColorfulText
    }
}

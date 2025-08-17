package icu.windea.pls.lang.util.renderers

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.awt.event.*
import java.util.concurrent.atomic.*
import javax.imageio.*

/**
 * 仅渲染单行文本，从第二行开始的文本会被截断。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationTextInlayRenderer(
    val editor: Editor,
    val factory: PresentationFactory,
    var builder: MutableList<InlayPresentation> = mutableListOf(),
) {
    var textLengthLimit: Int = -1
    var iconHeightLimit: Int = -1

    private val truncateRemain by lazy { AtomicInteger(textLengthLimit) } //记录到需要截断为止所剩余的长度
    private var lineEnd = false
    private val guardStack = ArrayDeque<String>() //防止StackOverflow

    fun withLimit(textLengthLimit: Int, iconHeightLimit: Int): ParadoxLocalisationTextInlayRenderer {
        this.textLengthLimit = textLengthLimit
        this.iconHeightLimit = iconHeightLimit
        return this
    }

    fun render(element: ParadoxLocalisationProperty): InlayPresentation? {
        //虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        return doRender {
            renderTo(element)
        }
    }

    fun render(element: ParadoxLocalisationParameter): InlayPresentation? {
        //虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        return doRender {
            renderRichTextTo(element)
        }
    }

    private fun renderTo(element: ParadoxLocalisationProperty): Boolean {
        guardStack.addLast(element.name)
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return true
        var continueProcess = true
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            val r = renderRichTextTo(richText)
            if (!r) {
                continueProcess = false
                break
            }
        }
        return continueProcess
    }

    @Suppress("unused")
    fun renderWithColor(color: Color?, action: () -> Boolean): InlayPresentation? {
        return doRender {
            renderWithColorTo(color, action)
        }
    }

    private fun renderWithColorTo(color: Color?, action: () -> Boolean): Boolean {
        val textAttributesKey = if (color != null) ParadoxLocalisationAttributesKeys.getColorOnlyKey(color) else null
        val oldBuilder = builder
        builder = mutableListOf()
        val continueProcess = action()
        val newBuilder = builder
        builder = oldBuilder
        val presentation = mergePresentation(newBuilder) ?: return true
        val finalPresentation = if (textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, editor) else presentation
        builder.add(finalPresentation)
        return continueProcess
    }

    private fun doRender(action: () -> Boolean): InlayPresentation? {
        val r = action()
        if (!r) builder.add(factory.smallText("...")) //添加省略号
        return mergePresentation(builder)
    }

    private fun renderRichTextTo(element: ParadoxLocalisationRichText): Boolean {
        return when (element) {
            is ParadoxLocalisationString -> renderStringTo(element)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element)
            is ParadoxLocalisationParameter -> renderParameterTo(element)
            is ParadoxLocalisationCommand -> renderCommandTo(element)
            is ParadoxLocalisationIcon -> renderIconTo(element)
            is ParadoxLocalisationConceptCommand -> renderConceptCommandTo(element)
            is ParadoxLocalisationTextFormat -> renderTextFormatTo(element)
            is ParadoxLocalisationTextIcon -> renderTextIconTo(element)
            else -> true
        }
    }

    private fun renderStringTo(element: ParadoxLocalisationString): Boolean {
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, ParadoxEscapeManager.Type.Inlay)
        builder.add(factory.truncatedSmallText(text))
        return continueProcess()
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText): Boolean {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return true
        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.colorInfo?.color else null
        return renderWithColorTo(color) {
            var continueProcess = true
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderRichTextTo(richText)
                if (!r) {
                    continueProcess = false
                    break
                }
            }
            continueProcess
        }
    }

    private fun renderParameterTo(element: ParadoxLocalisationParameter): Boolean {
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        return renderWithColorTo(color) r@{
            //如果处理文本失败，则使用原始文本
            //直接解析为本地化（或者封装变量）以优化性能
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            val presentation = when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        getElementPresentation(element)
                    } else {
                        val resolvedName = resolved.name
                        if (guardStack.contains(resolvedName)) {
                            getElementPresentation(element)
                        } else {
                            try {
                                val oldBuilder = builder
                                builder = mutableListOf()
                                renderTo(resolved)
                                val newBuilder = builder
                                builder = oldBuilder
                                mergePresentation(newBuilder)
                            } finally {
                                guardStack.removeLast()
                            }
                        }
                    }
                }
                resolved is CwtProperty -> {
                    factory.smallText(resolved.value ?: PlsStringConstants.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    factory.smallText(resolved.value ?: PlsStringConstants.unresolved)
                }
                else -> {
                    factory.truncatedSmallText(element.text)
                }
            }
            if (presentation != null) builder.add(presentation)
            continueProcess()
        }
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand): Boolean {
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        return renderWithColorTo(color) r@{
            //直接显示命令文本，适用对应的颜色高亮
            //点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc）
            val presentations = mutableListOf<InlayPresentation>()
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    getElementPresentation(c)?.let { presentations.add(it) }
                } else {
                    presentations.add(factory.smallText(c.text))
                }
            }
            val mergedPresentation = mergePresentation(presentations)
            if (mergedPresentation != null) builder.add(mergedPresentation)
            continueProcess()
        }
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon): Boolean {
        //尝试渲染图标
        run {
            val resolved = element.reference?.resolve() ?: return@run
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageManager.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageManager.resolveUrlByFile(resolved.virtualFile, resolved.project, frameInfo)
                else -> null
            }

            //如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return true

            val iconFileUrl = iconUrl.toFileUrl()
            //找不到图标的话就直接跳过
            val icon = iconFileUrl.toIconOrNull() ?: return@run
            //这里需要尝试使用图标的原始高度
            val iconHeight = icon.iconHeight
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { iconHeight }
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            if (originalIconHeight > iconHeightLimit) return true //图标过大，不再尝试渲染
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val presentation = factory.psiSingleReference(factory.smallScaledIcon(icon)) { resolved }
            builder.add(presentation)
            return true
        }

        //直接显示原始文本
        //点击其中的相关文本也能跳转到相关声明
        val presentation = getElementPresentation(element)
        if (presentation != null) builder.add(presentation)
        return continueProcess()
    }

    private fun renderConceptCommandTo(element: ParadoxLocalisationConceptCommand): Boolean {
        //尝试渲染概念文本
        val (referenceElement, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r2@{
            if (richTextList.isNullOrEmpty()) return@r2
            val newBuilder = mutableListOf<InlayPresentation>()
            val oldBuilder = builder
            builder = newBuilder
            var continueProcess = true
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderRichTextTo(richText)
                if (!r) {
                    continueProcess = false
                    break
                }
            }
            builder = oldBuilder
            val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
            var presentation: InlayPresentation = SequencePresentation(newBuilder)

            val attributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
            presentation = WithAttributesPresentation(presentation, conceptAttributesKey, editor, attributesFlags)
            val referencePresentation = factory.psiSingleReference(presentation) { referenceElement }
            presentation = factory.onHover(referencePresentation, object : InlayPresentationFactory.HoverListener {
                override fun onHover(event: MouseEvent, translated: Point) {
                    attributesFlags.isDefault = true //change foreground
                }

                override fun onHoverFinished() {
                    attributesFlags.isDefault = false //reset foreground
                }
            })
            builder.add(presentation)
            if (!continueProcess) return false
            return continueProcess()
        }

        builder.add(factory.smallText(element.name))
        return continueProcess()
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat): Boolean {
        //TODO 1.4.1+ 更完善的支持（适用文本格式）

        //直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return true
        var continueProcess = true
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            val r = renderRichTextTo(richText)
            if (!r) {
                continueProcess = false
                break
            }
        }
        return continueProcess
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon): Boolean {
        //TODO 1.4.1+ 更完善的支持（渲染文本图标）

        //直接显示原始文本
        //点击其中的相关文本也能跳转到相关声明
        val presentation = getElementPresentation(element)
        if (presentation != null) builder.add(presentation)
        return continueProcess()
    }

    private fun mergePresentation(presentations: MutableList<InlayPresentation>): InlayPresentation? {
        return when {
            presentations.isEmpty() -> null
            presentations.size == 1 -> presentations.first()
            else -> SequencePresentation(presentations)
        }
    }

    private fun PresentationFactory.truncatedSmallText(text: String): InlayPresentation {
        val truncatedText = if (textLengthLimit > 0) text.take(truncateRemain.get()) else text
        val truncatedTextSingleLine = truncatedText.substringBefore('\n')
        val finalText = truncatedTextSingleLine
        val result = smallText(finalText)
        if (truncatedTextSingleLine.length != truncatedText.length) {
            lineEnd = true
        }
        if (textLengthLimit > 0) {
            truncateRemain.getAndAdd(-truncatedText.length)
        }
        return result
    }

    private fun continueProcess(): Boolean {
        return !lineEnd && (truncateRemain.get() > 0 || textLengthLimit <= 0)
    }

    private fun getElementPresentation(element: PsiElement): InlayPresentation? {
        val presentations = mutableListOf<InlayPresentation>()

        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            presentations.add(factory.smallText(element.text))
            return mergePresentation(presentations)
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
            //不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is MockPsiElement) {
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
        return mergePresentation(presentations)
    }
}

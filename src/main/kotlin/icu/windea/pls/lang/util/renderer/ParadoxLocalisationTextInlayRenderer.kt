package icu.windea.pls.lang.util.renderer

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.concurrent.atomic.*
import javax.imageio.*

@Suppress("UnstableApiUsage")
object ParadoxLocalisationTextInlayRenderer {
    /**
     * 如果[truncateLimit]小于等于0，则仅渲染首行文本。
     */
    class Context(
        val editor: Editor,
        val factory: PresentationFactory,
        var builder: MutableList<InlayPresentation>
    ) {
        var truncateLimit: Int = -1
        var iconHeightLimit: Int = -1
        val truncateRemain by lazy { AtomicInteger(truncateLimit) } //记录到需要截断为止所剩余的长度
        val guardStack = LinkedList<String>() //防止StackOverflow
    }

    fun render(element: ParadoxLocalisationProperty, factory: PresentationFactory, editor: Editor, truncateLimit: Int, iconHeightLimit: Int): InlayPresentation? {
        //虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        return doRender(factory, editor, truncateLimit, iconHeightLimit) { context ->
            context.guardStack.addLast(element.name)
            renderTo(element, context)
        }
    }

    fun render(element: ParadoxLocalisationPropertyReference, factory: PresentationFactory, editor: Editor, truncateLimit: Int, iconHeightLimit: Int): InlayPresentation? {
        //虽然看起来截断后的长度不正确，但是实际上是正确的，因为图标前后往往存在或不存在神秘的空白
        return doRender(factory, editor, truncateLimit, iconHeightLimit) { context ->
            //context.guardStack.addLast(element.name) //不要加上这段代码
            renderTo(element, context)
        }
    }

    fun renderWithColor(color: Color?, factory: PresentationFactory, editor: Editor, truncateLimit: Int, iconHeightLimit: Int, action: (Context) -> Boolean): InlayPresentation? {
        return doRender(factory, editor, truncateLimit, iconHeightLimit) { context ->
            renderWithColorTo(color, context, action)
        }
    }

    private fun doRender(factory: PresentationFactory, editor: Editor, truncateLimit: Int, iconHeightLimit: Int, action: (Context) -> Boolean): InlayPresentation? {
        val context = Context(editor, factory, mutableListOf())
        context.truncateLimit = truncateLimit
        context.iconHeightLimit = iconHeightLimit
        val r = action(context)
        if (!r) context.builder.add(factory.smallText("...")) //添加省略号
        return mergePresentation(context.builder)
    }

    private fun renderTo(element: ParadoxLocalisationProperty, context: Context): Boolean {
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return true
        var continueProcess = true
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            val r = renderTo(richText, context)
            if (!r) {
                continueProcess = false
                break
            }
        }
        return continueProcess
    }

    private fun renderTo(element: ParadoxLocalisationRichText, context: Context): Boolean {
        return when (element) {
            is ParadoxLocalisationString -> renderStringTo(element, context)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, context)
            is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, context)
            is ParadoxLocalisationCommand -> renderCommandTo(element, context)
            is ParadoxLocalisationIcon -> renderIconTo(element, context)
            is ParadoxLocalisationTextFormat -> renderTextFormatTo(element, context)
            is ParadoxLocalisationTextIcon -> renderTextIconTo(element, context)
            else -> true
        }
    }

    private fun renderWithColorTo(color: Color?, context: Context, action: (Context) -> Boolean): Boolean {
        val textAttributesKey = if (color != null) ParadoxLocalisationAttributesKeys.getColorOnlyKey(color) else null
        val oldBuilder = context.builder
        context.builder = mutableListOf()
        val continueProcess = action(context)
        val newBuilder = context.builder
        context.builder = oldBuilder
        val presentation = mergePresentation(newBuilder) ?: return true
        val finalPresentation = if (textAttributesKey != null) WithAttributesPresentation(presentation, textAttributesKey, context.editor) else presentation
        context.builder.add(finalPresentation)
        return continueProcess
    }

    private fun renderStringTo(element: ParadoxLocalisationString, context: Context): Boolean = with(context.factory) {
        val text = buildString {
            ParadoxEscapeManager.unescapeLocalisationString(element.text, this, ParadoxEscapeManager.Type.Inlay)
        }
        context.builder.add(truncatedSmallText(text, context))
        return continueProcess(context)
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context): Boolean {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return true
        val color = if (getSettings().others.renderLocalisationColorfulText) element.colorInfo?.color else null
        return renderWithColorTo(color, context) {
            var continueProcess = true
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderTo(richText, context)
                if (!r) {
                    continueProcess = false
                    break
                }
            }
            continueProcess
        }
    }

    private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, context: Context): Boolean = with(context.factory) {
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        return renderWithColorTo(color, context) r@{
            //如果处理文本失败，则使用原始文本
            val resolved = element.reference?.resolveLocalisation() //直接解析为本地化以优化性能
                ?: element.scriptedVariableReference?.reference?.resolve()
            val presentation = when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        getElementPresentation(element, context)
                    } else {
                        val resolvedName = resolved.name
                        if (context.guardStack.contains(resolvedName)) {
                            getElementPresentation(element, context)
                        } else {
                            context.guardStack.addLast(resolvedName)
                            try {
                                val oldBuilder = context.builder
                                context.builder = mutableListOf()
                                renderTo(resolved, context)
                                val newBuilder = context.builder
                                context.builder = oldBuilder
                                mergePresentation(newBuilder)
                            } finally {
                                context.guardStack.removeLast()
                            }
                        }
                    }
                }
                resolved is CwtProperty -> {
                    smallText(resolved.value ?: PlsConstants.Strings.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    smallText(resolved.value ?: PlsConstants.Strings.unresolved)
                }
                else -> {
                    truncatedSmallText(element.text, context)
                }
            }
            if (presentation != null) context.builder.add(presentation)
            continueProcess(context)
        }
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context): Boolean = with(context.factory) {
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        return renderWithColorTo(color, context) r@{
            //显示解析后的概念文本
            run {
                val concept = element.concept ?: return@run
                return@r renderConceptTo(concept, context)
            }

            //直接显示命令文本，适用对应的颜色高亮
            //点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc）
            val presentations = mutableListOf<InlayPresentation>()
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    getElementPresentation(c, context)?.let { presentations.add(it) }
                } else {
                    presentations.add(smallText(c.text))
                }
            }
            val mergedPresentation = mergePresentation(presentations)
            if (mergedPresentation != null) context.builder.add(mergedPresentation)
            continueProcess(context)
        }
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context): Boolean = with(context.factory) {
        val resolved = element.reference?.resolve()
        val iconFrame = element.frame
        val frameInfo = ImageFrameInfo.of(iconFrame)
        val iconUrl = when {
            resolved is ParadoxScriptDefinitionElement -> ParadoxImageResolver.resolveUrlByDefinition(resolved, frameInfo)
            resolved is PsiFile -> ParadoxImageResolver.resolveUrlByFile(resolved.virtualFile, frameInfo)
            else -> null
        } ?: ParadoxImageResolver.getDefaultUrl()

        val iconFileUrl = iconUrl.toFileUrl()
        //找不到图标的话就直接跳过
        val icon = iconFileUrl.toIconOrNull() ?: return true
        //这里需要尝试使用图标的原始高度
        val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
        if (originalIconHeight <= context.iconHeightLimit) {
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val presentation = psiSingleReference(smallScaledIcon(icon)) { resolved }
            context.builder.add(presentation)
        } else {
            val unknownIcon = PlsConstants.Paths.unknownPngClasspathUrl.toIconOrNull() ?: return true
            val presentation = psiSingleReference(smallScaledIcon(unknownIcon)) { resolved }
            context.builder.add(presentation)
        }
        return true
    }

    private fun renderConceptTo(element: ParadoxLocalisationConcept, context: Context): Boolean = with(context.factory) {
        val (referenceElement, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r2@{
            if (richTextList == null) return@r2
            val newBuilder = mutableListOf<InlayPresentation>()
            val oldBuilder = context.builder
            context.builder = newBuilder
            var continueProcess = true
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                val r = renderTo(richText, context)
                if (!r) {
                    continueProcess = false
                    break
                }
            }
            context.builder = oldBuilder
            val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
            var presentation: InlayPresentation = SequencePresentation(newBuilder)

            val attributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
            presentation = WithAttributesPresentation(presentation, conceptAttributesKey, context.editor, attributesFlags)
            presentation = onHover(psiSingleReference(presentation) { referenceElement }, object : InlayPresentationFactory.HoverListener {
                override fun onHover(event: MouseEvent, translated: Point) {
                    attributesFlags.isDefault = true //change foreground
                }

                override fun onHoverFinished() {
                    attributesFlags.isDefault = false //reset foreground
                }
            })
            context.builder.add(presentation)
            if (!continueProcess) return false
            return continueProcess(context)
        }
        context.builder.add(smallText(element.name))
        return continueProcess(context)
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat, context: Context): Boolean = with(context.factory) {
        //TODO 1.4.1+ 更完善的支持

        //直接渲染其中的文本，暂不考虑适用文本格式
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return true
        var continueProcess = true
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            val r = renderTo(richText, context)
            if (!r) {
                continueProcess = false
                break
            }
        }
        continueProcess
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon, context: Context): Boolean = with(context.factory) {
        //TODO 1.4.1+ 更完善的支持

        //直接显示原始文本，暂不考虑渲染文本图标
        //点击其中的相关文本也能跳转到相关声明
        val presentation = getElementPresentation(element, context)
        if (presentation != null) context.builder.add(presentation)
        continueProcess(context)
    }

    private fun mergePresentation(presentations: MutableList<InlayPresentation>): InlayPresentation? {
        return when {
            presentations.isEmpty() -> null
            presentations.size == 1 -> presentations.first()
            else -> SequencePresentation(presentations)
        }
    }

    private fun PresentationFactory.truncatedSmallText(text: String, context: Context): InlayPresentation {
        if (context.truncateLimit <= 0) {
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

    private fun getElementPresentation(element: PsiElement, context: Context): InlayPresentation? = with(context.factory) {
        val presentations = mutableListOf<InlayPresentation>()

        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            presentations.add(smallText(element.text))
            return mergePresentation(presentations)
        }
        var i = 0
        for (reference in references) {
            ProgressManager.checkCanceled()
            val startOffset = reference.rangeInElement.startOffset
            if (startOffset != i) {
                val s = text.substring(i, startOffset)
                presentations.add(smallText(s))
            }
            i = reference.rangeInElement.endOffset
            val s = reference.rangeInElement.substring(text)
            val resolved = reference.resolve()
            //不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is ParadoxFakePsiElement) {
                presentations.add(smallText(s))
            } else {
                presentations.add(psiSingleReference(smallText(s)) { reference.resolve() })
            }
        }
        val endOffset = references.last().rangeInElement.endOffset
        if (endOffset != text.length) {
            val s = text.substring(endOffset)
            presentations.add(smallText(s))
        }
        return mergePresentation(presentations)
    }
}

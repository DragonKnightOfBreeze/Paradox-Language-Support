package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.ui.ColorUtil
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.ReferenceLinkService
import icu.windea.pls.lang.codeInsight.documentation.appendImgTag
import icu.windea.pls.lang.codeInsight.documentation.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDocumentationFontSize
import icu.windea.pls.lang.psi.mock.MockPsiElement
import icu.windea.pls.lang.resolveLocalisation
import icu.windea.pls.lang.resolveScriptedVariable
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer.*
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
import icu.windea.pls.model.codeInsight.ReferenceLinkType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.awt.Color
import javax.imageio.ImageIO
import javax.swing.UIManager

/**
 * 用于将本地化文本渲染为 HTML 文本。
 */
@Suppress("unused")
class ParadoxLocalisationTextHtmlRenderer : ParadoxRenderer<PsiElement, Context, String> {
    data class Context(
        var builder: DocumentationBuilder = DocumentationBuilder()
    ) {
        val guardStack: ArrayDeque<String> = ArrayDeque() // 防止 StackOverflow
        val colorStack: ArrayDeque<Color> = ArrayDeque()
    }

    var color: Color? = null
    var forDoc: Boolean = false

    fun withColor(color: Color?) = apply { this.color = color }
    fun forDoc() = apply { forDoc = true }

    override fun initContext(): Context {
        return Context()
    }

    override fun render(input: PsiElement, context: Context): String {
        return when (input) {
            is ParadoxLocalisationProperty -> render(input, context)
            is ParadoxLocalisationRichText -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxLocalisationProperty, context: Context = initContext()): String {
        with(context) { renderProperty(element) }
        return context.builder.content.toString()
    }

    fun render(element: ParadoxLocalisationRichText, context: Context = initContext()): String {
        with(context) { renderRootRichText(element) }
        return context.builder.content.toString()
    }

    context(context: Context)
    private fun renderProperty(element: ParadoxLocalisationProperty) {
        context.guardStack.addLast(element.name)
        try {
            val richTextList = element.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return
            renderWithColor(color) {
                for (richText in richTextList) {
                    ProgressManager.checkCanceled()
                    renderRichText(richText)
                }
            }
        } finally {
            context.guardStack.removeLast()
        }
    }

    context(context: Context)
    private fun renderRootRichText(element: ParadoxLocalisationRichText) {
        renderWithColor(color) {
            renderRichText(element)
        }
    }

    context(context: Context)
    private fun renderWithColor(color: Color?, action: () -> Unit) {
        if (color == null) return action()

        context.colorStack.addLast(color)
        context.builder.append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
        action()
        context.colorStack.removeLast()
        context.builder.append("</span>")
    }

    context(context: Context)
    private fun renderRichText(element: ParadoxLocalisationRichText) {
        when (element) {
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
    private fun renderString(element: ParadoxLocalisationString) {
        val escapeType = ParadoxEscapeManager.Type.Html
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text.escapeXml(), escapeType)
        context.builder.append(text)
    }

    context(context: Context)
    private fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
        // 如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (shouldRenderColurfulText()) element.colorInfo?.color else null
        renderWithColor(color) {
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                renderRichText(richText)
            }
        }
    }

    context(context: Context)
    private fun renderParameter(element: ParadoxLocalisationParameter) {
        // 如果处理文本失败，则使用原始文本
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        renderWithColor(color) {
            // 直接解析为本地化（或者封装变量）以优化性能
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        context.builder.append("<code>")
                        renderElementText(element)
                        context.builder.append("</code>")
                    } else {
                        val resolvedName = resolved.name
                        if (context.guardStack.contains(resolvedName)) {
                            context.builder.append("<code>")
                            renderElementText(element)
                            context.builder.append("</code>")
                        } else {
                            renderProperty(resolved)
                        }
                    }
                }
                resolved is CwtProperty -> {
                    context.builder.append(resolved.value?.escapeXml() ?: PlsStrings.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    context.builder.append(resolved.value?.escapeXml() ?: PlsStrings.unresolved)
                }
                else -> {
                    context.builder.append("<code>")
                    context.builder.append(element.text.escapeXml())
                    context.builder.append("</code>")
                }
            }
        }
    }

    context(context: Context)
    private fun renderIcon(element: ParadoxLocalisationIcon) {
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
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            // 这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            // 如果图标高度在 locFontSize 到 locMaxTextIconSize 之间，则将图标大小缩放到文档字体大小，否则需要基于文档字体大小进行缩放
            // 实际上，本地化文本可以嵌入任意大小的图片
            val docFontSize = getDocumentationFontSize().size
            val locFontSize = PlsInternalSettings.getInstance().localisationFontSize
            val locMaxTextIconSize = PlsInternalSettings.getInstance().localisationTextIconSizeLimit
            val scaleByDocFontSize = when {
                originalIconHeight in locFontSize..locMaxTextIconSize -> docFontSize.toFloat() / originalIconHeight
                else -> docFontSize.toFloat() / locFontSize
            }
            val iconWidth = icon.iconWidth
            val iconHeight = icon.iconHeight
            val scaleByIcon = originalIconHeight.toFloat() / iconHeight
            val scale = scaleByDocFontSize * scaleByIcon
            val finalIconWidth = (iconWidth * scale).toInt()
            val finalIconHeight = (iconHeight * scale).toInt()
            context.builder.appendImgTag(iconUrl, finalIconWidth, finalIconHeight)
            return
        }

        // 直接显示原始文本
        // （仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        context.builder.append("<code>")
        renderElementText(element)
        context.builder.append("</code>")
    }

    context(context: Context)
    private fun renderCommand(element: ParadoxLocalisationCommand) {
        // 如果处理文本失败，则使用原始文本
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (shouldRenderColurfulText()) element.argumentElement?.colorInfo?.color else null
        renderWithColor(color) r@{
            // 直接显示命令文本，适用对应的颜色高亮
            // （仅限快速文档）点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc），但不显示为超链接
            context.builder.append("<code>")
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    renderElementText(c)
                } else {
                    context.builder.append(c.text.escapeXml())
                }
            }
            context.builder.append("</code>")
        }
    }

    context(context: Context)
    private fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
        // 尝试渲染概念文本
        val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
        val editorColorsManager = EditorColorsManager.getInstance()
        val schema = editorColorsManager.schemeForCurrentUITheme
        val conceptColor = schema.getAttributes(conceptAttributesKey).foregroundColor
        val (referenceElement, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r@{
            if (richTextList.isNullOrEmpty()) return@r
            val newBuilder = DocumentationBuilder()
            val oldBuilder = context.builder
            context.builder = newBuilder
            for (richText in richTextList) {
                renderRichText(richText)
            }
            context.builder = oldBuilder
            val conceptText = newBuilder.toString()
            if (referenceElement !is ParadoxScriptDefinitionElement) return@r
            val definitionInfo = referenceElement.definitionInfo ?: return@r
            val definitionName = definitionInfo.name.or.anonymous()
            val definitionType = definitionInfo.type
            renderWithColor(conceptColor) {
                val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType, definitionInfo.gameType)
                context.builder.appendPsiLinkOrUnresolved(link.escapeXml(), conceptText, context = referenceElement)
            }
            return
        }

        renderWithColor(conceptColor) {
            context.builder.append(element.name)
        }
    }

    context(context: Context)
    private fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
        // TODO 1.4.1+ 更完善的支持（渲染文本图标）

        // 直接显示原始文本
        // （仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        context.builder.append("<code>")
        renderElementText(element)
        context.builder.append("</code>")
    }

    context(context: Context)
    private fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
        // TODO 1.4.1+ 更完善的支持（适用文本格式）

        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            renderRichText(richText)
        }
    }

    context(context: Context)
    private fun renderElementText(element: PsiElement) {
        if (!forDoc) {
            context.builder.append(element.text.escapeXml())
            return
        }

        val defaultColor = UIManager.getColor("EditorPane.foreground")

        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            context.builder.append(text.escapeXml())
            return
        }
        var i = 0
        for (reference in references) {
            ProgressManager.checkCanceled()
            val startOffset = reference.rangeInElement.startOffset
            if (startOffset != i) {
                val s = text.substring(i, startOffset)
                context.builder.append(s.escapeXml())
            }
            i = reference.rangeInElement.endOffset
            val resolved = reference.resolve()
            // 不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is MockPsiElement) {
                val s = reference.rangeInElement.substring(text)
                context.builder.append(s.escapeXml())
            } else {
                val link = ReferenceLinkService.createPsiLink(resolved)
                if (link != null) {
                    // 如果没有颜色，这里需要使用文档的默认前景色，以显示为普通文本
                    val usedColor = if (context.colorStack.isEmpty()) defaultColor else null
                    renderWithColor(usedColor) {
                        context.builder.append(link)
                    }
                } else {
                    val s = reference.rangeInElement.substring(text)
                    context.builder.append(s.escapeXml())
                }
            }
        }
        val endOffset = references.last().rangeInElement.endOffset
        if (endOffset != text.length) {
            val s = text.substring(endOffset)
            context.builder.append(s.escapeXml())
        }
    }

    private fun shouldRenderColurfulText(): Boolean {
        return PlsSettings.getInstance().state.others.renderLocalisationColorfulText
    }
}

@file:Suppress("unused")

package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.reference.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.imageio.*
import javax.swing.*

class ParadoxLocalisationTextHtmlRenderer(
    var builder: DocumentationBuilder = DocumentationBuilder(),
    var color: Color? = null,
    var forDoc: Boolean = false,
) {
    private val guardStack = ArrayDeque<String>() //防止StackOverflow
    private val colorStack = ArrayDeque<Color>()

    fun render(element: ParadoxLocalisationProperty): String {
        renderTo(element)
        return builder.content.toString()
    }

    fun renderTo(element: ParadoxLocalisationProperty) {
        guardStack.addLast(element.name)
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        val color = color
        renderWithColorTo(color) {
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                renderRichTextTo(richText)
            }
        }
    }

    fun renderWithColor(color: Color?, action: () -> Unit): String {
        return buildDocumentation { renderWithColorTo(color, action) }
    }

    fun renderWithColorTo(color: Color?, action: () -> Unit) {
        if (color != null) {
            colorStack.addLast(color)
            builder.append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
        }
        action()
        if (color != null) {
            colorStack.removeLast()
            builder.append("</span>")
        }
    }

    private fun renderRichTextTo(element: ParadoxLocalisationRichText) {
        when (element) {
            is ParadoxLocalisationString -> renderStringTo(element)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element)
            is ParadoxLocalisationParameter -> renderParameterTo(element)
            is ParadoxLocalisationCommand -> renderCommandTo(element)
            is ParadoxLocalisationIcon -> renderIconTo(element)
            is ParadoxLocalisationConceptCommand -> renderConceptCommandTo(element)
            is ParadoxLocalisationTextFormat -> renderTextFormatTo(element)
            is ParadoxLocalisationTextIcon -> renderTextIconTo(element)
        }
    }

    private fun renderStringTo(element: ParadoxLocalisationString) {
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text.escapeXml(), ParadoxEscapeManager.Type.Html)
        builder.append(text)
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText) {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.colorInfo?.color else null
        renderWithColorTo(color) {
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                renderRichTextTo(richText)
            }
        }
    }

    private fun renderParameterTo(element: ParadoxLocalisationParameter) {
        //如果处理文本失败，则使用原始文本
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        renderWithColorTo(color) {
            //直接解析为本地化（或者封装变量）以优化性能
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        builder.append("<code>")
                        renderElementText(element)
                        builder.append("</code>")
                    } else {
                        val resolvedName = resolved.name
                        if (guardStack.contains(resolvedName)) {
                            builder.append("<code>")
                            renderElementText(element)
                            builder.append("</code>")
                        } else {
                            try {
                                renderTo(resolved)
                            } finally {
                                guardStack.removeLast()
                            }
                        }
                    }
                }
                resolved is CwtProperty -> {
                    builder.append(resolved.value?.escapeXml() ?: PlsStringConstants.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    builder.append(resolved.value?.escapeXml() ?: PlsStringConstants.unresolved)
                }
                else -> {
                    builder.append("<code>")
                    builder.append(element.text.escapeXml())
                    builder.append("</code>")
                }
            }
        }
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand) {
        //如果处理文本失败，则使用原始文本
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        renderWithColorTo(color) r@{
            //直接显示命令文本，适用对应的颜色高亮
            //（仅限快速文档）点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc），但不显示为超链接
            builder.append("<code>")
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    renderElementText(c)
                } else {
                    builder.append(c.text.escapeXml())
                }
            }
            builder.append("</code>")
        }
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon) {
        //尝试渲染图标
        runCatchingCancelable r@{
            val resolved = element.reference?.resolve() ?: return@r
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageManager.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageManager.resolveUrlByFile(resolved.virtualFile, resolved.project, frameInfo)
                else -> null
            }

            //如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return@r

            val iconFileUrl = iconUrl.toFileUrl()
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            //这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            //如果图标高度在 locFontSize 到 locMaxTextIconSize 之间，则将图标大小缩放到文档字体大小，否则需要基于文档字体大小进行缩放
            //实际上，本地化文本可以嵌入任意大小的图片
            val docFontSize = getDocumentationFontSize().size
            val locFontSize = PlsFacade.getInternalSettings().locFontSize
            val locMaxTextIconSize = PlsFacade.getInternalSettings().locTextIconSizeLimit
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
            builder.appendImgTag(iconUrl, finalIconWidth, finalIconHeight)
            return
        }

        //直接显示原始文本
        //（仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        builder.append("<code>")
        renderElementText(element)
        builder.append("</code>")
    }

    private fun renderConceptCommandTo(element: ParadoxLocalisationConceptCommand) {
        //尝试渲染概念文本
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
        run {
            if (richTextList.isNullOrEmpty()) return@run
            val newBuilder = DocumentationBuilder()
            val oldBuilder = builder
            builder = newBuilder
            for (richText in richTextList) {
                renderRichTextTo(richText)
            }
            builder = oldBuilder
            val conceptText = newBuilder.toString()
            if (referenceElement !is ParadoxScriptDefinitionElement) return@run
            val definitionInfo = referenceElement.definitionInfo ?: return@run
            val definitionName = definitionInfo.name.or.anonymous()
            val definitionType = definitionInfo.type
            renderWithColorTo(conceptColor) {
                val link = ParadoxReferenceLinkType.Definition.createLink(definitionInfo.gameType, definitionName, definitionType)
                builder.appendPsiLinkOrUnresolved(link.escapeXml(), conceptText, context = referenceElement)
            }
            return
        }

        renderWithColorTo(conceptColor) {
            builder.append(element.name)
        }
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat) {
        //TODO 1.4.1+ 更完善的支持（适用文本格式）

        //直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            renderRichTextTo(richText)
        }
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon) {
        //TODO 1.4.1+ 更完善的支持（渲染文本图标）

        //直接显示原始文本
        //（仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        builder.append("<code>")
        renderElementText(element)
        builder.append("</code>")
    }

    private fun renderElementText(element: PsiElement) {
        if (!forDoc) {
            builder.append(element.text.escapeXml())
            return
        }

        val defaultColor = UIManager.getColor("EditorPane.foreground")

        val text = element.text
        val references = element.references
        if (references.isEmpty()) {
            builder.append(text.escapeXml())
            return
        }
        var i = 0
        for (reference in references) {
            ProgressManager.checkCanceled()
            val startOffset = reference.rangeInElement.startOffset
            if (startOffset != i) {
                val s = text.substring(i, startOffset)
                builder.append(s.escapeXml())
            }
            i = reference.rangeInElement.endOffset
            val resolved = reference.resolve()
            //不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is MockPsiElement) {
                val s = reference.rangeInElement.substring(text)
                builder.append(s.escapeXml())
            } else {
                val link = ParadoxReferenceLinkProvider.createPsiLink(resolved)
                if (link != null) {
                    //如果没有颜色，这里需要使用文档的默认前景色，以显示为普通文本
                    val usedColor = if (colorStack.isEmpty()) defaultColor else null
                    renderWithColorTo(usedColor) {
                        builder.append(link)
                    }
                } else {
                    val s = reference.rangeInElement.substring(text)
                    builder.append(s.escapeXml())
                }
            }
        }
        val endOffset = references.last().rangeInElement.endOffset
        if (endOffset != text.length) {
            val s = text.substring(endOffset)
            builder.append(s.escapeXml())
        }
    }
}

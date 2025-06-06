package icu.windea.pls.lang.util.renderer

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.documentation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.imageio.*
import javax.swing.*

object ParadoxLocalisationTextHtmlRenderer {
    class Context(
        var builder: DocumentationBuilder,
        val element: ParadoxLocalisationProperty,
        var color: Color? = null,
        var forDoc: Boolean = false,
    ) {
        val gameType by lazy { selectGameType(element) }

        val guardStack = ArrayDeque<String>() //防止StackOverflow
        val colorStack = ArrayDeque<Color>()
    }

    fun render(element: ParadoxLocalisationProperty, color: Color? = null, forDoc: Boolean = false): String {
        return buildDocumentation { renderTo(this, element, color, forDoc) }
    }

    fun renderTo(builder: DocumentationBuilder, element: ParadoxLocalisationProperty, color: Color? = null, forDoc: Boolean = false) {
        val context = Context(builder, element, color, forDoc)
        context.guardStack.addLast(element.name)
        renderTo(element, context)
    }

    fun renderWithColor(color: Color?, context: Context, action: (Context) -> Unit): String {
        return buildDocumentation { renderWithColorTo(color, context, action) }
    }

    fun renderWithColorTo(color: Color?, context: Context, action: (Context) -> Unit) {
        if (color != null) {
            context.colorStack.addLast(color)
            context.builder.append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
        }
        action(context)
        if (color != null) {
            context.colorStack.removeLast()
            context.builder.append("</span>")
        }
    }

    private fun renderTo(element: ParadoxLocalisationProperty, context: Context) {
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        val color = context.color
        renderWithColorTo(color, context) {
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                renderTo(richText, context)
            }
        }
    }

    private fun renderTo(element: ParadoxLocalisationRichText, context: Context) {
        when (element) {
            is ParadoxLocalisationString -> renderStringTo(element, context)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, context)
            is ParadoxLocalisationParameter -> renderParameterTo(element, context)
            is ParadoxLocalisationCommand -> renderCommandTo(element, context)
            is ParadoxLocalisationIcon -> renderIconTo(element, context)
            is ParadoxLocalisationConceptCommand -> renderConceptTo(element, context)
            is ParadoxLocalisationTextFormat -> renderTextFormatTo(element, context)
            is ParadoxLocalisationTextIcon -> renderTextIconTo(element, context)
        }
    }

    private fun renderStringTo(element: ParadoxLocalisationString, context: Context) {
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text.escapeXml(), ParadoxEscapeManager.Type.Html)
        context.builder.append(text)
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.colorInfo?.color else null
        renderWithColorTo(color, context) {
            for (richText in richTextList) {
                ProgressManager.checkCanceled()
                renderTo(richText, context)
            }
        }
    }

    private fun renderParameterTo(element: ParadoxLocalisationParameter, context: Context) {
        //如果处理文本失败，则使用原始文本
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        renderWithColorTo(color, context) {
            val resolved = element.reference?.resolveLocalisation() //直接解析为本地化以优化性能
                ?: element.scriptedVariableReference?.reference?.resolve()
            when {
                resolved is ParadoxLocalisationProperty -> {
                    if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                        context.builder.append("<code>")
                        renderElementText(element, context)
                        context.builder.append("</code>")
                    } else {
                        val resolvedName = resolved.name
                        if (context.guardStack.contains(resolvedName)) {
                            context.builder.append("<code>")
                            renderElementText(element, context)
                            context.builder.append("</code>")
                        } else {
                            context.guardStack.addLast(resolvedName)
                            try {
                                renderTo(resolved, context)
                            } finally {
                                context.guardStack.removeLast()
                            }
                        }
                    }
                }
                resolved is CwtProperty -> {
                    context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.Strings.unresolved)
                }
                resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                    context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.Strings.unresolved)
                }
                else -> {
                    context.builder.append("<code>")
                    context.builder.append(element.text.escapeXml())
                    context.builder.append("</code>")
                }
            }
        }
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //如果处理文本失败，则使用原始文本
        //如果有颜色码，则使用该颜色渲染，否则保留颜色码

        val color = if (PlsFacade.getSettings().others.renderLocalisationColorfulText) element.argumentElement?.colorInfo?.color else null
        renderWithColorTo(color, context) r@{
            //直接显示命令文本，适用对应的颜色高亮
            //（仅限快速文档）点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc），但不显示为超链接
            context.builder.append("<code>")
            element.forEachChild { c ->
                if (c is ParadoxLocalisationCommandText) {
                    renderElementText(c, context)
                } else {
                    context.builder.append(c.text.escapeXml())
                }
            }
            context.builder.append("</code>")
        }
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        //尝试渲染图标
        run {
            val resolved = element.reference?.resolve()
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageResolver.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageResolver.resolveUrlByFile(resolved.virtualFile, frameInfo)
                else -> null
            }
            if (iconUrl == null) return@run
            val iconFileUrl = iconUrl.toFileUrl()
            val icon = iconFileUrl.toIconOrNull() ?: return@run
            //这里需要尝试使用图标的原始高度
            val iconWidth = icon.iconWidth
            val iconHeight = icon.iconHeight
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrNull() ?: iconHeight
            //如果图标高度在 locFontSize 到 locMaxTextIconSize 之间，则将图标大小缩放到文档字体大小，否则需要基于文档字体大小进行缩放
            //实际上，本地化文本可以嵌入任意大小的图片
            val docFontSize = getDocumentationFontSize().size
            val locFontSize = PlsConstants.Settings.locFontSize
            val locMaxTextIconSize = PlsConstants.Settings.locTextIconSizeLimit
            val scaleByDocFontSize = when {
                originalIconHeight in locFontSize..locMaxTextIconSize -> docFontSize.toFloat() / originalIconHeight
                else -> docFontSize.toFloat() / locFontSize
            }
            val scaleByIcon = originalIconHeight.toFloat() / iconHeight
            val scale = scaleByDocFontSize * scaleByIcon
            val finalIconWidth = (iconWidth * scale).toInt()
            val finalIconHeight = (iconHeight * scale).toInt()
            context.builder.appendImgTag(iconUrl, finalIconWidth, finalIconHeight)
            return
        }

        //直接显示原始文本
        //（仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        context.builder.append("<code>")
        renderElementText(element, context)
        context.builder.append("</code>")
    }

    private fun renderConceptTo(element: ParadoxLocalisationConceptCommand, context: Context) {
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
            val oldBuilder = context.builder
            context.builder = newBuilder
            for (richText in richTextList) {
                renderTo(richText, context)
            }
            context.builder = oldBuilder
            val conceptText = newBuilder.toString()
            if (referenceElement !is ParadoxScriptDefinitionElement) return@run
            val definitionInfo = referenceElement.definitionInfo ?: return@run
            val definitionName = definitionInfo.name.orAnonymous()
            val definitionType = definitionInfo.type
            renderWithColorTo(conceptColor, context) {
                context.builder.appendDefinitionLink(context.gameType.orDefault(), definitionName, definitionType, context.element, conceptText)
            }
            return
        }

        renderWithColorTo(conceptColor, context) {
            context.builder.append(element.name)
        }
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat, context: Context) {
        //TODO 1.4.1+ 更完善的支持（适用文本格式）

        //直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            renderTo(richText, context)
        }
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon, context: Context) {
        //TODO 1.4.1+ 更完善的支持（渲染文本图标）

        //直接显示原始文本
        //（仅限快速文档）点击其中的相关文本也能跳转到相关声明，但不显示为超链接
        context.builder.append("<code>")
        renderElementText(element, context)
        context.builder.append("</code>")
    }

    private fun renderElementText(element: PsiElement, context: Context) {
        if (!context.forDoc) {
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
            //不要尝试跳转到dynamicValue的声明处
            if (resolved == null || resolved is ParadoxFakePsiElement) {
                val s = reference.rangeInElement.substring(text)
                context.builder.append(s.escapeXml())
            } else {
                val link = ParadoxDocumentationLinkProvider.create(resolved)
                if (link != null) {
                    //如果没有颜色，这里需要使用文档的默认前景色，以显示为普通文本
                    val usedColor = if(context.colorStack.isEmpty()) defaultColor else null
                    renderWithColorTo(usedColor, context) {
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
}

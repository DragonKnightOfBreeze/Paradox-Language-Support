package icu.windea.pls.lang.util.renderer

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
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
import java.util.*
import javax.swing.*

object ParadoxLocalisationTextHtmlRenderer {
    class Context(
        var builder: DocumentationBuilder,
        val element: ParadoxLocalisationProperty,
        var color: Color? = null,
        var forDoc: Boolean = false,
    ) {
        val gameType by lazy { selectGameType(element) }

        val guardStack = LinkedList<String>() //防止StackOverflow
        val colorStack = LinkedList<Color>()
    }

    fun render(element: ParadoxLocalisationProperty, color: Color? = null, forDoc: Boolean = false): String {
        return buildDocumentation { renderTo(this, element, color, forDoc) }
    }

    fun renderTo(builder: DocumentationBuilder, element: ParadoxLocalisationProperty, color: Color? = null, forDoc: Boolean = false) {
        val context = Context(builder, element, color, forDoc)
        context.guardStack.addLast(element.name)
        renderTo(element, context)
    }

    private fun renderTo(element: ParadoxLocalisationProperty, context: Context) {
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        val color = context.color
        if (color != null) {
            context.colorStack.addLast(color)
            context.builder.append("<span color=\"").append(color.toHex()).append("\">")
        } else {
            context.builder.append("<span>")
        }
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            renderTo(richText, context)
        }
        context.builder.append("</span>")
    }

    private fun renderTo(element: ParadoxLocalisationRichText, context: Context) {
        when (element) {
            is ParadoxLocalisationString -> renderStringTo(element, context)
            is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, context)
            is ParadoxLocalisationIcon -> renderIconTo(element, context)
            is ParadoxLocalisationCommand -> renderCommandTo(element, context)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, context)
        }
    }

    private fun renderStringTo(element: ParadoxLocalisationString, context: Context) {
        ParadoxEscapeManager.unescapeLocalisationString(element.text.escapeXml(), context.builder.content, ParadoxEscapeManager.Type.Html)
    }

    private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, context: Context) {
        //如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (getSettings().others.renderLocalisationColorfulText) element.colorConfig?.color else null
        if (color != null) {
            context.builder.append("<span style=\"color: #").append(color.toHex()).append("\">")
        }
        val resolved = element.reference?.resolve()
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
                context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.unresolvedString)
            }
            resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.unresolvedString)
            }
            else -> {
                context.builder.append("<code>")
                context.builder.append(element.text.escapeXml())
                context.builder.append("</code>")
            }
        }
        if (color != null) {
            context.builder.append("</span>")
        }
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        val resolved = element.reference?.resolve()
        val iconFrame = element.frame
        val frameInfo = ImageFrameInfo.of(iconFrame)
        val iconUrl = when {
            resolved is ParadoxScriptDefinitionElement -> ParadoxImageResolver.resolveUrlByDefinition(resolved, frameInfo)
            resolved is PsiFile -> ParadoxImageResolver.resolveUrlByFile(resolved.virtualFile, frameInfo)
            else -> null
        } ?: ParadoxImageResolver.getDefaultUrl()

        //找不到图标的话就直接跳过
        val icon = iconUrl.toFileUrl().toIconOrNull() ?: return
        //如果图标大小在16*16到32*32之间，则将图标大小缩放到文档字体大小，否则需要基于文档字体大小进行缩放
        //实际上，本地化文本可以嵌入任意大小的图片
        val docFontSize = getDocumentationFontSize().size
        val scale = when {
            icon.iconHeight in 16..32 -> docFontSize.toFloat() / icon.iconHeight
            else -> docFontSize.toFloat() / 18
        }
        val iconWidth = (icon.iconWidth * scale).toInt()
        val iconHeight = (icon.iconHeight * scale).toInt()
        context.builder.appendImgTag(iconUrl, iconWidth, iconHeight)
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //显示解析后的概念文本
        run r1@{
            val concept = element.concept ?: return@r1
            val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT_KEY
            val conceptColor = EditorColorsManager.getInstance().globalScheme.getAttributes(conceptAttributesKey).foregroundColor
            val (referenceElement, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(concept)
            val richTextList = when {
                textElement is ParadoxLocalisationConceptText -> textElement.richTextList
                textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
                else -> null
            }
            run r2@{
                if (richTextList == null) return@r2
                val newBuilder = DocumentationBuilder()
                val oldBuilder = context.builder
                context.builder = newBuilder
                for (v in richTextList) {
                    renderTo(v, context)
                }
                context.builder = oldBuilder
                val conceptText = newBuilder.toString()
                if (referenceElement !is ParadoxScriptDefinitionElement) return@r2
                val definitionInfo = referenceElement.definitionInfo ?: return@r2
                val definitionName = definitionInfo.name.orAnonymous()
                val definitionType = definitionInfo.type
                if (conceptColor != null) context.builder.append("<span style=\"color: #").append(conceptColor.toHex()).append("\">")
                context.builder.appendDefinitionLink(context.gameType.orDefault(), definitionName, definitionType, context.element, label = conceptText)
                if (conceptColor != null) context.builder.append("</span>")
                return
            }
            if (conceptColor != null) context.builder.append("<span style=\"color: #").append(conceptColor.toHex()).append("\">")
            context.builder.append(concept.name)
            if (conceptColor != null) context.builder.append("</span>")
            return
        }

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

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (getSettings().others.renderLocalisationColorfulText) element.colorConfig?.color else null
        if (color != null) {
            context.colorStack.addLast(color)
            context.builder.append("<span style=\"color: #").append(color.toHex()).append("\">")
        }
        for (richText in richTextList) {
            ProgressManager.checkCanceled()
            renderTo(richText, context)
        }
        if (color != null) {
            context.colorStack.removeLast()
            context.builder.append("</span>")
        }
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
                    val useDefaultColor = context.colorStack.isEmpty()
                    if (useDefaultColor) {
                        context.builder.append("<span style=\"color: #").append(defaultColor.toHex()).append("\">")
                    }
                    context.builder.append(link)
                    if (useDefaultColor) {
                        context.builder.append("</span>")
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

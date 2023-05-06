package icu.windea.pls.tool.localisation

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import java.util.*

@Suppress("unused")
object ParadoxLocalisationTextHtmlRenderer {
    class Context {
        val builder = StringBuilder()
        val guardStack = LinkedList<String>() //防止StackOverflow
    }
    
    fun render(element: ParadoxLocalisationProperty, forDoc: Boolean = false): String {
        val context = Context()
        context.guardStack.addLast(element.name)
        renderTo(element, context)
        return context.builder.toString()
    }
    
    private fun renderTo(element: ParadoxLocalisationProperty, context: Context) {
        val richTextList = element.propertyValue?.richTextList
        if(richTextList == null || richTextList.isEmpty()) return
        for(richText in richTextList) {
            ProgressManager.checkCanceled()
            renderTo(richText, context)
        }
    }
    
    private fun renderTo(element: ParadoxLocalisationRichText, context: Context) {
        when(element) {
            is ParadoxLocalisationString -> renderStringTo(element, context)
            is ParadoxLocalisationEscape -> renderEscapeTo(element, context)
            is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, context)
            is ParadoxLocalisationIcon -> renderIconTo(element, context)
            is ParadoxLocalisationCommand -> renderCommandTo(element, context)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, context)
        }
    }
    
    private fun renderStringTo(element: ParadoxLocalisationString, context: Context) {
        context.builder.append(element.text.escapeXml())
    }
    
    private fun renderEscapeTo(element: ParadoxLocalisationEscape, context: Context) {
        val elementText = element.text
        when {
            elementText == "\\n" -> context.builder.append("<br>\n")
            elementText == "\\r" -> context.builder.append("<br>\r")
            elementText == "\\t" -> context.builder.append("&emsp;")
            elementText.length > 1 -> context.builder.append(elementText[1])
        }
    }
    
    private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, context: Context) {
        //如果处理文本失败，则使用原始文本，如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val colorHex = element.colorConfig?.color?.toHex()
        if(colorHex != null) {
            context.builder.append("<span style=\"color: >").append(colorHex).append("\">")
        }
        val resolved = element.reference?.resolve()
            ?: element.scriptedVariableReference?.reference?.resolve()
        when {
            resolved is ParadoxLocalisationProperty -> {
                val resolvedName = resolved.name
                if(context.guardStack.contains(resolvedName)) {
                    //infinite recursion, do not render context
                    context.builder.append("<code>").append(element.text.escapeXml()).append("</code>")
                } else {
                    context.guardStack.addLast(resolvedName)
                    renderTo(resolved, context)
                    context.guardStack.removeLast()
                }
            }
            resolved is CwtProperty -> {
                context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.unresolvedString)
            }
            resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                context.builder.append(resolved.value?.escapeXml() ?: PlsConstants.unresolvedString)
            }
            else -> {
                context.builder.append("<code>").append(element.text.escapeXml()).append("</code>")
            }
        }
        if(colorHex != null) {
            context.builder.append("</span>")
        }
    }
    
    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        val resolved = element.reference?.resolve()
        val iconFrame = element.frame
        val iconUrl = when {
            resolved is ParadoxScriptDefinitionElement -> ParadoxDdsUrlResolver.resolveByDefinition(resolved, iconFrame, defaultToUnknown = true)
            resolved is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(resolved.virtualFile, iconFrame, defaultToUnknown = true)
            else -> DdsConverter.getUnknownPngUrl()
        }
        if(iconUrl.isNotEmpty()) {
            //找不到图标的话就直接跳过
            val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return
            //基于文档的字体大小缩放图标，直到图标宽度等于字体宽度
            @Suppress("DEPRECATION")
            val docFontSize = DocumentationComponent.getQuickDocFontSize().size //基于这个得到的图标大小并不完美……
            val iconWidth = icon.iconWidth
            val iconHeight = icon.iconHeight
            val usedWidth = docFontSize * iconWidth / iconHeight
            val usedHeight = docFontSize
            context.builder.appendImgTag(iconUrl, usedWidth, usedHeight)
        }
    }
    
    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //直接显示原始文本
        //（仅限快速文档）点击其中的相关文本也能跳转到相关声明（如scope和scripted_loc），但不显示为超链接
        context.builder.append("<code>")
        DocumentationElementLinkProvider.getElementText(context.builder, element, plainLink = true)
        context.builder.append("</code>")
    }
    
    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if(richTextList.isEmpty()) return
        val colorHex = element.colorConfig?.color?.toHex()
        if(colorHex != null) {
            context.builder.append("<span style=\"color: #").append(colorHex).append("\">")
        }
        for(richText in richTextList) {
            ProgressManager.checkCanceled()
            renderTo(richText, context)
        }
        if(colorHex != null) {
            context.builder.append("</span>")
        }
    }
}

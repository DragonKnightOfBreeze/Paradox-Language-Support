package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.core.util.builders.buildDocumentation
import icu.windea.pls.core.util.text.EscapeType
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.ReferenceLinkService
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDocumentationFontSize
import icu.windea.pls.lang.psi.resolveLocalisation
import icu.windea.pls.lang.psi.resolveScriptedVariable
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.builders.appendPsiLinkOrUnresolved
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
import icu.windea.pls.model.codeInsight.ReferenceLinkType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.awt.Color
import javax.imageio.ImageIO
import javax.swing.UIManager

/**
 * 将本地化文本渲染为快速文档中使用的 HTML 文本的渲染器。
 *
 * 说明：
 * - 使用基于 PSI 超链接协议（`psi_element://`）的链接实现目标导航。
 */
class ParadoxLocalisationTextQuickDocRenderer : ParadoxLocalisationTextRenderer<String, ParadoxLocalisationTextQuickDocRenderContext, ParadoxLocalisationTextQuickDocRenderSettings>() {
    override val settings = ParadoxLocalisationTextQuickDocRenderSettings()

    override fun createContext() = ParadoxLocalisationTextQuickDocRenderContext(settings)
}

class ParadoxLocalisationTextQuickDocRenderContext(
    private val settings: ParadoxLocalisationTextQuickDocRenderSettings,
    var builder: DocumentationBuilder = buildDocumentation(),
) : ParadoxLocalisationTextRenderContext<String>() {
    private val guardStack: ArrayDeque<String> = ArrayDeque() // 避免 StackOverflow
    private val colorStack: ArrayDeque<Color> = ArrayDeque()

    override fun build(): String {
        return builder.content.toString()
    }

    override fun renderRootProperty(element: ParadoxLocalisationProperty) {
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        withGuard(guardStack, element) {
            withColorSpan(settings.color) {
                renderRichTexts(richTextList)
            }
        }
    }

    override fun renderRootRichTexts(elements: List<ParadoxLocalisationRichText>) {
        withColorSpan(settings.color) {
            super.renderRootRichTexts(elements)
        }
    }

    override fun renderRootRichText(element: ParadoxLocalisationRichText) {
        withColorSpan(settings.color) {
            super.renderRootRichText(element)
        }
    }

    private inline fun <R> withColorSpan(color: Color?, action: () -> R): R {
        if (color == null) return action()
        return withColor(colorStack, color) {
            try {
                builder.append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
                action()
            } finally {
                builder.append("</span>")
            }
        }
    }

    override fun renderText(element: ParadoxLocalisationText) {
        val text = ParadoxEscapeManager.unescapeLocalisationText(element.text.escapeXml(), EscapeType.Html)
        builder.append(text)
    }

    override fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
        // 如果处理文本失败，则清除非法的颜色标记，直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        val color = if (shouldRenderColorfulText()) element.colorInfo?.color else null
        withColorSpan(color) {
            renderRichTexts(richTextList)
        }
    }

    override fun renderParameter(element: ParadoxLocalisationParameter) {
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColorfulText()) element.argumentElement?.colorInfo?.color else null
        withColorSpan(color) action@{
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
                val v = resolved.value?.escapeXml() ?: FallbackStrings.unresolved
                builder.append(v)
                return@action
            }

            // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明，但不显示为超链接）
            builder.append("<code>")
            renderElementText(element)
            builder.append("</code>")
        }
    }

    private fun renderRichTextsForParameter(richTextList: List<ParadoxLocalisationRichText>) {
        renderRichTexts(richTextList)
    }

    override fun renderIcon(element: ParadoxLocalisationIcon) {
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
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            // 这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            // 如果图标高度在 `localisationFontSize` 到 `localisationTextIconSizeLimit` 之间，则将图标大小缩放到文档字体大小，否则需要基于文档字体大小进行缩放
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
            builder.appendImage(iconUrl, finalIconWidth, finalIconHeight)
            return
        }

        // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明，但不显示为超链接）
        builder.append("<code>")
        renderElementText(element)
        builder.append("</code>")
    }

    override fun renderCommand(element: ParadoxLocalisationCommand) {
        // 如果处理文本失败，则使用原始文本
        // 如果有颜色码，则使用该颜色渲染，否则保留颜色码
        val color = if (shouldRenderColorfulText()) element.argumentElement?.colorInfo?.color else null
        withColorSpan(color) {
            // 直接显示命令文本，适用对应的颜色高亮（点击其中的相关文本也能跳转到相关声明，但不显示为超链接）
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

    override fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
        val conceptAttributesKey = ParadoxLocalisationAttributesKeys.CONCEPT
        val editorColorsManager = EditorColorsManager.getInstance()
        val schema = editorColorsManager.schemeForCurrentUITheme
        val conceptColor = schema.getAttributes(conceptAttributesKey).foregroundColor
        // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
        val (referenceElement, resolved) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        // 概念文本
        run {
            if (resolved !is ParadoxLocalisationConceptText) return@run
            val richTextList = resolved.richTextList
            if (richTextList.isEmpty()) return
            renderRichTextsForConceptCommand(richTextList, referenceElement, conceptColor)
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
                renderRichTextsForConceptCommand(richTextList, referenceElement, conceptColor)
            }
            return
        }

        withColorSpan(conceptColor) {
            // 回退：直接显示原始文本（点击其中的相关文本也能跳转到相关声明，但不显示为超链接）
            builder.append("<code>")
            renderElementText(element)
            builder.append("</code>")
        }
    }

    private fun renderRichTextsForConceptCommand(richTextList: List<ParadoxLocalisationRichText>, referenceElement: PsiElement?, conceptColor: Color) {
        val newBuilder = buildDocumentation()
        val oldBuilder = builder
        builder = newBuilder
        renderRichTexts(richTextList)
        builder = oldBuilder
        val conceptText = newBuilder.toString()
        if (referenceElement !is ParadoxDefinitionElement) return
        val definitionInfo = referenceElement.definitionInfo ?: return
        val definitionName = definitionInfo.name.or.anonymous()
        val definitionType = definitionInfo.type
        withColorSpan(conceptColor) {
            val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType, definitionInfo.gameType)
            builder.appendPsiLinkOrUnresolved(link.escapeXml(), conceptText, context = referenceElement)
        }
    }

    override fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
        // TODO 1.4.1+ 更完善的支持（渲染文本图标）

        // 直接显示原始文本（点击其中的相关文本也能跳转到相关声明，但不显示为超链接）
        builder.append("<code>")
        renderElementText(element)
        builder.append("</code>")
    }

    override fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
        // TODO 1.4.1+ 更完善的支持（适用文本格式）

        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        renderRichTexts(richTextList)
    }

    private fun renderElementText(element: PsiElement) {
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
            // 不要尝试跳转到 dynamicValue 的声明处
            if (resolved == null || resolved is LightElementBase) {
                val s = reference.rangeInElement.substring(text)
                builder.append(s.escapeXml())
            } else {
                val link = ReferenceLinkService.createPsiLink(resolved)
                if (link != null) {
                    // 如果没有颜色，这里需要使用文档的默认前景色，以显示为普通文本
                    val usedColor = if (colorStack.isEmpty()) defaultColor else null
                    withColorSpan(usedColor) {
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

data class ParadoxLocalisationTextQuickDocRenderSettings(
    var color: Color? = null,
) : ParadoxLocalisationTextRenderSettings()

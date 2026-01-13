package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.psi.select.resolveLocalisation
import icu.windea.pls.lang.psi.select.resolveScriptedVariable
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptText
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText
import icu.windea.pls.localisation.psi.ParadoxLocalisationString
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于将本地化文本渲染为去除格式后的纯文本。
 */
@Suppress("unused")
class ParadoxLocalisationTextRenderer : ParadoxRenderer<PsiElement, Context, String> {
    data class Context(
        var builder: StringBuilder = StringBuilder()
    ) {
        val guardStack: ArrayDeque<String> = ArrayDeque()
    }

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
        return context.builder.toString()
    }

    fun render(element: ParadoxLocalisationRichText, context: Context = initContext()): String {
        with(context) { renderRootRichText(element) }
        return context.builder.toString()
    }

    context(context: Context)
    private fun renderProperty(element: ParadoxLocalisationProperty) {
        context.guardStack.addLast(element.name)
        try {
            val richTextList = element.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return
            for (richText in richTextList) {
                renderRichText(richText)
            }
        } finally {
            context.guardStack.removeLast()
        }
    }

    context(context: Context)
    private fun renderRootRichText(element: ParadoxLocalisationRichText) {
        renderRichText(element)
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
        val escapeType = ParadoxEscapeManager.Type.Default
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, escapeType)
        context.builder.append(text)
    }

    context(context: Context)
    private fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
        // 直接渲染其中的文本
        val richTextList = element.richTextList
        if (richTextList.isEmpty()) return
        for (richText in richTextList) {
            renderRichText(richText)
        }
    }

    context(context: Context)
    private fun renderParameter(element: ParadoxLocalisationParameter) {
        // 直接解析为本地化（或者封装变量）以优化性能
        val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
        when {
            resolved is ParadoxLocalisationProperty -> {
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                    context.builder.append(element.text)
                } else {
                    val resolvedName = resolved.name
                    if (context.guardStack.contains(resolvedName)) {
                        // infinite recursion, do not render context
                        context.builder.append(element.text)
                    } else {
                        renderProperty(resolved)
                    }
                }
            }
            resolved is CwtProperty -> {
                context.builder.append(resolved.value)
            }
            resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                context.builder.append(resolved.value)
            }
            else -> {
                context.builder.append(element.text)
            }
        }
    }

    context(context: Context)
    private fun renderIcon(element: ParadoxLocalisationIcon) {
        // 忽略
        // builder.append(":${element.name}:")
    }

    context(context: Context)
    private fun renderCommand(element: ParadoxLocalisationCommand) {
        // 直接显示命令文本
        context.builder.append(element.text)
    }

    context(context: Context)
    private fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
        // 尝试渲染概念文本
        val (_, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r2@{
            if (richTextList.isNullOrEmpty()) return@r2
            for (richText in richTextList) {
                renderRichText(richText)
            }
            return
        }

        context.builder.append(element.text)
        return
    }

    context(context: Context)
    private fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
        // 忽略
        // builder.append(":${element.name}:")
    }

    context(context: Context)
    private fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
        // 直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            renderRichText(richText)
        }
    }
}

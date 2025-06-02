package icu.windea.pls.lang.util.renderer

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationTextRenderer {
    class Context(
        var builder: StringBuilder
    ) {
        val guardStack = ArrayDeque<String>() //防止StackOverflow
    }

    fun render(element: ParadoxLocalisationProperty): String {
        return buildString { renderTo(this, element) }
    }

    fun renderTo(builder: StringBuilder, element: ParadoxLocalisationProperty) {
        val context = Context(builder)
        context.guardStack.addLast(element.name)
        renderTo(element, context)
    }

    private fun renderTo(element: ParadoxLocalisationProperty, context: Context) {
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            renderTo(richText, context)
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
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, ParadoxEscapeManager.Type.Default)
        context.builder.append(text)
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //直接渲染其中的文本
        for (v in element.richTextList) {
            renderTo(v, context)
        }
    }

    private fun renderParameterTo(element: ParadoxLocalisationParameter, context: Context) {
        val resolved = element.reference?.resolveLocalisation() //直接解析为本地化以优化性能
            ?: element.scriptedVariableReference?.reference?.resolve()
        when {
            resolved is ParadoxLocalisationProperty -> {
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                    context.builder.append(element.text)
                } else {
                    val resolvedName = resolved.name
                    if (context.guardStack.contains(resolvedName)) {
                        //infinite recursion, do not render context
                        context.builder.append(element.text)
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

    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //直接显示命令文本
        context.builder.append(element.text)
    }

    private fun renderConceptTo(element: ParadoxLocalisationConceptCommand, context: Context) {
        //尝试渲染概念文本
        val (_, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
        val richTextList = when {
            textElement is ParadoxLocalisationConceptText -> textElement.richTextList
            textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
            else -> null
        }
        run r2@{
            if (richTextList.isNullOrEmpty()) return@r2
            for (richText in richTextList) {
                renderTo(richText, context)
            }
            return
        }

        context.builder.append(element.text)
        return
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        //忽略
        //builder.append(":${element.name}:")
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat, context: Context) {
        //直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            renderTo(richText, context)
        }
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon, context: Context) {
        //忽略
        //builder.append(":${element.name}:")
    }
}

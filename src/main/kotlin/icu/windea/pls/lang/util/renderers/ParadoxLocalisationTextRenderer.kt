package icu.windea.pls.lang.util.renderers

import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolveLocalisation
import icu.windea.pls.lang.resolveScriptedVariable
import icu.windea.pls.lang.util.ParadoxEscapeManager
import icu.windea.pls.lang.util.ParadoxGameConceptManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
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

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationTextRenderer(
    var builder: StringBuilder = StringBuilder()
) {
    private val guardStack = ArrayDeque<String>() //防止StackOverflow

    fun render(element: ParadoxLocalisationProperty): String {
        renderTo(element)
        return builder.toString()
    }

    fun renderTo(element: ParadoxLocalisationProperty) {
        guardStack.addLast(element.name)
        val richTextList = element.propertyValue?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            renderRichTextTo(richText)
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
            else -> throw UnsupportedOperationException()
        }
    }

    private fun renderStringTo(element: ParadoxLocalisationString) {
        val text = ParadoxEscapeManager.unescapeStringForLocalisation(element.text, ParadoxEscapeManager.Type.Default)
        builder.append(text)
    }

    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText) {
        //直接渲染其中的文本
        for (v in element.richTextList) {
            renderRichTextTo(v)
        }
    }

    private fun renderParameterTo(element: ParadoxLocalisationParameter) {
        //直接解析为本地化（或者封装变量）以优化性能
        val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
        when {
            resolved is ParadoxLocalisationProperty -> {
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                    builder.append(element.text)
                } else {
                    val resolvedName = resolved.name
                    if (guardStack.contains(resolvedName)) {
                        //infinite recursion, do not render context
                        builder.append(element.text)
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
                builder.append(resolved.value)
            }
            resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                builder.append(resolved.value)
            }
            else -> {
                builder.append(element.text)
            }
        }
    }

    private fun renderCommandTo(element: ParadoxLocalisationCommand) {
        //直接显示命令文本
        builder.append(element.text)
    }

    private fun renderIconTo(element: ParadoxLocalisationIcon) {
        //忽略
        //builder.append(":${element.name}:")
    }

    private fun renderConceptCommandTo(element: ParadoxLocalisationConceptCommand) {
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
                renderRichTextTo(richText)
            }
            return
        }

        builder.append(element.text)
        return
    }

    private fun renderTextFormatTo(element: ParadoxLocalisationTextFormat) {
        //直接渲染其中的文本
        val richTextList = element.textFormatText?.richTextList
        if (richTextList.isNullOrEmpty()) return
        for (richText in richTextList) {
            renderRichTextTo(richText)
        }
    }

    private fun renderTextIconTo(element: ParadoxLocalisationTextIcon) {
        //忽略
        //builder.append(":${element.name}:")
    }
}

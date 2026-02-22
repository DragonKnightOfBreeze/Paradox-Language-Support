package icu.windea.pls.lang.util.renderers

import icu.windea.pls.core.util.text.EscapeType
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.lang.psi.resolveLocalisation
import icu.windea.pls.lang.psi.resolveScriptedVariable
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
import icu.windea.pls.localisation.psi.ParadoxLocalisationText
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 将本地化文本渲染为去除格式后的纯文本的渲染器。
 */
class ParadoxLocalisationTextPlainRenderer : ParadoxLocalisationTextRenderer<ParadoxLocalisationTextPlainRenderer.Scope, String>() {
    override fun createScope(): Scope {
        return Scope()
    }

    class Scope(
        var builder: StringBuilder = StringBuilder(),
    ) : ParadoxLocalisationTextRenderer.Scope<String>() {
        private val guardStack: ArrayDeque<String> = ArrayDeque() // 避免 StackOverflow

        override fun build(): String {
            return builder.toString()
        }

        override fun renderRootProperty(element: ParadoxLocalisationProperty) {
            val richTextList = element.propertyValue?.richTextList
            if (richTextList.isNullOrEmpty()) return
            withGuard(guardStack, element) {
                renderRichTexts(richTextList)
            }
        }

        override fun renderText(element: ParadoxLocalisationText) {
            val text = ParadoxEscapeManager.unescapeLocalisationText(element.text, EscapeType.Default)
            builder.append(text)
        }

        override fun renderColorfulText(element: ParadoxLocalisationColorfulText) {
            // 直接渲染其中的文本
            val richTextList = element.richTextList
            if (richTextList.isEmpty()) return
            renderRichTexts(richTextList)
        }

        override fun renderParameter(element: ParadoxLocalisationParameter) {
            // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
            val resolved = element.resolveLocalisation() ?: element.resolveScriptedVariable()
            // 本地化
            run {
                if (resolved !is ParadoxLocalisationProperty) return@run
                if (ParadoxLocalisationManager.isSpecialLocalisation(resolved)) return@run // 跳过特殊本地化
                if (checkGuard(guardStack, resolved)) return@run // 跳过存在递归的情况
                val richTextList = resolved.propertyValue?.richTextList
                if (richTextList.isNullOrEmpty()) return
                withGuard(guardStack, resolved) {
                    renderRichTexts(richTextList)
                }
                return
            }
            // 封装变量
            run {
                if (resolved !is ParadoxScriptScriptedVariable) return@run
                val v = resolved.value ?: FallbackStrings.unresolved
                builder.append(v)
                return
            }

            // 回退
            builder.append(element.text)
        }

        override fun renderIcon(element: ParadoxLocalisationIcon) {
            // 忽略
            // builder.append(":${element.name}:")
        }

        override fun renderCommand(element: ParadoxLocalisationCommand) {
            // 直接显示命令文本
            builder.append(element.text)
        }

        override fun renderConceptCommand(element: ParadoxLocalisationConceptCommand) {
            // 尝试渲染解析后的文本，如果处理失败，则使用原始文本
            val (_, resolved) = ParadoxGameConceptManager.getReferenceElementAndTextElement(element)
            // 概念文本
            run {
                if (resolved !is ParadoxLocalisationConceptText) return@run
                val richTextList = resolved.richTextList
                if (richTextList.isEmpty()) return
                renderRichTexts(richTextList)
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
                    renderRichTexts(richTextList)
                }
                return
            }

            // 回退
            builder.append(element.text)
        }

        override fun renderTextIcon(element: ParadoxLocalisationTextIcon) {
            // 忽略
            // builder.append(":${element.name}:")
        }

        override fun renderTextFormat(element: ParadoxLocalisationTextFormat) {
            val richTextList = element.textFormatText?.richTextList
            if (richTextList.isNullOrEmpty()) return
            renderRichTexts(richTextList)
        }
    }
}

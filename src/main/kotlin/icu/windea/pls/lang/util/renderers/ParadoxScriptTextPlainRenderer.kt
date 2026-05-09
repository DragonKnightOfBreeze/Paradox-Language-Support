package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.elementType
import icu.windea.pls.core.findChild
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 将脚本文本渲染为纯文本的渲染器。
 *
 * 说明：
 * - 移除额外的注释、空行和空白，以及不必要的括起表达式的双引号。
 * - 可以在一定程度上配置输出格式。
 */
class ParadoxScriptTextPlainRenderer : ParadoxScriptTextRenderer<String, ParadoxScriptTextPlainRenderContext, ParadoxScriptTextPlainRenderSettings>() {
    override val settings = ParadoxScriptTextPlainRenderSettings()

    override fun createContext() = ParadoxScriptTextPlainRenderContext(settings)
}

open class ParadoxScriptTextPlainRenderContext(
    private val settings: ParadoxScriptTextPlainRenderSettings,
    var builder: StringBuilder = StringBuilder(),
    var depth: Int = 0,
) : ParadoxScriptTextRenderContext<String>() {
    override fun build(): String {
        return builder.toString()
    }

    override fun renderFile(element: ParadoxScriptFile) {
        val members = element.members(settings.conditional, settings.inline)
        val m = OnceMarker()
        for (member in members) {
            ProgressManager.checkCanceled()
            if (m.mark()) renderBlankBetweenMembers()
            renderIndent()
            renderMember(member)
        }
    }

    override fun renderProperty(element: ParadoxScriptProperty) {
        val propertyKey = element.propertyKey
        renderExpressionElement(propertyKey)
        renderSeparator(element)
        val propertyValue = element.propertyValue
        if (propertyValue == null) {
            builder.append(FallbackStrings.unresolved)
            return
        }
        renderValue(propertyValue)
    }

    override fun renderValue(element: ParadoxScriptValue) {
        if (element is ParadoxScriptBlock && settings.renderInBlock) {
            renderLeftBracket()
            val members = element.members(settings.conditional, settings.inline)
            val m = OnceMarker()
            for (member in members) {
                ProgressManager.checkCanceled()
                if (!m.get()) renderBlankAfterLeftBracket()
                if (m.mark()) renderBlankBetweenMembers()
                renderIndent()
                renderMember(member)
            }
            if (m.get()) renderBlankBeforeRightBracket()
            renderRightBracket()
            return
        }
        renderExpressionElement(element)
    }

    override fun renderExpressionElement(element: ParadoxScriptExpressionElement) {
        val resolved = element.resolved()
        val v = when (resolved) {
            is ParadoxScriptInlineMath -> resolved.text
            is ParadoxScriptStringExpressionElement -> resolved.value.quoteIfNecessary()
            else -> resolved?.value
        }
        builder.append(v ?: FallbackStrings.unresolved)
    }

    fun renderIndent() {
        if (!settings.multiline || depth <= 0) return
        builder.append(settings.indent.repeat(depth))
    }

    fun renderSeparator(element: ParadoxScriptProperty) {
        val separator = element.findChild { it.elementType in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS }
        val separatorText = separator?.text ?: "="
        builder.append(" ").append(separatorText).append(" ")
    }

    fun renderLeftBracket() {
        depth++
        builder.append("{")
    }

    fun renderBlankAfterLeftBracket() {
        when {
            settings.multiline -> builder.appendLine()
            else -> builder.append(" ")
        }
    }

    fun renderRightBracket() {
        builder.append("}")
        depth--
    }

    fun renderBlankBeforeRightBracket() {
        when {
            settings.multiline -> {
                builder.appendLine()
                if (depth > 1) {
                    builder.append(settings.indent.repeat(depth - 1))
                }
            }
            else -> builder.append(" ")
        }
    }

    fun renderBlankBetweenMembers() {
        when {
            settings.multiline -> builder.appendLine()
            else -> builder.append(" ")
        }
    }
}

data class ParadoxScriptTextPlainRenderSettings(
    var renderInBlock: Boolean = true,
    var multiline: Boolean = true,
    var indent: String = "    ",
    var inline: Boolean = false,
    var conditional: Boolean = false,
) : ParadoxScriptTextRenderSettings()

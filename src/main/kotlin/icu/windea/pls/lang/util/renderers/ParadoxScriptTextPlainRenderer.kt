package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.elementType
import icu.windea.pls.core.findChild
import icu.windea.pls.core.letIf
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextPlainRenderer.*
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 用于将脚本片段渲染为纯文本。
 */
class ParadoxScriptTextPlainRenderer : ParadoxScriptTextRendererBase<Context, String>() {
    data class Context(
        var builder: StringBuilder = StringBuilder(),
        var depth: Int = 0,
    )

    var renderInBlock: Boolean = true
    var multiline: Boolean = true
    var indent: String = "    "
    var inline: Boolean = false
    var conditional: Boolean = false

    override fun initContext(): Context {
        return Context()
    }

    override fun getOutput(context: Context): String {
        return context.builder.toString()
    }

    context(context: Context)
    override fun renderFile(element: ParadoxScriptFile) {
        val members = element.members(conditional, inline)
        val m = OnceMarker()
        for (member in members) {
            ProgressManager.checkCanceled()
            if (m.mark()) renderBlankBetweenMembers()
            renderIndent()
            renderMember(member)
        }
    }

    context(context: Context)
    override fun renderProperty(element: ParadoxScriptProperty) {
        val propertyKey = element.propertyKey
        renderExpressionElement(propertyKey)
        renderSeparator(element)
        val propertyValue = element.propertyValue
        if (propertyValue == null) {
            context.builder.append(FallbackStrings.unresolved)
            return
        }
        renderValue(propertyValue)
    }

    context(context: Context)
    override fun renderValue(element: ParadoxScriptValue) {
        if (element is ParadoxScriptBlock && renderInBlock) {
            renderLeftBracket()
            val members = element.members(conditional, inline)
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

    context(context: Context)
    override fun renderExpressionElement(element: ParadoxScriptExpressionElement) {
        val resolved = element.resolved()
        val v = resolved?.value?.letIf(resolved is ParadoxScriptStringExpressionElement) { it.quoteIfNecessary() }
        context.builder.append(v ?: FallbackStrings.unresolved)
    }

    context(context: Context)
    private fun renderIndent() {
        if (!multiline || context.depth <= 0) return
        context.builder.append(indent.repeat(context.depth))
    }

    context(context: Context)
    private fun renderSeparator(element: ParadoxScriptProperty) {
        val separator = element.findChild { it.elementType in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS }
        val separatorText = separator?.text ?: "="
        context.builder.append(" ").append(separatorText).append(" ")
    }

    context(context: Context)
    private fun renderLeftBracket() {
        context.depth++
        context.builder.append("{")
    }

    context(context: Context)
    private fun renderBlankAfterLeftBracket() {
        when {
            multiline -> context.builder.appendLine()
            else -> context.builder.append(" ")
        }
    }

    context(context: Context)
    private fun renderRightBracket() {
        context.builder.append("}")
        context.depth--
    }

    context(context: Context)
    private fun renderBlankBeforeRightBracket() {
        when {
            multiline -> {
                context.builder.appendLine()
                if (context.depth > 1) {
                    context.builder.append(indent.repeat(context.depth - 1))
                }
            }
            else -> context.builder.append(" ")
        }
    }

    context(context: Context)
    private fun renderBlankBetweenMembers() {
        when {
            multiline -> context.builder.appendLine()
            else -> context.builder.append(" ")
        }
    }
}

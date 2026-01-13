package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.findChild
import icu.windea.pls.core.letIf
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextRenderer.*
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.value

/**
 * 用于将脚本成员结构渲染为纯文本。
 */
class ParadoxScriptTextRenderer : ParadoxRenderer<PsiElement, Context, String> {
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

    override fun render(input: PsiElement, context: Context): String {
        return when (input) {
            is ParadoxScriptFile -> render(input, context)
            is ParadoxScriptMember -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxScriptFile, context: Context = initContext()): String {
        with(context) { renderFile(element) }
        return context.builder.toString()
    }

    fun render(element: ParadoxScriptMember, context: Context = initContext()): String {
        with(context) { renderMember(element) }
        return context.builder.toString()
    }

    context(context: Context)
    private fun renderFile(element: ParadoxScriptFile) {
        val members = element.members(conditional, inline)
        val m = OnceMarker()
        for (member in members) {
            if (m.mark()) renderBlankBetweenMembers()
            renderIndent()
            renderMember(member)
        }
    }

    context(context: Context)
    private fun renderMember(element: ParadoxScriptMember) {
        when (element) {
            is ParadoxScriptProperty -> renderProperty(element)
            is ParadoxScriptValue -> renderValue(element)
        }
    }

    context(context: Context)
    private fun renderIndent() {
        if (!multiline || context.depth <= 0) return
        context.builder.append(indent.repeat(context.depth))
    }

    context(context: Context)
    private fun renderProperty(element: ParadoxScriptProperty) {
        val propertyKey = element.propertyKey
        renderExpressionElement(propertyKey)
        renderSeparator(element)
        val propertyValue = element.propertyValue
        if (propertyValue == null) {
            context.builder.append(PlsStrings.unresolved)
            return
        }
        renderValue(propertyValue)
    }

    context(context: Context)
    private fun renderValue(element: ParadoxScriptValue) {
        if (element is ParadoxScriptBlock && renderInBlock) {
            renderLeftBracket()
            val members = element.members(conditional, inline)
            val m = OnceMarker()
            for (member in members) {
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
    private fun renderExpressionElement(element: ParadoxScriptExpressionElement) {
        val v = element.value()?.letIf(element is ParadoxScriptStringExpressionElement) { it.quoteIfNecessary() }
        context.builder.append(v ?: PlsStrings.unresolved)
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

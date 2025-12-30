package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.options
import icu.windea.pls.core.findChild
import icu.windea.pls.core.letIf
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.psi.conditional
import icu.windea.pls.lang.psi.inline
import icu.windea.pls.lang.psi.members
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.value

/**
 * 用于将脚本成员结构渲染为纯文本。
 */
class ParadoxScriptTextRenderer : ParadoxRenderer<PsiElement, ParadoxScriptTextRenderer.Context, String> {
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

    fun render(element: ParadoxScriptFile, context: Context): String {
        with(context) { renderRoot(element) }
        return context.builder.toString()
    }

    fun render(element: ParadoxScriptMember, context: Context): String {
        with(context) { renderRoot(element) }
        return context.builder.toString()
    }

    context(context: Context)
    private fun renderRoot(element: PsiElement) {
        when (element) {
            is ParadoxScriptFile -> renderFile(element)
            is ParadoxScriptProperty -> renderProperty(element)
            is ParadoxScriptValue -> renderValue(element)
            else -> throw UnsupportedOperationException()
        }
    }

    context(context: Context)
    private fun renderFile(element: ParadoxScriptFile) {
        val members = getMembers(element)
        val m = OnceMarker()
        for (member in members) {
            if (m.mark()) renderBlankBetweenMembers()
            renderMember(member)
        }
    }

    context(context: Context)
    private fun renderMember(element: ParadoxScriptMember) {
        when (element) {
            is ParadoxScriptProperty -> {
                renderIndent()
                renderProperty(element)
            }
            is ParadoxScriptValue -> {
                renderIndent()
                renderValue(element)
            }
        }
    }

    context(context: Context)
    private fun renderProperty(element: ParadoxScriptProperty) {
        val propertyKey = element.propertyKey
        renderExpressionElement(propertyKey)
        renderSeparator(element)
        val propertyValue = element.propertyValue
        if (propertyValue == null) {
            context.builder.append(PlsStringConstants.unresolved)
            return
        }
        renderValue(propertyValue)
    }

    context(context: Context)
    private fun renderValue(element: ParadoxScriptValue) {
        if (element is ParadoxScriptBlock && renderInBlock) {
            renderLeftBracket()
            val members = getMembers(element)
            val m = OnceMarker()
            for (member in members) {
                if (m.mark()) renderBlankBetweenMembers()
                renderMember(member)
            }
            renderRightBracket()
            return
        }
        renderExpressionElement(element)
    }

    context(context: Context)
    private fun renderExpressionElement(element: ParadoxScriptExpressionElement) {
        val v = element.value()?.letIf(element is ParadoxScriptStringExpressionElement) { it.quoteIfNecessary() }
        context.builder.append(v ?: PlsStringConstants.unresolved)
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
        when {
            multiline -> context.builder.appendLine()
            else -> context.builder.append(" ")
        }
    }

    context(context: Context)
    private fun renderRightBracket() {
        when {
            multiline -> {
                context.builder.appendLine()
                if (context.depth > 1) {
                    context.builder.append(indent.repeat(context.depth - 1))
                }
            }
            else -> context.builder.append(" ")
        }
        context.builder.append("}")
        context.depth--
    }

    context(context: Context)
    private fun renderIndent() {
        if (!multiline || context.depth <= 0) return
        context.builder.append(indent.repeat(context.depth))
    }

    context(context: Context)
    private fun renderBlankBetweenMembers() {
        when {
            multiline -> context.builder.appendLine()
            else -> context.builder.append(" ")
        }
    }

    context(_: Context)
    private fun getMembers(element: ParadoxScriptMemberContainer): WalkingSequence<ParadoxScriptMember> {
        val inlineOption = this@ParadoxScriptTextRenderer.inline
        val conditionalOption = this@ParadoxScriptTextRenderer.conditional
        return element.members().options { inline(inlineOption).conditional(conditionalOption) }
    }
}

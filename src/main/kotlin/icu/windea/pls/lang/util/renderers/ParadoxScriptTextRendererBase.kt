package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

abstract class ParadoxScriptTextRendererBase<C, R> : ParadoxScriptTextRenderer<C, R> {
    final override fun render(input: PsiElement, context: C): R {
        return when (input) {
            is ParadoxScriptFile -> render(input, context)
            is ParadoxScriptMember -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxScriptFile, context: C = initContext()): R {
        with(context) { renderFile(element) }
        return getOutput(context)
    }

    fun render(element: ParadoxScriptMember, context: C = initContext()): R {
        with(context) { renderMember(element) }
        return getOutput(context)
    }

    protected abstract fun getOutput(context: C): R

    context(context: C)
    protected abstract fun renderFile(element: ParadoxScriptFile)

    context(context: C)
    protected open fun renderMember(element: ParadoxScriptMember) {
        when (element) {
            is ParadoxScriptProperty -> renderProperty(element)
            is ParadoxScriptValue -> renderValue(element)
            else -> throw UnsupportedOperationException("Unsupported member element type: ${element.elementType}")
        }
    }

    context(context: C)
    protected abstract fun renderProperty(element: ParadoxScriptProperty)

    context(context: C)
    protected abstract fun renderValue(element: ParadoxScriptValue)

    context(context: C)
    protected abstract fun renderExpressionElement(element: ParadoxScriptExpressionElement)
}

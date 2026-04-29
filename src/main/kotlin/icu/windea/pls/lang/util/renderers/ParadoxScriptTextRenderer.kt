package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 脚本文本的渲染器。
 */
abstract class ParadoxScriptTextRenderer<T, C : ParadoxScriptTextRenderContext<T>, S : ParadoxScriptTextRenderSettings> : ParadoxRenderer<T, C, S> {
    final override fun render(input: PsiElement, context: C): T {
        return when (input) {
            is ParadoxScriptFile -> render(input, context)
            is ParadoxScriptMember -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxScriptFile, context: C = createContext()): T {
        context.renderFile(element)
        return context.build()
    }

    fun render(element: ParadoxScriptMember, context: C = createContext()): T {
        context.renderRootMember(element)
        return context.build()
    }
}

abstract class ParadoxScriptTextRenderContext<R> : ParadoxRenderContext<R> {
    abstract fun renderFile(element: ParadoxScriptFile)

    open fun renderRootMember(element: ParadoxScriptMember) {
        renderMember(element)
    }

    open fun renderMember(element: ParadoxScriptMember) {
        when (element) {
            is ParadoxScriptProperty -> renderProperty(element)
            is ParadoxScriptValue -> renderValue(element)
            else -> throw UnsupportedOperationException("Unsupported member element type: ${element.elementType}")
        }
    }

    abstract fun renderProperty(element: ParadoxScriptProperty)

    abstract fun renderValue(element: ParadoxScriptValue)

    abstract fun renderExpressionElement(element: ParadoxScriptExpressionElement)
}

abstract class ParadoxScriptTextRenderSettings : ParadoxRenderSettings

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
abstract class ParadoxScriptTextRenderer<C : ParadoxScriptTextRenderer.Scope<R>, R> : ParadoxRenderer<C, R> {
    final override fun render(input: PsiElement, scope: C): R {
        return when (input) {
            is ParadoxScriptFile -> render(input, scope)
            is ParadoxScriptMember -> render(input, scope)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxScriptFile, scope: C = createScope()): R {
        scope.renderFile(element)
        return scope.build()
    }

    fun render(element: ParadoxScriptMember, scope: C = createScope()): R {
        scope.renderRootMember(element)
        return scope.build()
    }

    abstract class Scope<R> : ParadoxRenderer.Scope<R> {
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
}

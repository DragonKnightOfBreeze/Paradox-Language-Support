package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRowElement

/**
 * CSV 文本的渲染器。
 */
abstract class ParadoxCsvTextRenderer<S : ParadoxCsvTextRenderer.Scope<R>, R> : ParadoxRenderer<S, R> {
    final override fun render(input: PsiElement, scope: S): R {
        return when (input) {
            is ParadoxCsvFile -> render(input, scope)
            is ParadoxCsvRowElement -> render(input, scope)
            is ParadoxCsvColumn -> render(input, scope)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxCsvFile, scope: S = createScope()): R {
        scope.renderFile(element)
        return scope.build()
    }

    fun render(element: ParadoxCsvRowElement, scope: S = createScope()): R {
        scope.renderRowElement(element)
        return scope.build()
    }

    fun render(element: ParadoxCsvColumn, scope: S = createScope()): R {
        scope.renderColumn(element)
        return scope.build()
    }

    abstract class Scope<R> : ParadoxRenderer.Scope<R> {
        abstract fun renderFile(element: ParadoxCsvFile)

        abstract fun renderRowElement(element: ParadoxCsvRowElement)

        abstract fun renderColumn(element: ParadoxCsvColumn)
    }
}

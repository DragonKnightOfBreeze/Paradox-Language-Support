package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRowElement

abstract class ParadoxCsvTextRendererBase<C, R> : ParadoxCsvTextRenderer<C, R> {
    final override fun render(input: PsiElement, context: C): R {
        return when (input) {
            is ParadoxCsvFile -> render(input, context)
            is ParadoxCsvRowElement -> render(input, context)
            is ParadoxCsvColumn -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxCsvFile, context: C = initContext()): R {
        with(context) { renderFile(element) }
        return getOutput(context)
    }

    fun render(element: ParadoxCsvRowElement, context: C = initContext()): R {
        with(context) { renderRowElement(element) }
        return getOutput(context)
    }

    fun render(element: ParadoxCsvColumn, context: C = initContext()): R {
        with(context) { renderColumn(element) }
        return getOutput(context)
    }

    protected abstract fun getOutput(context: C): R

    context(context: C)
    protected abstract fun renderFile(element: ParadoxCsvFile)

    context(context: C)
    protected abstract fun renderRowElement(element: ParadoxCsvRowElement)

    context(context: C)
    protected abstract fun renderColumn(element: ParadoxCsvColumn)
}

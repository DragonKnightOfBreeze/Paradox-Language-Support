package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRowElement

/**
 * CSV 文本的渲染器。
 */
abstract class ParadoxCsvTextRenderer<T, S : ParadoxCsvTextRenderSettings, C : ParadoxCsvTextRenderContext<T>> : ParadoxRenderer<T, C, S> {
    final override fun render(input: PsiElement, context: C): T {
        return when (input) {
            is ParadoxCsvFile -> render(input, context)
            is ParadoxCsvRowElement -> render(input, context)
            is ParadoxCsvColumn -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxCsvFile, context: C = createContext()): T {
        context.renderFile(element)
        return context.build()
    }

    fun render(element: ParadoxCsvRowElement, context: C = createContext()): T {
        context.renderRowElement(element)
        return context.build()
    }

    fun render(element: ParadoxCsvColumn, context: C = createContext()): T {
        context.renderColumn(element)
        return context.build()
    }
}

abstract class ParadoxCsvTextRenderSettings : ParadoxRenderSettings

abstract class ParadoxCsvTextRenderContext<T> : ParadoxRenderContext<T> {
    abstract fun renderFile(element: ParadoxCsvFile)

    abstract fun renderRowElement(element: ParadoxCsvRowElement)

    abstract fun renderColumn(element: ParadoxCsvColumn)
}

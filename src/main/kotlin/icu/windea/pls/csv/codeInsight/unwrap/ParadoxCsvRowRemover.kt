package icu.windea.pls.csv.codeInsight.unwrap

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.indexOfLineEnd
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvRow

class ParadoxCsvRowRemover : ParadoxCsvUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxCsvRow
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxCsvRow) return "" // unexpected
        return ChronicleBundle.message("csv.remove.row")
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxCsvRow) return // unexpected
        removeLineEnd(element, context) // remove ending line break of current row
        context.delete(element)
    }

    private fun removeLineEnd(element: PsiElement, context: Context) {
        val nextElement = element.nextSibling
        if (nextElement !is PsiWhiteSpace) return
        val text = nextElement.text
        val lineEndIndex = text.indexOfLineEnd()
        if (lineEndIndex == -1) return
        if (lineEndIndex == text.length) {
            context.delete(element)
        } else {
            val newText = text.substring(lineEndIndex)
            element.replace(ParadoxCsvElementFactory.createWhiteSpaceFromText(element.project, newText))
        }
    }
}

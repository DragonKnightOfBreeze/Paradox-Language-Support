package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.util.*

class ParadoxCsvAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is ParadoxCsvExpressionElement) {
            annotateExpression(element, holder)
        }
    }

    private fun annotateExpression(element: ParadoxCsvExpressionElement, holder: AnnotationHolder) {
        //不高亮表格头中的列
        if(element is ParadoxCsvColumn && element.isHeaderColumn()) return

        val columnConfig = when (element) {
            is ParadoxCsvColumn -> ParadoxCsvManager.getColumnConfig(element)
            else -> null
        }
        val config = columnConfig?.valueConfig ?: return
        ParadoxExpressionManager.annotateCsvExpression(element, null, holder, config)
    }
}

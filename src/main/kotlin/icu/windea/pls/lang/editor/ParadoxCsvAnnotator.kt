package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxExpressionManager

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

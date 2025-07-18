package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager

class ParadoxCsvAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is ParadoxCsvExpressionElement) {
            annotateExpression(element, holder)
        }
    }

    private fun annotateExpression(element: ParadoxCsvExpressionElement, holder: AnnotationHolder) {
        ParadoxExpressionManager.annotateCsvExpression(element, null, holder)
    }
}

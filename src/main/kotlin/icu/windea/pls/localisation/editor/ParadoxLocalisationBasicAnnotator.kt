package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationBasicAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)
    }

    private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
        //by @雪丶我
        //不允许紧邻的图标
        if (element is ParadoxLocalisationIcon && element.prevSibling is ParadoxLocalisationIcon) {
            holder.newAnnotation(HighlightSeverity.ERROR, PlsBundle.message("localisation.annotator.neighboringIcon"))
                .withFix(InsertStringFix(PlsBundle.message("localisation.annotator.neighboringIcon.fix"), " ", element.startOffset))
                .create()
        }
    }
}

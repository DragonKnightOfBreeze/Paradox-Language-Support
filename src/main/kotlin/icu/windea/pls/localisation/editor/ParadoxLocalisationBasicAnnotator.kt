package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.InsertStringFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

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

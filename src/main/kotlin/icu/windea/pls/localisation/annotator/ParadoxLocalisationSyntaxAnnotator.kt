package icu.windea.pls.localisation.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fixes.InsertStringFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

class ParadoxLocalisationSyntaxAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkAdjacentIcon(element, holder)
    }

    private fun checkAdjacentIcon(element: PsiElement, holder: AnnotationHolder) {
        // by @雪丶我
        // 不允许紧邻的图标
        if (element !is ParadoxLocalisationIcon) return
        if (element.prevSibling !is ParadoxLocalisationIcon) return
        holder.newAnnotation(HighlightSeverity.ERROR, PlsBundle.message("message.adjacent.icon.unexpected"))
            .withFix(InsertStringFix(element, PlsBundle.message("fix.adjacent.icon.unexpected"), " ", element.startOffset))
            .create()
    }
}

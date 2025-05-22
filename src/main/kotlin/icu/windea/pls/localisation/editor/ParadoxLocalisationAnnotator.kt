package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys as Keys

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)

        when (element) {
            //is ParadoxLocalisationProperty -> annotateProperty(element, holder)
            is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
            is ParadoxLocalisationParameter -> annotateParameter(element, holder)
            is ParadoxLocalisationCommand -> annotateCommand(element, holder)
            is ParadoxLocalisationExpressionElement -> annotateExpression(element, holder)
        }
    }

    private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
        //by @雪丶我
        //不允许紧邻的图标
        if (element is ParadoxLocalisationIcon && element.prevSibling is ParadoxLocalisationIcon) {
            holder.newAnnotation(ERROR, PlsBundle.message("localisation.annotator.neighboringIcon"))
                .withFix(InsertStringFix(PlsBundle.message("localisation.annotator.neighboringIcon.fix"), " ", element.startOffset))
                .create()
        }
    }

    //private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
    //    val localisationInfo = element.localisationInfo
    //    if (localisationInfo != null) annotateLocalisation(element, holder, localisationInfo)
    //}
    //
    //private fun annotateLocalisation(element: ParadoxLocalisationProperty, holder: AnnotationHolder, localisationInfo: ParadoxLocalisationInfo) {
    //    //目前不需要作任何处理
    //}

    private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
        annotateTextColor(element, holder)
    }

    private fun annotateParameter(element: ParadoxLocalisationParameter, holder: AnnotationHolder) {
        annotateByArgument(element, holder)
    }

    private fun annotateCommand(element: ParadoxLocalisationCommand, holder: AnnotationHolder) {
        annotateByArgument(element, holder)
    }

    private fun annotateByArgument(element: ParadoxLocalisationArgumentAwareElement, holder: AnnotationHolder) {
        val argumentElement = element.argumentElement ?: return
        if (argumentElement is ParadoxLocalisationTextColorAwareElement) {
            annotateTextColor(argumentElement, holder)
        }
    }

    private fun annotateTextColor(element: ParadoxLocalisationTextColorAwareElement, holder: AnnotationHolder) {
        //颜色高亮
        val color = element.colorInfo?.color ?: return
        val attributesKey = Keys.getColorKey(color) ?: return
        val (idElement, idOffset) = ParadoxTextColorManager.getIdElementAndOffset(element) ?: return
        if (idOffset == -1) return
        val range = TextRange.from(idOffset + idElement.startOffset, 1)
        holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
    }

    private fun annotateExpression(element: ParadoxLocalisationExpressionElement, holder: AnnotationHolder) {
        ParadoxExpressionManager.annotateLocalisationExpression(element, null, holder)
    }
}

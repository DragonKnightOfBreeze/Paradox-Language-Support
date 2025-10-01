package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.lang.resolveLocalisation
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationArgumentAwareElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextColorAwareElement

class ParadoxLocalisationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
            is ParadoxLocalisationParameter -> annotateParameter(element, holder)
            is ParadoxLocalisationCommand -> annotateCommand(element, holder)
            is ParadoxLocalisationExpressionElement -> annotateExpression(element, holder)
        }
    }

    private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
        annotateTextColor(element, holder)
    }

    private fun annotateParameter(element: ParadoxLocalisationParameter, holder: AnnotationHolder) {
        run {
            // 如果可以被解析为本地化，则高亮为本地化引用
            if (element.resolveLocalisation() == null) return@run
            val idElement = element.idElement ?: return@run
            val attributesKey = ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE_KEY
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(idElement).textAttributes(attributesKey).create()
        }

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
        // 颜色高亮
        val color = element.colorInfo?.color ?: return
        val attributesKey = ParadoxLocalisationAttributesKeys.getColorKey(color) ?: return
        val (idElement, idOffset) = ParadoxTextColorManager.getIdElementAndOffset(element) ?: return
        if (idOffset == -1) return
        val range = TextRange.from(idOffset + idElement.startOffset, 1)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
    }

    private fun annotateExpression(element: ParadoxLocalisationExpressionElement, holder: AnnotationHolder) {
        ParadoxExpressionManager.annotateLocalisationExpression(element, null, holder)
    }
}


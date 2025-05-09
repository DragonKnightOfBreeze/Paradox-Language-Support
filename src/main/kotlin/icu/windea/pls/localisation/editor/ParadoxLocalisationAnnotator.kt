package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys as Keys

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)

        when (element) {
            is ParadoxLocalisationProperty -> annotateProperty(element, holder)
            is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
            is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
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

    private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
        val localisationInfo = element.localisationInfo
        if (localisationInfo != null) annotateLocalisation(element, holder, localisationInfo)
    }

    private fun annotateLocalisation(element: ParadoxLocalisationProperty, holder: AnnotationHolder, localisationInfo: ParadoxLocalisationInfo) {
        //颜色高亮（并非特别必要，注释掉）
        //val category = localisationInfo.category
        //val attributesKey = when(category) {
        //	ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationAttributesKeys.LOCALISATION_KEY
        //	ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY
        //}
        //holder.newSilentAnnotation(INFORMATION)
        //	.range(element.propertyKey)
        //	.textAttributes(attributesKey)
        //	.create()
    }

    private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
        //颜色高亮
        val location = element.idElement ?: return
        val attributesKey = element.reference?.getAttributesKey() ?: return
        holder.newSilentAnnotation(INFORMATION).range(location).textAttributes(attributesKey).create()
    }

    private fun annotatePropertyReference(element: ParadoxLocalisationPropertyReference, holder: AnnotationHolder) {
        annotateByArgument(element.argumentElement, holder)
    }

    private fun annotateCommand(element: ParadoxLocalisationCommand, holder: AnnotationHolder) {
        annotateByArgument(element.argumentElement, holder)
    }

    private fun annotateByArgument(element: ParadoxLocalisationArgument?, holder: AnnotationHolder) {
        if (element == null) return
        val text = element.text
        val textColorCharIndex = text.indexOfLast { ParadoxLocalisationArgumentManager.isTextColorChar(it) }
        if(textColorCharIndex == -1) return
        val colorId = text[textColorCharIndex]
        val colorConfig = ParadoxTextColorManager.getInfo(colorId.toString(), element.project, element) ?: return
        val attributesKey = Keys.getColorKey(colorConfig.color) ?: return
        val colorIdOffset = element.startOffset + text.indexOf(colorId)
        val range = TextRange.create(colorIdOffset, colorIdOffset + 1)
        holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
    }

    private fun annotateExpression(element: ParadoxLocalisationExpressionElement, holder: AnnotationHolder) {
        ParadoxExpressionManager.annotateExpression(element, null, holder)
    }
}

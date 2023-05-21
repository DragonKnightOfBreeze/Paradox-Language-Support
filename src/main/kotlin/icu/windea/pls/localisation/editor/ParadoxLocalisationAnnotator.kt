package icu.windea.pls.localisation.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.highlighter.ParadoxLocalisationAttributesKeys as Keys

@Suppress("UNUSED_PARAMETER")
class ParadoxLocalisationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        check(element, holder)
        when(element) {
            is ParadoxLocalisationProperty -> annotateProperty(element, holder)
            is ParadoxLocalisationPropertyReference -> annotatePropertyReference(element, holder)
            is ParadoxLocalisationColorfulText -> annotateColorfulText(element, holder)
            is ParadoxLocalisationCommandScope -> annotateCommandScope(element, holder)
            is ParadoxLocalisationCommandField -> annotateCommandField(element, holder)
        }
    }
    
    private fun check(element: PsiElement, holder: AnnotationHolder) {
        //不允许紧接的图标
        if(element is ParadoxLocalisationIcon && element.prevSibling is ParadoxLocalisationIcon) {
            val startOffset = element.startOffset
            holder.newAnnotation(ERROR, PlsBundle.message("localisation.annotator.adjacentIcon"))
                .range(TextRange.create(startOffset, startOffset + 1)) //icon prefix
                .withFix(InsertStringFix(" ", startOffset))
                .create()
        }
    }
    
    private fun annotateProperty(element: ParadoxLocalisationProperty, holder: AnnotationHolder) {
        val localisationInfo = element.localisationInfo
        if(localisationInfo != null) annotateLocalisation(element, holder, localisationInfo)
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
    
    private fun annotatePropertyReference(element: ParadoxLocalisationPropertyReference, holder: AnnotationHolder) {
        //颜色高亮
        val colorConfig = element.colorConfig
        if(colorConfig != null) {
            val location = element.propertyReferenceParameter
            if(location != null) {
                val range = location.textRange
                val attributesKey = Keys.getColorKey(colorConfig.color) ?: return
                holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
            }
        }
    }
    
    private fun annotateColorfulText(element: ParadoxLocalisationColorfulText, holder: AnnotationHolder) {
        //颜色高亮
        val location = element.idElement ?: return
        val attributesKey = element.reference?.getAttributesKey() ?: return
        holder.newSilentAnnotation(INFORMATION).range(location).textAttributes(attributesKey).create()
    }
    
    private fun annotateCommandScope(element: ParadoxLocalisationCommandScope, holder: AnnotationHolder) {
        //颜色高亮
        val attributesKey = element.reference.getAttributesKey() ?: return
        holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
    }
    
    private fun annotateCommandField(element: ParadoxLocalisationCommandField, holder: AnnotationHolder) {
        //颜色高亮
        val attributesKey = element.reference?.getAttributesKey() ?: return
        holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKey).create()
    }
}

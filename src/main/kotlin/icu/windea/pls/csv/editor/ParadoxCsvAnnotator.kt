package icu.windea.pls.csv.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.model.*

class ParadoxCsvAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxCsvColumn -> annotateColumn(element, holder)
        }
    }

    private fun annotateColumn(element: ParadoxCsvColumn, holder: AnnotationHolder) {
        // annotate column by resolved type
        val resolvedType = ParadoxTypeResolver.resolve(element.value)
        val attributesKeys = when (resolvedType) {
            ParadoxType.Int -> ParadoxCsvAttributesKeys.NUMBER_KEY
            ParadoxType.Float -> ParadoxCsvAttributesKeys.NUMBER_KEY
            ParadoxType.Boolean -> ParadoxCsvAttributesKeys.KEYWORD_KEY
            else -> null
        }
        if (attributesKeys == null) return
        holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKeys).create()
    }
}

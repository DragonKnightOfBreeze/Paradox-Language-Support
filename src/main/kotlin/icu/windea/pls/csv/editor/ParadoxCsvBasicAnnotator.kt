package icu.windea.pls.csv.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.model.*

class ParadoxCsvBasicAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxCsvColumn -> annotateColumn(element, holder)
        }
    }

    private fun annotateColumn(element: ParadoxCsvColumn, holder: AnnotationHolder) {
        val attributesKeys = getAttributesKey(element)
        if (attributesKeys == null) return
        holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(attributesKeys).create()
    }

    private fun getAttributesKey(element: ParadoxCsvColumn): TextAttributesKey? {
        if (element.isHeaderColumn()) return ParadoxCsvAttributesKeys.HEADER_KEY

        val resolvedType = ParadoxTypeResolver.resolve(element.value)
        val attributesKeys = when (resolvedType) {
            ParadoxType.Int -> ParadoxCsvAttributesKeys.NUMBER_KEY
            ParadoxType.Float -> ParadoxCsvAttributesKeys.NUMBER_KEY
            ParadoxType.Boolean -> ParadoxCsvAttributesKeys.KEYWORD_KEY
            //ParadoxType.String -> ParadoxCsvAttributesKeys.STRING_KEY
            else -> null
        }
        return attributesKeys
    }
}

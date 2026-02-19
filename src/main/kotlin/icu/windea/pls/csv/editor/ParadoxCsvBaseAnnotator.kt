package icu.windea.pls.csv.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.model.ParadoxType

/**
 * 用于在 CSV 文件中提供基础的语义高亮。
 *
 * - 对于列（头列），提供特殊高亮。
 * - 对于列（非头列），如果格式匹配布尔值或数字，提供对应的高亮。
 */
class ParadoxCsvBaseAnnotator : Annotator {
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
        if (element.firstChild == null) return null
        if (element.isHeaderColumn()) return ParadoxCsvAttributesKeys.HEADER_KEY

        val resolvedType = ParadoxTypeResolver.resolve(element.value)
        val attributesKeys = when (resolvedType) {
            ParadoxType.Boolean -> ParadoxCsvAttributesKeys.KEYWORD_KEY
            ParadoxType.Int -> ParadoxCsvAttributesKeys.NUMBER_KEY
            ParadoxType.Float -> ParadoxCsvAttributesKeys.NUMBER_KEY
            // ParadoxType.String -> ParadoxCsvAttributesKeys.STRING_KEY
            else -> null
        }
        return attributesKeys
    }
}

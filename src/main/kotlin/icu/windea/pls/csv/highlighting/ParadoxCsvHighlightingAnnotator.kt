package icu.windea.pls.csv.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver

/**
 * 为 CSV 文件提供额外的语法级别的代码高亮。
 *
 * 说明：
 * 这些代码高亮由注解器（annotator）提供，而非由语法高亮器（syntaxHighlighter）直接在词法级别提供。
 */
class ParadoxCsvHighlightingAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxCsvColumn -> annotateColumn(element, holder)
        }
    }

    private fun annotateColumn(element: ParadoxCsvColumn, holder: AnnotationHolder) {
        // - 对于列（头列），提供特殊高亮
        // - 对于列（非头列），如果格式匹配布尔值或数字，则提供对应的高亮

        val attributesKeys = getAttributesKey(element) ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(attributesKeys).create()
    }

    private fun getAttributesKey(element: ParadoxCsvColumn): TextAttributesKey? {
        if (element.firstChild == null) return null
        if (ParadoxCsvPsiService.isHeaderColumn(element)) return ParadoxCsvHighlighterColors.HEADER

        val resolvedType = ParadoxTypeResolver.resolveExpressionType(element.value)
        val attributesKeys = when (resolvedType) {
            ParadoxExpressionType.Boolean -> ParadoxCsvHighlighterColors.KEYWORD
            ParadoxExpressionType.Int -> ParadoxCsvHighlighterColors.NUMBER
            ParadoxExpressionType.Float -> ParadoxCsvHighlighterColors.NUMBER
            // ParadoxType.String -> ParadoxCsvAttributesKeys.STRING_KEY
            else -> null
        }
        return attributesKeys
    }
}

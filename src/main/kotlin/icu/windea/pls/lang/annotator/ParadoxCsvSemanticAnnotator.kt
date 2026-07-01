package icu.windea.pls.lang.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.annotator.ParadoxCsvSyntaxAnnotator
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * @see ParadoxCsvSyntaxAnnotator
 */
class ParadoxCsvSemanticAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is ParadoxCsvExpressionElement) annotateExpression(element, holder)
    }

    private fun annotateExpression(element: ParadoxCsvExpressionElement, holder: AnnotationHolder) {
        // 不高亮表格头中的列
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return

        // 高亮复杂枚举值声明
        if (annotateComplexEnumValue(element, holder)) return

        val columnConfig = when (element) {
            is ParadoxCsvColumn -> ParadoxCsvManager.getColumnConfig(element)
            else -> null
        }
        val config = columnConfig?.valueConfig ?: return
        ParadoxExpressionManager.annotateCsvExpression(element, null, config, holder)
    }

    private fun annotateComplexEnumValue(element: ParadoxCsvExpressionElement, holder: AnnotationHolder): Boolean {
        if (element.complexEnumValueInfo == null) return false
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE).create()
        return true
    }
}

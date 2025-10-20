package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.getColumnIndex
import icu.windea.pls.csv.psi.isEmptyColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.inspections.PlsInspectionManager
import icu.windea.pls.lang.util.ParadoxCsvFileManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFileMatcher
import javax.swing.JComponent

/**
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isCsvFile(file, smart = true, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val header = file.header
        if (header == null) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvFileManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxCsvColumn) visitColumn(element)
            }

            private fun visitColumn(element: ParadoxCsvColumn) {
                if (element.isHeaderColumn()) return
                val columnConfig = ParadoxCsvFileManager.getColumnConfig(element, rowConfig) ?: return
                if (ParadoxCsvFileManager.isMatchedColumnConfig(element, columnConfig)) return
                val config = columnConfig.valueConfig ?: return

                val locationElement = when {
                    // special handle for empty columns
                    element.isEmptyColumn() -> {
                        val isFirst = element.getColumnIndex() == 0
                        element.siblings(forward = isFirst).find { it.elementType == ParadoxCsvElementTypes.SEPARATOR } ?: return
                    }
                    else -> element
                }

                val description = getMessage(element, columnConfig, config)
                val highlightType = getHighlightType(element, columnConfig, config)
                val fixes = getFixes(element, columnConfig, config)
                holder.registerProblem(locationElement, description, highlightType, *fixes)
            }
        }
    }

    private fun getMessage(element: ParadoxCsvColumn, columnConfig: CwtPropertyConfig, config: CwtValueConfig): String {
        return PlsBundle.message("inspection.csv.unresolvedExpression.desc.1", element.name, columnConfig.key, config.value)
    }

    @Suppress("unused")
    private fun getHighlightType(element: ParadoxCsvColumn, columnConfig: CwtPropertyConfig, config: CwtValueConfig): ProblemHighlightType {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    }

    @Suppress("unused")
    private fun getFixes(element: ParadoxCsvColumn, columnConfig: CwtPropertyConfig, config: CwtValueConfig): Array<LocalQuickFix> {
        val expectedConfigs = listOf(config)
        val result = mutableListOf<LocalQuickFix>()
        result += PlsInspectionManager.getSimilarityBasedFixesForUnresolvedExpression(element, expectedConfigs)
        if (result.isEmpty()) return LocalQuickFix.EMPTY_ARRAY
        return result.toTypedArray()
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
        }
    }
}

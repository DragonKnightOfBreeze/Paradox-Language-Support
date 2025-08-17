package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import javax.swing.*

/**
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (ignoredInInjectedFiles && PlsVfsManager.isInjectedFile(file.virtualFile)) return false
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val header = file.header
        if (header == null) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxCsvColumn) visitColumn(element)
            }

            private fun visitColumn(element: ParadoxCsvColumn) {
                if (element.isHeaderColumn()) return
                val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return
                if (ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return
                val config = columnConfig.valueConfig ?: return

                val description = PlsBundle.message("inspection.csv.unresolvedExpression.desc.1", element.name, columnConfig.key, config.value)
                if (element.isEmptyColumn()) { //special handle for empty columns
                    val isFirst = element.getColumnIndex() == 0
                    val locationElement = element.siblings(forward = isFirst).find { it.elementType == ParadoxCsvElementTypes.SEPARATOR } ?: return
                    holder.registerProblem(locationElement, description)
                    return
                }
                holder.registerProblem(element, description)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
        }
    }
}

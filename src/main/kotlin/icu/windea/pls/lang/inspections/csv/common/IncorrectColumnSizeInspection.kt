package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import javax.swing.*

/**
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class IncorrectColumnSizeInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (ignoredInInjectedFiles && PlsFileManager.isInjectedFile(file.virtualFile)) return false
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

        //如果表头中的列数与期望的不一致，则直接跳过检查

        val expectColumnSize = rowConfig.columnConfigs.size
        val headerColumnSize = header.columnList.size
        if (headerColumnSize != expectColumnSize) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxCsvRow) visitRow(element)
            }

            private fun visitRow(element: ParadoxCsvRow) {
                val columnSize = element.columnList.size
                if (columnSize == expectColumnSize) return

                val description = PlsBundle.message("inspection.csv.incorrectColumnSize.desc.1", expectColumnSize, columnSize, rowConfig.name)
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

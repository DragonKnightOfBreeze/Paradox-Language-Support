package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxCsvManager
import javax.swing.JComponent

/**
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class IncorrectColumnSizeInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的 CSV 文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isCsvFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val header = file.header
        if (header == null) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        val expectColumnSize = rowConfig.columns.size

        // 如果表头中的列数与期望的不一致，则直接跳过检查
        val headerColumnSize = ParadoxCsvManager.computeHeaderColumnSize(header)
        if (headerColumnSize != expectColumnSize) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxCsvVisitor() {
            override fun visitRow(element: ParadoxCsvRow) {
                ProgressManager.checkCanceled()
                val columnSize = ParadoxCsvManager.computeColumnSize(element)
                if (columnSize == expectColumnSize) return
                val location = element.lastChild ?: return // latest non-empty column or separator
                val description = PlsBundle.message("inspection.csv.incorrectColumnSize.desc.1", rowConfig.name, expectColumnSize, columnSize)
                holder.registerProblem(location, description)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}

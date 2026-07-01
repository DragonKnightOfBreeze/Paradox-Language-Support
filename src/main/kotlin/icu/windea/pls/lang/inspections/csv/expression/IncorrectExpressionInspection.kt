package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.csv.psi.isEmptyColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.lang.inspections.ParadoxInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxCsvManager
import javax.swing.JComponent

/**
 * （CSV 文件中的）不正确的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 *
 * @see ParadoxIncorrectExpressionChecker
 */
class IncorrectExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的 CSV 文件
        return ParadoxPsiFileMatchService.isCsvFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val header = file.header
        if (header == null) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        val checkers = ParadoxIncorrectExpressionChecker.EP_NAME.extensionList
        return object : ParadoxCsvVisitor() {
            override fun visitColumn(element: ParadoxCsvColumn) {
                ProgressManager.checkCanceled()
                if (element.isHeaderColumn()) return // skip header columns
                if (element.isEmptyColumn()) return // skip empty columns
                val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return
                if (!ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return
                val config = columnConfig.valueConfig ?: return

                // 开始检查
                ParadoxInspectionService.checkIncorrectExpression(element, config, holder, checkers)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}

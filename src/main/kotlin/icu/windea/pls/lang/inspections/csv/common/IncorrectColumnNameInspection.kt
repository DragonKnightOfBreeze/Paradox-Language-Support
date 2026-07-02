package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxCsvManager
import javax.swing.JComponent

/**
 * （CSV 文件中的）不正确的列名的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class IncorrectColumnNameInspection : LocalInspectionTool() {
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

        val expectColumnSize = rowConfig.columns.size

        return object : ParadoxCsvVisitor() {
            override fun visitHeader(element: ParadoxCsvHeader) {
                ProgressManager.checkCanceled()
                // TODO 2.2.0
                // val columnSize = ParadoxCsvPsiService.getColumnSize(element)
                // if (columnSize == expectColumnSize) return
                // if (rowConfig.skipLastRow && columnSize == expectColumnSize + 1) return
                // val location = element.lastChild ?: return // latest non-empty column or separator
                // val description = ChronicleBundle.message("inspection.csv.incorrectColumnSize.desc.1", rowConfig.name, expectColumnSize, columnSize)
                // holder.registerProblem(location, description)
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

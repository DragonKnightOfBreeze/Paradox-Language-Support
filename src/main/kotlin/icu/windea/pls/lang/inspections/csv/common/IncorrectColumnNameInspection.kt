package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.CwtRowType
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.lang.fixes.ReplaceWithExpressionFix
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
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的 CSV 文件
        return ParadoxPsiFileMatchService.isCsvFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxCsvVisitor() {
            override fun visitHeader(element: ParadoxCsvHeader) {
                ProgressManager.checkCanceled()
                when (rowConfig.type) {
                    CwtRowType.Key -> {
                        val allColumnNames = rowConfig.columns.map { it.key }
                        if (allColumnNames.isEmpty()) return // skip (checked by `IncorrectColumnSizeInspection`)
                        val existingColumnNames = ParadoxCsvPsiService.getColumnNames(element)
                        val expectColumnNames = mutableSetOf<String>().apply { addAll(allColumnNames) }.apply { removeAll(existingColumnNames) }
                        val expect = expectColumnNames.joinToString()
                        for ((columnIndex, columnElement) in element.columnList.withIndex()) {
                            if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) continue // ignored
                            if (columnIndex >= rowConfig.columns.size) {
                                val description = ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.4", rowConfig.name)
                                holder.registerProblem(columnElement, description)
                                return // skip (no future checks)
                            }
                            if (columnElement.name in allColumnNames) continue // continue (matched)
                            if (expect.isNotEmpty()) {
                                val expectColumnNamePreferred = rowConfig.columns[columnIndex].key
                                val description = ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.1", rowConfig.name, expect)
                                if (expectColumnNamePreferred in expectColumnNames) {
                                    val fix = ReplaceWithExpressionFix(expectColumnNamePreferred)
                                    holder.registerProblem(columnElement, description, fix)
                                } else {
                                    holder.registerProblem(columnElement, description)
                                }
                            } else {
                                val description = ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.3", rowConfig.name, expect)
                                holder.registerProblem(columnElement, description)
                            }
                        }
                    }
                    CwtRowType.Index -> {
                        for ((columnIndex, columnElement) in element.columnList.withIndex()) {
                            if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) continue // ignored
                            if (columnIndex >= rowConfig.columns.size) {
                                val description = ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.4", rowConfig.name)
                                holder.registerProblem(columnElement, description)
                                return // skip (no future checks)
                            }
                            val expectColumnName = rowConfig.columns[columnIndex].key
                            if (expectColumnName == columnElement.name) continue // continue (matched)
                            val description = ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.2", rowConfig.name, expectColumnName)
                            val fix = ReplaceWithExpressionFix(expectColumnName)
                            holder.registerProblem(columnElement, description, fix)
                        }
                    }
                }
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

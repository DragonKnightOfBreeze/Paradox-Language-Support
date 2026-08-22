package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.CwtRowType
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.core.collections.forEachIndexedFast
import icu.windea.pls.core.truncate
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.lang.fixes.ReplaceWithExpressionFix
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager

/**
 * （CSV 文件中的）不正确的列名的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class IncorrectColumnNameInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var showExpect = true
    @JvmField var truncateExpect = -1

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("showExpect", ChronicleBundle.message("inspection.option.showExpect")),
            OptPane.number("truncateExpect", ChronicleBundle.message("inspection.option.truncateExpect"), Int.MIN_VALUE, Int.MAX_VALUE),
        )
    }

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
        val rowConfig = ParadoxConfigManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR
        return object : ParadoxCsvVisitor() {
            override fun visitHeader(element: ParadoxCsvHeader) {
                ProgressManager.checkCanceled()
                check(element, rowConfig, holder)
            }
        }
    }

    private fun check(element: ParadoxCsvHeader, rowConfig: CwtRowConfig, holder: ProblemsHolder) {
        when (rowConfig.type) {
            CwtRowType.Key -> {
                val allColumnNames = rowConfig.columns.map { it.key }
                if (allColumnNames.isEmpty()) return // skip (checked by `IncorrectColumnSizeInspection`)
                val existingColumnNames = ParadoxCsvPsiService.getColumnNames(element)
                val expectColumnNames = mutableSetOf<String>().apply { addAll(allColumnNames) }.apply { removeAll(existingColumnNames) }
                val expectText = expectColumnNames.truncate(truncateExpect).joinToString()
                element.columnList.forEachIndexedFast f@{ columnIndex, columnElement ->
                    if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) return@f // ignored
                    if (columnIndex >= rowConfig.columns.size) {
                        val description = when {
                            showExpect -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.4", rowConfig.name)
                            else -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                        return // skip (no future checks)
                    }
                    if (columnElement.name in allColumnNames) return@f // continue (matched)
                    if (expectColumnNames.isNotEmpty()) {
                        val description = when {
                            showExpect -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.1", rowConfig.name, expectText)
                            else -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.0")
                        }
                        val expectColumnNamePreferred = rowConfig.columns[columnIndex].key
                        if (expectColumnNamePreferred in expectColumnNames) {
                            val fix = ReplaceWithExpressionFix(expectColumnNamePreferred)
                            holder.registerProblem(columnElement, description, fix)
                        } else {
                            holder.registerProblem(columnElement, description)
                        }
                    } else {
                        val description = when {
                            showExpect -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.3", rowConfig.name, expectText)
                            else -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                    }
                }
            }
            CwtRowType.Index -> {
                element.columnList.forEachIndexedFast f@{ columnIndex, columnElement ->
                    if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) return@f // ignored
                    if (columnIndex >= rowConfig.columns.size) {
                        val description = when {
                            showExpect -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.4", rowConfig.name)
                            else -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                        return // skip (no future checks)
                    }
                    val expectColumnName = rowConfig.columns[columnIndex].key
                    if (expectColumnName == columnElement.name) return@f // continue (matched)
                    val description = when {
                        showExpect -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.2", rowConfig.name, expectColumnName)
                        else -> ChronicleBundle.message("inspection.csv.incorrectColumnName.desc.0")
                    }
                    val fix = ReplaceWithExpressionFix(expectColumnName)
                    holder.registerProblem(columnElement, description, fix)
                }
            }
        }
    }
}

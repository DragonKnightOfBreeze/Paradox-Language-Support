package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxCsvFileManager
import icu.windea.pls.lang.util.PlsVfsManager
import javax.swing.JComponent

/**
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedColumnsInspection : LocalInspectionTool() {
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
        val rowConfig = ParadoxCsvFileManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is ParadoxCsvFile) return
                val rowConfig = ParadoxCsvFileManager.getRowConfig(file) ?: return
                val header = file.header ?: return
                val headerColumns = header.columnList
                val unresolvedKeys = headerColumns.map { it.name }.toMutableSet()
                unresolvedKeys -= rowConfig.columns.keys
                rowConfig.endColumn?.let { endColumn -> unresolvedKeys -= endColumn }
                if (unresolvedKeys.isEmpty()) return

                val hasEmpty = unresolvedKeys.remove("")
                if (unresolvedKeys.isNotEmpty()) {
                    val description = PlsBundle.message("inspection.csv.unresolvedColumns.desc.1", unresolvedKeys.joinToString(", "), rowConfig.name)
                    holder.registerProblem(file, description)
                }
                if (hasEmpty) {
                    val description = PlsBundle.message("inspection.csv.unresolvedColumns.desc.2", rowConfig.name)
                    holder.registerProblem(file, description)
                }
            }
        }
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

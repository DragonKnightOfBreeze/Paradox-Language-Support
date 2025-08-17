package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.*
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
class MissingColumnsInspection : LocalInspectionTool() {
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
            override fun visitFile(file: PsiFile) {
                if (file !is ParadoxCsvFile) return
                val rowConfig = ParadoxCsvManager.getRowConfig(file) ?: return
                val header = file.header ?: return
                val headerColumns = header.columnList
                val missingKeys = rowConfig.columns.keys.toMutableSet()
                missingKeys -= headerColumns.map { it.name }.toSet()
                if (missingKeys.isEmpty()) return

                val description = PlsBundle.message("inspection.csv.missingColumns.desc.1", missingKeys.joinToString(", "), rowConfig.name)
                holder.registerProblem(file, description)
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

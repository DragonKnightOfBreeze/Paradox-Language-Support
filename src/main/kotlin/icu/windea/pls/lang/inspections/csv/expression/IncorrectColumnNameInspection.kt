package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

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
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("option.ignoredInInjectedFiles")),
            OptPane.checkbox("showExpect", ChronicleInspectionBundle.message("option.showExpect")),
            OptPane.number("truncateExpect", ChronicleInspectionBundle.message("option.truncateExpect"), Int.MIN_VALUE, Int.MAX_VALUE),
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
        val context = createContext(holder)
        if (context.rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR
        return object : ParadoxCsvVisitor() {
            override fun visitHeader(element: ParadoxCsvHeader) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForIncorrectColumnName(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, showExpect = showExpect, truncateExpect = truncateExpect, )
    }
}

package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxCsvManager

/**
 * （CSV 文件中的）不正确的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 *
 * @see ParadoxIncorrectExpressionChecker
 */
class IncorrectExpressionInspection : LocalInspectionTool(), ParadoxExpressionInspectionContext.Aware {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("showExpect", ChronicleBundle.message("inspection.option.showExpect")),
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
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR
        val context = createContext(holder)
        return object : ParadoxPsiElementVisitor() {
            override fun visitExpressionElement(element: ParadoxCsvExpressionElement) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForIncorrectExpression(element, rowConfig, context)
            }
        }
    }

    override fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, false, showExpect)
    }
}

package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

/**
 * 检查是否存在无法解析的表达式。
 *
 * 如果当前节点存在对应的行规则和列规则，但列规则不匹配，则认为未通过检查。
 *
 * 当涉及部分特殊情况时，此代码检查会被直接跳过。
 * 例如：因为存在匹配的扩展规则而被忽略。
 *
 * @property ignoreInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoreByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoreInInjectedFiles = false
    @JvmField var ignoreByConfigs = false
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoreInInjectedFiles", ChronicleInspectionBundle.message("option.ignoreInInjectedFiles")),
            OptPane.checkbox("ignoreByConfigs", ChronicleInspectionBundle.message("option.ignoreByConfigs")),
            OptPane.checkbox("showExpect", ChronicleInspectionBundle.message("option.showExpect")),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoreInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
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
        return object : ParadoxPsiElementVisitor() {
            override fun visitExpressionElement(element: ParadoxCsvExpressionElement) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForUnresolvedExpression(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, ignoreByConfigs = ignoreByConfigs, showExpect = showExpect)
    }
}

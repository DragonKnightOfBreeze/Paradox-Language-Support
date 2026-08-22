package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * （脚本文件中的）无法解析的表达式的代码检查。
 *
 * 如果当前节点存在对应的规则上下文，但不存在匹配的规则，则认为未通过检查。
 *
 * 当涉及部分特殊情况时，此代码检查会被直接跳过。
 * 例如：其规则上下文指定跳过检查。因为存在匹配的扩展规则而被忽略。
 *
 * 如果当前节点未通过检查，而父节点也未通过检查，此代码检查会被跳过，避免冗余的报错。
 * 例如：如果属性键无法解析，不会继续检查属性值。如果块无法解析，不会继续检查其中的成员。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 * @property ignoredByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var ignoredByConfigs = false
    @JvmField var showExpect = true
    @JvmField var truncateExpect = -1

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleInspectionBundle.message("inspection.option.ignoredInInlineScriptFiles")),
            OptPane.checkbox("ignoredByConfigs", ChronicleInspectionBundle.message("inspection.option.ignoredByConfigs")),
            OptPane.checkbox("showExpect", ChronicleInspectionBundle.message("inspection.option.showExpect")),
            OptPane.number("truncateExpect", ChronicleInspectionBundle.message("inspection.option.truncateExpect"), Int.MIN_VALUE, Int.MAX_VALUE),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 按需忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.isInlineScriptFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        val context = createContext(holder)
        return object : ParadoxPsiElementVisitor() {
            override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForUnresolvedExpression(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, ignoredByConfigs = ignoredByConfigs, showExpect = showExpect, truncateExpect = truncateExpect, )
    }
}

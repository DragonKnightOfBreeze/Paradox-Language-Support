package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 缺失的表达式的代码检查。
 *
 * @property firstOnly （配置项）是否仅标出第一个错误。
 * @property firstOnlyOnFile （配置项）在文件级别上，是否仅标出第一个错误。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class MissingExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var showExpect = true
    @JvmField var firstOnly = false
    @JvmField var firstOnlyOnFile = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleInspectionBundle.message("option.ignoredInInlineScriptFiles")),
            OptPane.checkbox("showExpect", ChronicleInspectionBundle.message("option.showExpect")),
            OptPane.checkbox("firstOnly", ChronicleInspectionBundle.message("lang.missingExpression.option.firstOnly")),
            OptPane.checkbox("firstOnlyOnFile", ChronicleInspectionBundle.message("lang.missingExpression.option.firstOnlyOnFile")),
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

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : ParadoxScriptVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is ParadoxScriptFile) return
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForMissingExpression(file, context)
            }

            override fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForMissingExpression(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, showExpect = showExpect, firstOnly = firstOnly, firstOnlyOnFile = firstOnlyOnFile)
    }
}

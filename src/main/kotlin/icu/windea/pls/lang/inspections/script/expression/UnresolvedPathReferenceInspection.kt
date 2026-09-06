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
import icu.windea.pls.lang.inspections.forPatterns
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 检查是否存在无法解析的路径引用。
 *
 * @property ignoredFileNames （配置项）需要忽略解析的文件名。一组模式，分号分隔，忽略大小写。
 * @property ignoreInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoreInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedPathReferenceInspection : LocalInspectionTool() {
    @JvmField var ignoreInInjectedFiles = false
    @JvmField var ignoreInInlineScriptFiles = false
    @JvmField var ignoreByConfigs = false
    @JvmField var ignoredFileNames = "*.lua;*.tga"
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoreInInjectedFiles", ChronicleInspectionBundle.message("option.ignoreInInjectedFiles")),
            OptPane.checkbox("ignoreInInlineScriptFiles", ChronicleInspectionBundle.message("option.ignoreInInlineScriptFiles")),
            OptPane.checkbox("ignoreByConfigs", ChronicleInspectionBundle.message("option.ignoreByConfigs")),
            OptPane.expandableString("ignoredFileNames", ChronicleInspectionBundle.message("option.ignoredFileNames"), ";").forPatterns(),
            OptPane.checkbox("showExpect", ChronicleInspectionBundle.message("option.showExpect")),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoreInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 按需忽略内联脚本文件
        if (ignoreInInlineScriptFiles && ParadoxInlineScriptManager.isInlineScriptFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                ParadoxExpressionInspectionService.checkForUnresolvedPathReference(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        return ParadoxExpressionInspectionContext(this, holder, ignoreByConfigs = ignoreByConfigs, ignoredFileNames = ignoredFileNames, showExpect = showExpect)
    }
}

package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * （对于脚本文件）检查是否存在不正确的语法。
 *
 * 包括：
 * - 不期望的比较运算符。
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkComparisonOperator(holder, element, context)
                checkSafeAssignOperator(holder, element, context)
                checkSafeCallAssignOperator(holder, element, context)
            }
        }
    }

    private fun checkComparisonOperator(holder: ProblemsHolder, element: PsiElement, context: Context) {
        if (context.rootFile == null) return

        if (element !is ParadoxScriptProperty) return
        val token = element.findChild { it.elementType in ParadoxScriptTokenSets.COMPARISON_TOKENS } ?: return

        // 所在属性的键与值应可以表示一个数值
        val numberRepresentable = ParadoxMemberService.isNumberRepresentable(element)
        if (numberRepresentable == false) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.1")
            holder.registerProblem(token, description)
            return
        }

        // NOTE 2.1.4 所在属性对应的匹配的规则，不一定在触发器子句规则之内

        // 所在属性对应的匹配的规则，其使用的属性分隔符应是 `==`（而非常规的 `=`）
        val allowed = ParadoxMemberService.isComparisonOperatorAllowed(element)
        if (allowed == false) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.2")
            holder.registerProblem(token, description)
            return
        }
    }

    private fun checkSafeAssignOperator(holder: ProblemsHolder, element: PsiElement, context: Context) {
        if (context.rootFile == null) return

        if (element.elementType != ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN) return

        val supported = ParadoxSyntaxConstraint.SafeAssignOperator.testStrict(context.gameType, context.gameVersion)
        if (!supported) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.11", context.gameType.title)
            holder.registerProblem(element, description)
            return
        }
    }

    private fun checkSafeCallAssignOperator(holder: ProblemsHolder, element: PsiElement, context: Context) {
        if (context.rootFile == null) return

        if (element.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) return

        val supported = ParadoxSyntaxConstraint.SafeCallAssignOperator.testStrict(context.gameType, context.gameVersion)
        if (!supported) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.12", context.gameType.title)
            holder.registerProblem(element, description)
            return
        }
    }

    private fun createContext(holder: ProblemsHolder): Context {
        val file = selectFile(holder.file)
        val rootFile = selectRootFile(holder.file)
        val rootInfo = file?.fileInfo?.rootInfo
        val gameType = rootInfo?.gameType
        val gameVersion = rootInfo?.gameVersion
        return Context(file, rootFile, gameType, gameVersion)
    }

    private data class Context(
        val file: VirtualFile?,
        val rootFile: VirtualFile?,
        val gameType: ParadoxGameType?,
        val gameVersion: String?,
    )
}

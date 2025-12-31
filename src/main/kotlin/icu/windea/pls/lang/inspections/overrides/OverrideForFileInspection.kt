package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingFilesFix
import icu.windea.pls.lang.util.ParadoxFileManager

/**
 * 对文件的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同路径（相对于入口目录）的文件，那么就说存在对此文件的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的文件。
 */
class OverrideForFileInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR
        if (!ParadoxFileManager.canOverrideFile(file, fileInfo.group)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val overrideResult = ParadoxOverrideService.getOverrideResultForFile(file)
                if (overrideResult == null) return

                val locationElement = file
                val (key, target, results) = overrideResult
                val message = PlsBundle.message("inspection.overrideForFile.desc", key)
                val fix = NavigateToOverridingFilesFix(key, target, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}

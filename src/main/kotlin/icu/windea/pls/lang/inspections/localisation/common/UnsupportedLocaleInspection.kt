package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * （本地化文件中的）不支持的语言环境的代码检查。
 */
class UnsupportedLocaleInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitLocale(element: ParadoxLocalisationLocale) {
                ProgressManager.checkCanceled()
                val locale = selectLocale(element)
                if (locale == null) {
                    val location = element.idElement
                    val description = ChronicleBundle.message("inspection.localisation.unsupportedLocale.desc.1", element.name)
                    holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                } else if(!locale.supports) {
                    val gameType = locale.configGroup.gameType
                    val location = element.idElement
                    val description = ChronicleBundle.message("inspection.localisation.unsupportedLocale.desc.2", element.name, gameType.title)
                    holder.registerProblem(location, description)
                }
            }
        }
    }
}

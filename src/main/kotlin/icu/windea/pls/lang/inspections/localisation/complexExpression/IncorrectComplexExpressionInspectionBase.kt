package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

/**
 * 不正确的复杂表达式（[ParadoxComplexExpression]）的代码检查的基类。
 */
abstract class IncorrectComplexExpressionInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxPsiElementVisitor() {
            override fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                super.visitExpressionElement(element)
                check(element, configGroup, holder)
            }
        }
    }

    private fun check(element: ParadoxLocalisationExpressionElement, configGroup: CwtConfigGroup, holder: ProblemsHolder) {
        val complexExpression = resolveComplexExpression(element, configGroup) ?: return
        val errors = complexExpression.getAllErrors(element)
        if (errors.isEmpty()) return
        val fixes = getFixes(element, complexExpression, errors)
        errors.forEachFast { error -> error.register(element, holder, *fixes) }
    }

    protected open fun resolveComplexExpression(element: ParadoxLocalisationExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        if ((!isAvailable(element))) return null
        return ParadoxComplexExpression.resolve(element, configGroup)
    }

    protected open fun isAvailable(element: ParadoxLocalisationExpressionElement): Boolean {
        return true
    }

    protected open fun getFixes(element: ParadoxLocalisationExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        return LocalQuickFix.EMPTY_ARRAY
    }
}

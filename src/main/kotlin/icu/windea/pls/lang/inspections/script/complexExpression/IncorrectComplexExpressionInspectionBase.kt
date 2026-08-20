package icu.windea.pls.lang.inspections.script.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.lang.fixes.QuoteLiteralFix
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * 不正确的复杂表达式（[ParadoxComplexExpression]）的代码检查的基类。
 */
abstract class IncorrectComplexExpressionInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                super.visitStringExpressionElement(element)
                check(element, configGroup, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return
        val complexExpression = resolveComplexExpression(element, configGroup) ?: return
        val errors = complexExpression.getAllErrors(element)
        if (errors.isEmpty()) return
        val fixes = getFixes(element, complexExpression, errors)
        errors.forEach { error -> error.register(element, holder, *fixes) }
    }

    protected open fun resolveComplexExpression(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        if ((!isAvailable(element))) return null
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (!isAvailableForConfig(config)) return null
        val expressionText = ParadoxExpressionService.getExpressionText(element)
        return ParadoxComplexExpression.resolveByConfig(expressionText, configGroup, config)
    }

    protected open fun isAvailable(element: ParadoxScriptStringExpressionElement): Boolean {
        return true
    }

    protected open fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        return true
    }

    protected open fun getFixes(element: ParadoxScriptStringExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        errors.forEachFast { error ->
            if (error.code == ParadoxComplexExpressionErrors.EXPRESSION_NOT_QUOTED) result += QuoteLiteralFix()
        }
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}

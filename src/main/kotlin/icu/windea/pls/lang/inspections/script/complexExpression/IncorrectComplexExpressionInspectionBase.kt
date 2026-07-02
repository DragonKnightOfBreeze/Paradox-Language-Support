package icu.windea.pls.lang.inspections.script.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.fixes.QuoteLiteralFix
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * 不正确的复杂表达式（[ParadoxComplexExpression]）的代码检查的基类。
 */
abstract class IncorrectComplexExpressionInspectionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return
                val complexExpression = resolveComplexExpression(element, configGroup) ?: return
                val errors = complexExpression.getAllErrors(element)
                if (errors.isEmpty()) return
                val fixes = getFixes(element, complexExpression, errors)
                errors.forEach { error -> error.register(element, holder, *fixes) }
            }
        }
    }

    protected open fun resolveComplexExpression(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        if (!isAvailableForConfig(config)) return null
        val expressionText = ParadoxExpressionManager.getExpressionText(element)
        return ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config)
    }

    protected abstract fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean

    protected open fun getFixes(element: ParadoxScriptStringExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        for (error in errors) {
            if (error.code == ParadoxComplexExpressionErrors.EXPRESSION_NOT_QUOTED) result += QuoteLiteralFix()
        }
        if (result.isEmpty()) return LocalQuickFix.EMPTY_ARRAY
        return result.toTypedArray()
    }
}

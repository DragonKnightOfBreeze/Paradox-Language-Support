package icu.windea.pls.lang.inspections.script.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFileMatcher
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 不正确的复杂表达式的检查的基类。
 */
abstract class IncorrectComplexExpressionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val complexExpression = resolveComplexExpression(element, configGroup) ?: return
                val errors = complexExpression.getAllErrors(element)
                if (errors.isEmpty()) return
                val fixes = getFixes(element, complexExpression)
                errors.forEach { error -> error.register(element, holder, *fixes) }
            }
        }
    }

    protected open fun resolveComplexExpression(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        if (!isAvailableForConfig(config)) return null
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        return ParadoxComplexExpression.resolveByConfig(value, textRange, configGroup, config)
    }

    protected abstract fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean

    protected open fun getFixes(element: ParadoxScriptStringExpressionElement, complexExpression: ParadoxComplexExpression): Array<out LocalQuickFix> {
        return LocalQuickFix.EMPTY_ARRAY
    }
}

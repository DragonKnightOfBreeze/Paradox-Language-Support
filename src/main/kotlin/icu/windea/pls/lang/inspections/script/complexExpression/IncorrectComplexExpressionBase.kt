package icu.windea.pls.lang.inspections.script.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * 不正确的复杂表达式的代码检查的基类。
 */
abstract class IncorrectComplexExpressionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isExpression()) return
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
        val value = element.value
        return ParadoxComplexExpression.resolveByConfig(value, null, configGroup, config)
    }

    protected abstract fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean

    protected open fun getFixes(element: ParadoxScriptStringExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        return LocalQuickFix.EMPTY_ARRAY
    }
}

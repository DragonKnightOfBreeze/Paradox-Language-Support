package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于在重命名封装变量时，自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticScriptedVariableRelatedLocalisationsRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxScriptScriptedVariable) return false
        val name = element.name?.orNull() ?: return false
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxScriptedVariableManager.getNameLocalisations(name, element, locale).isNotEmpty()
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.scriptedVariable.relatedLocalisations")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForScriptedVariables
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForScriptedVariables = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticScriptedVariableRelatedLocalisationsRenamer(element, newName)
    }
}

package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于在重命名定义时，自动重命名由其生成的修正的作为（显示）名称和描述的本地化（如果存在）。
 */
class AutomaticDefinitionGeneratedModifiersNameDescRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.modifiers.isNotEmpty()
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.definition.generatedModifiersNameDesc")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameGeneratedModifierNameDescForDefinitions
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameGeneratedModifierNameDescForDefinitions = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticDefinitionGeneratedModifiersNameDescRenamer(element, newName)
    }
}

package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings

/**
 * 用于在重命名定义时，自动重命名相关本地化（如果存在且需要）。
 */
class ParadoxDefinitionRelatedLocalisationsAutomaticRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.localisations.isNotEmpty()
    }

    override fun getOptionName(): String {
        return ChronicleBundle.message("rename.definition.relatedLocalisations")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForDefinitions
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForDefinitions = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return ParadoxDefinitionRelatedLocalisationsAutomaticRenamer(element, newName)
    }
}

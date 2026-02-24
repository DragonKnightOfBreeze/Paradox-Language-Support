package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 用于在重命名复杂枚举值时，自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticComplexEnumValueRelatedLocalisationsRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxComplexEnumValueElement) return false
        val name = element.name.orNull() ?: return false
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxComplexEnumValueManager.getNameLocalisations(name, element, locale).isNotEmpty()
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.complexEnumValue.relatedLocalisations")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForComplexEnumValues
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForComplexEnumValues = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticComplexEnumValueRelatedLocalisationsRenamer(element, newName)
    }
}

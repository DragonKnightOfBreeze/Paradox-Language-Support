package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings

/**
 * 用于在重命名本地化引用片段时，自动重命名关联的本地化。
 *
 * @see ParadoxLocalisationSnippetLightElement
 */
class ParadoxLocalisationSnippetAutomaticRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        return element is ParadoxLocalisationSnippetLightElement
    }

    override fun getOptionName(): String {
        return ChronicleBundle.message("rename.localisationSnippet.relatedLocalisations")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForLocalisationSnippets
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForLocalisationSnippets = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return ParadoxLocalisationSnippetAutomaticRenamer(element, newName)
    }
}

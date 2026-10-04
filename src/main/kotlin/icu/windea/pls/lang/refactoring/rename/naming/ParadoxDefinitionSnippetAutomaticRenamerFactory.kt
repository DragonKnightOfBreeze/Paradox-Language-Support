package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings

/**
 * 用于在重命名定义引用片段时，自动重命名关联的定义。
 *
 * @see ParadoxDefinitionSnippetLightElement
 */
class ParadoxDefinitionSnippetAutomaticRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        return element is ParadoxDefinitionSnippetLightElement
    }

    override fun getOptionName(): String {
        return ChronicleBundle.message("rename.definitionSnippet.relatedDefinitions")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedDefinitionsForDefinitionSnippets
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedDefinitionsForDefinitionSnippets = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return ParadoxDefinitionSnippetAutomaticRenamer(element, newName)
    }
}

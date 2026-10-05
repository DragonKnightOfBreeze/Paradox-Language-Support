package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive

/**
 * 用于在重命名定义引用片段时，自动重命名关联的定义。
 *
 * @see ParadoxDefinitionSnippetLightElement
 */
class ParadoxDefinitionSnippetAutomaticRenamer(element: PsiElement, newName: String) : ParadoxAutomaticRenamer() {
    class Factory : ParadoxAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            return element is ParadoxDefinitionSnippetLightElement
        }

        override fun getOptionName() = ChronicleBundle.message("rename.definitionSnippet.relatedDefinitions")

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

    init {
        val allRenames = mutableMapOf<PsiNamedElement, String>()
        prepareRenaming(element, newName, allRenames)
        for ((key, value) in allRenames) {
            ProgressManager.checkCanceled()
            myElements += key
            suggestAllNames(key.name, value)
        }
    }

    override fun isSelectedByDefault() = true

    override fun allowChangeSuggestedName() = false

    override fun getDialogTitle() = ChronicleBundle.message("rename.definitionSnippet.relatedDefinitions.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.definitionSnippet.relatedDefinitions.desc")

    override fun entityName() = ChronicleBundle.message("rename.definitionSnippet.relatedDefinitions.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDefinitionSnippetLightElement) return
        val name = element.name
        val type = element.definitionType
        if (name.isEmpty()) return
        val project = element.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        for (template in element.snippetTemplates) {
            ProgressManager.checkCanceled()
            val fullName = template.resolve(name)
            val newFullName = template.resolve(newName)
            val targets = ParadoxDefinitionSearch.searchElement(fullName, type, selector).findAll()
            for (target in targets) {
                ProgressManager.checkCanceled()
                allRenames[target] = newFullName
            }
        }
    }
}

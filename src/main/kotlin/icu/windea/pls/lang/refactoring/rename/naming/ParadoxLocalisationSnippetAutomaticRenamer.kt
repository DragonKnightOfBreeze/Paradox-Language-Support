package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 用于在重命名本地化引用片段时，自动重命名关联的本地化。
 *
 * @see ParadoxLocalisationSnippetLightElement
 */
class ParadoxLocalisationSnippetAutomaticRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.localisationSnippet.relatedLocalisations.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.localisationSnippet.relatedLocalisations.desc")

    override fun entityName() = ChronicleBundle.message("rename.localisationSnippet.relatedLocalisations.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxLocalisationSnippetLightElement) return
        val name = element.name
        if (name.isEmpty()) return
        val project = element.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        for (template in element.snippetTemplates) {
            ProgressManager.checkCanceled()
            val fullName = template.resolve(name)
            val newFullName = template.resolve(newName)
            val targets = ParadoxLocalisationSearch.searchNormal(fullName, selector).findAll()
            for (target in targets) {
                ProgressManager.checkCanceled()
                allRenames[target] = newFullName
            }
        }
    }
}

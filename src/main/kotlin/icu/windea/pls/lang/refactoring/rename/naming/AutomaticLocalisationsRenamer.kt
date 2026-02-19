package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class AutomaticLocalisationsRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
    init {
        element as ParadoxLocalisationProperty
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for ((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }

    override fun isSelectedByDefault() = true

    override fun allowChangeSuggestedName() = false

    override fun getDialogTitle() = PlsBundle.message("rename.localisation.overrides.title")

    override fun getDialogDescription() = PlsBundle.message("rename.localisation.overrides.desc")

    override fun entityName() = PlsBundle.message("rename.localisation.overrides.entityName")

    private fun prepareRenaming(element: ParadoxLocalisationProperty, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val name = element.name.orNull() ?: return
        val type = element.type ?: return

        ProgressManager.checkCanceled()
        val selector = selector(element.project, element).localisation().contextSensitive()
        val targets = ParadoxLocalisationSearch.search(name, type, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

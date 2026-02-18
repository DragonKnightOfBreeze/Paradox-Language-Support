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
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class AutomaticScriptedVariableRelatedLocalisationsRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
    init {
        element as ParadoxScriptScriptedVariable
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for ((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }

    override fun isSelectedByDefault() = true

    override fun allowChangeSuggestedName() = false

    override fun getDialogTitle() = PlsBundle.message("rename.scriptedVariable.relatedLocalisations.title")

    override fun getDialogDescription() = PlsBundle.message("rename.scriptedVariable.relatedLocalisations.desc")

    override fun entityName() = PlsBundle.message("rename.scriptedVariable.relatedLocalisations.entityName")

    private fun prepareRenaming(element: ParadoxScriptScriptedVariable, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val name = element.name?.orNull() ?: return

        ProgressManager.checkCanceled()
        val selector = selector(element.project, element).localisation().contextSensitive()
        val targets = ParadoxLocalisationSearch.search(name, ParadoxLocalisationType.Normal, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            allRenames[target] = newName
        }
    }
}

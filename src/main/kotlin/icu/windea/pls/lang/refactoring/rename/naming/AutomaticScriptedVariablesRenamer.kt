package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class AutomaticScriptedVariablesRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.scriptedVariable.overrides.title")

    override fun getDialogDescription() = PlsBundle.message("rename.scriptedVariable.overrides.desc")

    override fun entityName() = PlsBundle.message("rename.scriptedVariable.overrides.entityName")

    private fun prepareRenaming(element: ParadoxScriptScriptedVariable, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val name = element.name?.orNull() ?: return

        ProgressManager.checkCanceled()
        val selector = selector(element.project, element).scriptedVariable().contextSensitive()

        val targets = mutableSetOf<ParadoxScriptScriptedVariable>()
        ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { targets.addAll(it) }
        ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { targets.addAll(it) }

        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

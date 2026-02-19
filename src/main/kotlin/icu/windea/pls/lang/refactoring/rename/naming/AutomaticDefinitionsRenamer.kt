package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptProperty

class AutomaticDefinitionsRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
    init {
        element as ParadoxScriptProperty
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for ((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }

    override fun isSelectedByDefault() = true

    override fun allowChangeSuggestedName() = false

    override fun getDialogTitle() = PlsBundle.message("rename.definition.overrides.title")

    override fun getDialogDescription() = PlsBundle.message("rename.definition.overrides.desc")

    override fun entityName() = PlsBundle.message("rename.definition.overrides.entityName")

    private fun prepareRenaming(element: ParadoxScriptProperty, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val name = definitionInfo.name
        val type = definitionInfo.type
        if (name.isEmpty()) return

        ProgressManager.checkCanceled()
        val selector = selector(element.project, element).definition().contextSensitive()
        val targets = ParadoxDefinitionSearch.searchProperty(name, type, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

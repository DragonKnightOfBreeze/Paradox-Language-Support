package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector

/**
 * 用于在重命名动态值时，自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticDynamicValueRelatedLocalisationsRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.dynamicValue.relatedLocalisations.title")

    override fun getDialogDescription() = PlsBundle.message("rename.dynamicValue.relatedLocalisations.desc")

    override fun entityName() = PlsBundle.message("rename.dynamicValue.relatedLocalisations.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDynamicValueLightElement) return
        val name = element.name.orNull() ?: return
        ProgressManager.checkCanceled()
        val selector = selector(element.project, element).localisation().contextSensitive()
        val targets = ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            allRenames[target] = newName
        }
    }
}

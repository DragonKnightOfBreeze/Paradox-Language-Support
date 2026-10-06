package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于在重命名封装变量时，自动重命名相关本地化（如果存在且需要）。
 */
class ParadoxScriptedVariableRelatedLocalisationsAutomaticRenamer(element: PsiElement, newName: String) : ParadoxScriptedVariableAutomaticRenamer() {
    class Factory : ParadoxScriptedVariableAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxScriptScriptedVariable) return false
            val relatedLocalisations = ParadoxScriptedVariableManager.getRelatedLocalisations(element)
            return relatedLocalisations.isNotEmpty()
        }

        override fun getOptionName() = ChronicleBundle.message("rename.scriptedVariable.relatedLocalisations")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForScriptedVariables
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForScriptedVariables = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxScriptedVariableRelatedLocalisationsAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.scriptedVariable.relatedLocalisations.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.scriptedVariable.relatedLocalisations.desc")

    override fun entityName() = ChronicleBundle.message("rename.scriptedVariable.relatedLocalisations.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxScriptScriptedVariable) return
        val name = element.name?.orNull() ?: return
        ProgressManager.checkCanceled()
        val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
        val targets = ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

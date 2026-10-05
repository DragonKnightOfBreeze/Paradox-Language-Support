package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 用于在重命名动态值时，自动重命名相关本地化（如果存在且需要）。
 *
 * @see ParadoxDynamicValueLightElement
 */
class ParadoxDynamicValueRelatedLocalisationsAutomaticRenamer(element: PsiElement, newName: String) : ParadoxDynamicValueAutomaticRenamer() {
    class Factory : ParadoxDynamicValueAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxDynamicValueLightElement) return false
            val name = element.name.orNull() ?: return false
            val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
            return ParadoxDynamicValueManager.getNameLocalisations(name, element, locale).isNotEmpty()
        }

        override fun getOptionName() = ChronicleBundle.message("rename.dynamicValue.relatedLocalisations")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForDynamicValues
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForDynamicValues = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxDynamicValueRelatedLocalisationsAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.dynamicValue.relatedLocalisations.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.dynamicValue.relatedLocalisations.desc")

    override fun entityName() = ChronicleBundle.message("rename.dynamicValue.relatedLocalisations.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDynamicValueLightElement) return
        val name = element.name.orNull() ?: return
        ProgressManager.checkCanceled()
        val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
        val targets = ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            allRenames[target] = newName
        }
    }
}

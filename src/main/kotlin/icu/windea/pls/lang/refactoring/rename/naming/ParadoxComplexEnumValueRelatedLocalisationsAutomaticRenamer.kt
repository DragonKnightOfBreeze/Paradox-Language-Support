package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 用于在重命名复杂枚举值时，自动重命名相关本地化（如果存在且需要）。
 *
 * @see ParadoxComplexEnumValueLightElement
 */
class ParadoxComplexEnumValueRelatedLocalisationsAutomaticRenamer(element: PsiElement, newName: String) : ParadoxComplexEnumValueAutomaticRenamer() {
    class Factory : ParadoxComplexEnumValueAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxComplexEnumValueLightElement) return false
            val name = element.name.orNull() ?: return false
            val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
            return ParadoxComplexEnumValueManager.getNameLocalisations(name, element, locale).isNotEmpty()
        }

        override fun getOptionName() = ChronicleBundle.message("rename.complexEnumValue.relatedLocalisations")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForComplexEnumValues
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameRelatedLocalisationsForComplexEnumValues = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxComplexEnumValueRelatedLocalisationsAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.complexEnumValue.relatedLocalisations.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.complexEnumValue.relatedLocalisations.desc")

    override fun entityName() = ChronicleBundle.message("rename.complexEnumValue.relatedLocalisations.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxComplexEnumValueLightElement) return
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

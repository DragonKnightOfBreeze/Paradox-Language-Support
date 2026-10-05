package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.core.process
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 用于在重命名本地化时，自动重命名重载项（如果存在）。
 */
class ParadoxLocalisationsAutomaticRenamer(element: PsiElement, newName: String) : ParadoxLocalisationAutomaticRenamer() {
    class Factory : ParadoxLocalisationAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxLocalisationProperty) return false
            val name = element.name.orNull() ?: return false
            val type = element.type ?: return false
            val selector = ParadoxLocalisationSearch.selector(element.project, element)
            val processor = ProcessorFactory.duplicate<ParadoxLocalisationProperty>()
            ParadoxLocalisationSearch.search(name, selector, type).process(processor)
            return processor.result
        }

        override fun getOptionName() = ChronicleBundle.message("rename.localisation.overrides")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameLocalisations
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameLocalisations = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxLocalisationsAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.localisation.overrides.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.localisation.overrides.desc")

    override fun entityName() = ChronicleBundle.message("rename.localisation.overrides.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull() ?: return
        val type = element.type ?: return
        ProgressManager.checkCanceled()
        val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
        val targets = ParadoxLocalisationSearch.search(name, selector, type).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

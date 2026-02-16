package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.process
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.Processors
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class AutomaticLocalisationsRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxLocalisationProperty) return false
        val name = element.name.orNull() ?: return false
        val type = element.type ?: return false
        val selector = selector(element.project, element).localisation().contextSensitive()
        val processor = Processors.duplicate<ParadoxLocalisationProperty>()
        ParadoxLocalisationSearch.search(name, type, selector).process(processor)
        return processor.duplicated
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.localisation.overrides")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameLocalisations
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameLocalisations = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticLocalisationsRenamer(element, newName)
    }
}

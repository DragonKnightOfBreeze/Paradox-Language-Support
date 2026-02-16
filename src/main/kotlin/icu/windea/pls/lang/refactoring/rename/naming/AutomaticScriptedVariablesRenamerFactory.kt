package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.core.process
import icu.windea.pls.core.util.Processors
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class AutomaticScriptedVariablesRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxScriptScriptedVariable) return false
        val name = element.name?.orNull() ?: return false

        val selector = selector(element.project, element).scriptedVariable()
        val processor = Processors.duplicate<ParadoxScriptScriptedVariable>()
        ParadoxScriptedVariableSearch.searchLocal(name, selector).process(processor)
        if (!processor.duplicated) {
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).process(processor)
        }
        return processor.duplicated
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.scriptedVariable.overrides")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameScriptedVariables
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameScriptedVariables = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticScriptedVariablesRenamer(element, newName)
    }
}

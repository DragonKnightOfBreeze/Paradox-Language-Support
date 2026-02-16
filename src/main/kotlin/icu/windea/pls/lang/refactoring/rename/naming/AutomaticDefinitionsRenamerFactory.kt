package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.process
import icu.windea.pls.core.util.Processors
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptProperty

class AutomaticDefinitionsRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if (element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        val name = definitionInfo.name
        val type = definitionInfo.type
        if (name.isEmpty()) return false

        val selector = selector(element.project, element).definition()
        val processor = Processors.duplicate<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.searchProperty(name, type, selector).process(processor)
        return processor.duplicated
    }

    override fun getOptionName(): String {
        return PlsBundle.message("rename.definition.overrides")
    }

    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameDefinitions
    }

    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameDefinitions = enabled
    }

    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticDefinitionsRenamer(element, newName)
    }
}

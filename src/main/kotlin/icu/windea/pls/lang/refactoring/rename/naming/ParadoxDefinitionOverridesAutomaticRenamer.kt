package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.process
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 用于在重命名定义时，自动重命名重载项（如果存在）。
 */
class ParadoxDefinitionOverridesAutomaticRenamer(element: PsiElement, newName: String) : ParadoxDefinitionAutomaticRenamer() {
    class Factory : ParadoxDefinitionAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxScriptProperty) return false
            val definitionInfo = element.definitionInfo ?: return false
            val name = definitionInfo.name
            val type = definitionInfo.type
            if (name.isEmpty()) return false
            val selector = ParadoxDefinitionSearch.selector(element.project, element)
            val processor = ProcessorFactory.duplicate<ParadoxScriptProperty>()
            ParadoxDefinitionSearch.searchProperty(name, type, selector).process(processor)
            return processor.result
        }

        override fun getOptionName() = ChronicleBundle.message("rename.definition.overrides")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameDefinitions
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameDefinitions = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxDefinitionOverridesAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.definition.overrides.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.definition.overrides.desc")

    override fun entityName() = ChronicleBundle.message("rename.definition.overrides.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDefinitionElement) return
        val definitionInfo = element.definitionInfo ?: return
        val name = definitionInfo.name
        val type = definitionInfo.type
        if (name.isEmpty()) return
        ProgressManager.checkCanceled()
        val selector = ParadoxDefinitionSearch.selector(element.project, element).contextSensitive()
        val targets = ParadoxDefinitionSearch.searchElement(name, type, selector).findAll()
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

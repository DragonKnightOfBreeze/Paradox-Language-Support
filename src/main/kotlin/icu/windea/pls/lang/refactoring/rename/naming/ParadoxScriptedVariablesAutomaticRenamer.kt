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
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于在重命名封装变量时，自动重命名重载项（如果存在）。
 */
class ParadoxScriptedVariablesAutomaticRenamer(element: PsiElement, newName: String) : ParadoxScriptedVariableAutomaticRenamer() {
    class Factory : ParadoxScriptedVariableAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxScriptScriptedVariable) return false
            val name = element.name?.orNull() ?: return false
            val selector = ParadoxScriptedVariableSearch.selector(element.project, element)
            val processor = ProcessorFactory.duplicate<ParadoxScriptScriptedVariable>()
            ParadoxScriptedVariableSearch.searchLocal(name, selector).process(processor)
            if (!processor.result) {
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).process(processor)
            }
            return processor.result
        }

        override fun getOptionName() = ChronicleBundle.message("rename.scriptedVariable.overrides")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameScriptedVariables
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameScriptedVariables = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxScriptedVariablesAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.scriptedVariable.overrides.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.scriptedVariable.overrides.desc")

    override fun entityName() = ChronicleBundle.message("rename.scriptedVariable.overrides.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxScriptScriptedVariable) return
        val name = element.name?.orNull() ?: return
        ProgressManager.checkCanceled()
        val selector = ParadoxScriptedVariableSearch.selector(element.project, element).contextSensitive()
        val targets = mutableSetOf<ParadoxScriptScriptedVariable>()
        ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { targets.addAll(it) }
        ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { targets.addAll(it) }
        for (target in targets) {
            ProgressManager.checkCanceled()
            if (target == element) continue
            allRenames[target] = newName
        }
    }
}

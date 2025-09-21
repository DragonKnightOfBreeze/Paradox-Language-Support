package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于在重命名定义时自动重命名由其生成的修正（如果存在）。
 */
class AutomaticGeneratedModifiersRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
    init {
        element as ParadoxScriptDefinitionElement
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for ((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }

    override fun isSelectedByDefault() = true

    override fun allowChangeSuggestedName() = false

    override fun getDialogTitle() = PlsBundle.message("rename.generatedModifiers.title")

    override fun getDialogDescription() = PlsBundle.message("rename.generatedModifiers.desc")

    override fun entityName() = PlsBundle.message("rename.generatedModifiers.entityName")

    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.modifiers.orNull() ?: return
        for (info in infos) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            val newModifierName = CwtTemplateExpressionManager.extract(info.config.template, newName)
            val modifierElement = ParadoxModifierElement(element, modifierName, definitionInfo.gameType, definitionInfo.project)
            modifierElement.canRename = true
            allRenames[modifierElement] = newModifierName
        }
    }
}

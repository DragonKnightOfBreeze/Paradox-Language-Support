package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为名字和描述的本地化（如果存在）。
 */
class AutomaticGeneratedModifiersNameDescRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.generatedModifiersNameDesc.title")

    override fun getDialogDescription() = PlsBundle.message("rename.generatedModifiersNameDesc.desc")

    override fun entityName() = PlsBundle.message("rename.generatedModifiersNameDesc.entityName")

    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.modifiers.orNull() ?: return
        val project = definitionInfo.project
        for (info in infos) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            val newModifierName = CwtTemplateExpressionManager.extract(info.config.template, newName)
            run {
                //use first key only -> $_name
                val key = ParadoxModifierManager.getModifierNameKeys(modifierName, element).firstOrNull() ?: return@run
                val newKey = ParadoxModifierManager.getModifierNameKeys(newModifierName, element).firstOrNull() ?: return@run
                val selector = selector(project, element).localisation()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                    .withConstraint(ParadoxLocalisationConstraint.Modifier)
                val result = ParadoxLocalisationSearch.search(key, selector).findAll()
                result.forEach { allRenames[it] = newKey }
            }
            run {
                //use first key only -> $_desc
                val key = ParadoxModifierManager.getModifierNameKeys(modifierName, element).firstOrNull() ?: return@run
                val newKey = ParadoxModifierManager.getModifierDescKeys(newModifierName, element).firstOrNull() ?: return@run
                val selector = selector(project, element).localisation()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                    .withConstraint(ParadoxLocalisationConstraint.Modifier)
                val result = ParadoxLocalisationSearch.search(key, selector).findAll()
                result.forEach { allRenames[it] = newKey }
            }
        }
    }
}

package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为名字和描述的本地化（如果存在）。
 */
class AutomaticGeneratedModifiersNameDescRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
    init {
        element as ParadoxScriptDefinitionElement
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }
    
    override fun isSelectedByDefault() = true
    
    override fun allowChangeSuggestedName() = false
    
    override fun getDialogTitle() = PlsBundle.message("rename.generatedModifiersNameDesc.title")
    
    @Suppress("DialogTitleCapitalization")
    override fun getDialogDescription() = PlsBundle.message("rename.generatedModifiersNameDesc.desc")
    
    override fun entityName() = PlsBundle.message("rename.generatedModifiersNameDesc.entityName")
    
    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.modifiers.orNull() ?: return
        val project = definitionInfo.project
        for(info in infos) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            val newModifierName = info.config.template.extract(newName)
            run {
                //use first key only -> $_name
                val key = ParadoxModifierHandler.getModifierNameKeys(modifierName, element).firstOrNull() ?: return@run
                val newKey = ParadoxModifierHandler.getModifierNameKeys(newModifierName, element).firstOrNull() ?: return@run
                val selector = localisationSelector(project, element)
                    .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
                    .withConstraint(ParadoxLocalisationConstraint.Modifier)
                val result = ParadoxLocalisationSearch.search(key, selector).findAll()
                result.forEach { allRenames[it] =  newKey}
            }
            run {
                //use first key only -> $_desc
                val key = ParadoxModifierHandler.getModifierNameKeys(modifierName, element).firstOrNull() ?: return@run
                val newKey = ParadoxModifierHandler.getModifierDescKeys(newModifierName, element).firstOrNull() ?: return@run
                val selector = localisationSelector(project, element)
                    .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
                    .withConstraint(ParadoxLocalisationConstraint.Modifier)
                val result = ParadoxLocalisationSearch.search(key, selector).findAll()
                result.forEach { allRenames[it] =  newKey}
            }
        }
    }
}
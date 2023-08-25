package icu.windea.pls.core.refactoring.rename.naming

import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticRelatedLocalisationsRenamer(element: ParadoxScriptDefinitionElement, newName: String) : AutomaticRenamer() {
    init {
        val allRenames = mutableMapOf<PsiElement, String>()
        prepareRenaming(element, newName, allRenames)
        for((key, value) in allRenames) {
            myElements.add(key as PsiNamedElement)
            suggestAllNames(key.name, value)
        }
    }
    
    override fun isSelectedByDefault() = true
    
    override fun allowChangeSuggestedName() = false
    
    override fun getDialogTitle() = PlsBundle.message("rename.relatedLocalisations.title")
    
    @Suppress("DialogTitleCapitalization")
    override fun getDialogDescription() = PlsBundle.message("rename.relatedLocalisations.desc")
    
    override fun entityName() = PlsBundle.message("rename.relatedLocalisations.entityName")
    
    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: Map<PsiElement, String>) {
        
    }
}

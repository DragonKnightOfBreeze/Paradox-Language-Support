package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为图标的图片（重命名文件名，如果存在）。
 */
class AutomaticGeneratedModifiersIconRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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
    
    override fun getDialogTitle() = PlsBundle.message("rename.generatedModifiersIcon.title")
    
    @Suppress("DialogTitleCapitalization")
    override fun getDialogDescription() = PlsBundle.message("rename.generatedModifiersIcon.desc")
    
    override fun entityName() = PlsBundle.message("rename.generatedModifiersIcon.entityName")
    
    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.modifiers.orNull() ?: return
        val project = definitionInfo.project
        for(info in infos) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            val newModifierName = info.config.template.extract(newName)
            run {
                //use first key only -> gfx/interface/icons/modifiers/mod_$
                val iconPath = ParadoxModifierHandler.getModifierIconPaths(modifierName, element).firstOrNull() ?: return@run
                val newIconPath = ParadoxModifierHandler.getModifierIconPaths(newModifierName, element).firstOrNull() ?: return@run
                val newIconName = newIconPath.substringAfterLast('/')
                val iconSelector = fileSelector(project, element)
                val result = ParadoxFilePathSearch.searchIcon(iconPath, iconSelector).findAll()
                result.mapNotNull { it.toPsiFile(project) }.forEach { allRenames[it] =  newIconName + "." + it.name.substringAfterLast('.')}
            }
        }
    }
}


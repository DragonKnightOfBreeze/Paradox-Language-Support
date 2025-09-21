package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为图标的图片（重命名文件名，如果存在）。
 */
class AutomaticGeneratedModifiersIconRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.generatedModifiersIcon.title")

    override fun getDialogDescription() = PlsBundle.message("rename.generatedModifiersIcon.desc")

    override fun entityName() = PlsBundle.message("rename.generatedModifiersIcon.entityName")

    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.modifiers.orNull() ?: return
        val project = definitionInfo.project
        for (info in infos) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            val newModifierName = CwtTemplateExpressionManager.extract(info.config.template, newName)
            run {
                //use first key only -> gfx/interface/icons/modifiers/mod_$
                val iconPath = ParadoxModifierManager.getModifierIconPaths(modifierName, element).firstOrNull() ?: return@run
                val newIconPath = ParadoxModifierManager.getModifierIconPaths(newModifierName, element).firstOrNull() ?: return@run
                val newIconName = newIconPath.substringAfterLast('/')
                val iconSelector = selector(project, element).file()
                val result = ParadoxFilePathSearch.searchIcon(iconPath, iconSelector).findAll()
                result.mapNotNull { it.toPsiFile(project) }.forEach { allRenames[it] = newIconName + "." + it.name.substringAfterLast('.') }
            }
        }
    }
}


package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名相关图片（重命名文件名，如果存在且需要）。
 */
class AutomaticRelatedImagesRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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
    
    override fun getDialogTitle() = PlsBundle.message("rename.relatedImages.title")
    
    @Suppress("DialogTitleCapitalization")
    override fun getDialogDescription() = PlsBundle.message("rename.relatedImages.desc")
    
    override fun entityName() = PlsBundle.message("rename.relatedImages.entityName")
    
    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.images.orNull() ?: return
        for(info in infos) {
            ProgressManager.checkCanceled()
            val result = info.locationExpression.resolveAll(element, definitionInfo) ?: continue
            val rename =  info.locationExpression.resolvePlaceholder(newName) ?: continue
            val finalRename = if(rename.startsWith("GFX_")) rename else rename.substringAfterLast('/')
            result.elements.forEach { allRenames[it] = finalRename }
        }
    }
}
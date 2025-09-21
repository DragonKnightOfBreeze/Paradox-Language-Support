package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.CwtLocationExpressionManager
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于在重命名定义时自动重命名相关图片（重命名文件名，如果存在且需要）。
 */
class AutomaticRelatedImagesRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.relatedImages.title")

    override fun getDialogDescription() = PlsBundle.message("rename.relatedImages.desc")

    override fun entityName() = PlsBundle.message("rename.relatedImages.entityName")

    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.images.orNull() ?: return
        for (info in infos) {
            ProgressManager.checkCanceled()
            val resolveResult = CwtLocationExpressionManager.resolve(info.locationExpression, element, definitionInfo) ?: continue
            val rename = CwtLocationExpressionManager.resolvePlaceholder(info.locationExpression, newName) ?: continue
            val finalRename = if (rename.startsWith("GFX_")) rename else rename.substringAfterLast('/')
            resolveResult.elements.forEach { allRenames[it] = finalRename }
        }
    }
}

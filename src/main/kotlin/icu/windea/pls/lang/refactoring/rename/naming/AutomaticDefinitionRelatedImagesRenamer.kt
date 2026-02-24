package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.resolve.ParadoxConfigExpressionService
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于在重命名定义时，自动重命名相关图片（重命名文件名，如果存在且需要）。
 */
class AutomaticDefinitionRelatedImagesRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.definition.relatedImages.title")

    override fun getDialogDescription() = PlsBundle.message("rename.definition.relatedImages.desc")

    override fun entityName() = PlsBundle.message("rename.definition.relatedImages.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDefinitionElement) return
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.images.orNull() ?: return
        for (info in infos) {
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxConfigExpressionService.resolve(info.locationExpression, element, definitionInfo) ?: continue
            val rename1 = CwtConfigExpressionManager.resolvePlaceholder(info.locationExpression, newName) ?: continue
            val rename = if (rename1.startsWith("GFX_")) rename1 else rename1.substringAfterLast('/')
            for (resolved in resolveResult.elements) {
                if (resolved !is PsiNamedElement) continue
                allRenames[resolved] = rename
            }
        }
    }
}

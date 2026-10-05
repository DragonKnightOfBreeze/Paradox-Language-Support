package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.usageView.UsageInfo
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.refactoring.ParadoxRefactoringSettings
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService

/**
 * 用于在重命名定义时，自动重命名相关图片（重命名文件名，如果存在且需要）。
 */
class ParadoxDefinitionRelatedImagesAutomaticRenamer(element: PsiElement, newName: String) : ParadoxDefinitionAutomaticRenamer() {
    class Factory : ParadoxDefinitionAutomaticRenamer.Factory() {
        override fun isApplicable(element: PsiElement): Boolean {
            if (element !is ParadoxDefinitionElement) return false
            val definitionInfo = element.definitionInfo ?: return false
            return definitionInfo.images.isNotEmpty()
        }

        override fun getOptionName() = ChronicleBundle.message("rename.definition.relatedImages")

        override fun isEnabled(): Boolean {
            return ParadoxRefactoringSettings.getInstance().renameRelatedImagesForDefinitions
        }

        override fun setEnabled(enabled: Boolean) {
            ParadoxRefactoringSettings.getInstance().renameRelatedImagesForDefinitions = enabled
        }

        override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
            return ParadoxDefinitionRelatedImagesAutomaticRenamer(element, newName)
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

    override fun getDialogTitle() = ChronicleBundle.message("rename.definition.relatedImages.title")

    override fun getDialogDescription() = ChronicleBundle.message("rename.definition.relatedImages.desc")

    override fun entityName() = ChronicleBundle.message("rename.definition.relatedImages.entityName")

    private fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiNamedElement, String>) {
        if (element !is ParadoxDefinitionElement) return
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.images.orNull() ?: return
        for (info in infos) {
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxLocationExpressionService.resolve(info.locationExpression, element, definitionInfo) ?: continue
            val rename1 = CwtConfigExpressionManager.resolvePlaceholder(info.locationExpression, newName) ?: continue
            val rename = if (rename1.startsWith("GFX_")) rename1 else rename1.substringAfterLast('/')
            for (resolved in resolveResult.elements) {
                if (resolved !is PsiNamedElement) continue
                allRenames[resolved] = rename
            }
        }
    }
}

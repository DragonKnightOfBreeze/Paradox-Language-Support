package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticRelatedLocalisationsRenamer(element: PsiElement, newName: String) : AutomaticRenamer() {
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

    override fun getDialogTitle() = PlsBundle.message("rename.relatedLocalisations.title")

    override fun getDialogDescription() = PlsBundle.message("rename.relatedLocalisations.desc")

    override fun entityName() = PlsBundle.message("rename.relatedLocalisations.entityName")

    private fun prepareRenaming(element: ParadoxScriptDefinitionElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val definitionInfo = element.definitionInfo ?: return
        val infos = definitionInfo.localisations.orNull() ?: return
        for (info in infos) {
            ProgressManager.checkCanceled()
            val resolveResult = CwtLocationExpressionManager.resolve(info.locationExpression, element, definitionInfo) ?: continue
            val rename = CwtLocationExpressionManager.resolvePlaceholder(info.locationExpression, newName) ?: continue
            resolveResult.elements.forEach { allRenames[it] = rename }
        }
    }
}

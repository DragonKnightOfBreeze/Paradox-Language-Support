package icu.windea.pls.core.refactoring.rename.naming

import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名相关本地化（如果存在且需要）。
 */
class AutomaticRelatedLocalisationsRenamerFactory: AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if(element !is ParadoxScriptDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.localisations.isNotEmpty()
    }
    
    override fun getOptionName(): String {
        return PlsBundle.message("rename.relatedLocalisations")
    }
    
    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.instance.renameRelatedLocalisations
    }
    
    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.instance.renameRelatedLocalisations = enabled
    }
    
    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticRelatedLocalisationsRenamer(element, newName)
    }
}
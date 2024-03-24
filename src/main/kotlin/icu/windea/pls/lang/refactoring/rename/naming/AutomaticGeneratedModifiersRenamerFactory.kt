package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名由其生成的修正（如果存在）。
 */
class AutomaticGeneratedModifiersRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if(element !is ParadoxScriptDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.modifiers.isNotEmpty()
    }
    
    override fun getOptionName(): String {
        return PlsBundle.message("rename.generatedModifiers")
    }
    
    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameGeneratedModifier
    }
    
    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameGeneratedModifier = enabled
    }
    
    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticGeneratedModifiersRenamer(element, newName)
    }
}
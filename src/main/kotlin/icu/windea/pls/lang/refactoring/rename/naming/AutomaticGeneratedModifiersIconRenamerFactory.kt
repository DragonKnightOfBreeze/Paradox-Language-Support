package icu.windea.pls.lang.refactoring.rename.naming

import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.refactoring.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为图标的图片（重命名文件名，如果存在）。
 */
class AutomaticGeneratedModifiersIconRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if(element !is ParadoxScriptDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.modifiers.isNotEmpty()
    }
    
    override fun getOptionName(): String {
        return PlsBundle.message("rename.generatedModifiersIcon")
    }
    
    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameGeneratedModifierIcon
    }
    
    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameGeneratedModifierIcon = enabled
    }
    
    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticGeneratedModifiersIconRenamer(element, newName)
    }
}

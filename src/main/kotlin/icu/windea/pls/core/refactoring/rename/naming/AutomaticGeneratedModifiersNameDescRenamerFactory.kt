package icu.windea.pls.core.refactoring.rename.naming

import com.intellij.psi.*
import com.intellij.refactoring.rename.naming.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*

/**
 * 用于在重命名定义时自动重命名由其生成的修正的作为名字和描述的本地化（如果存在）。
 */
class AutomaticGeneratedModifiersNameDescRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        return element is ParadoxScriptDefinitionElement && element.definitionInfo != null
    }
    
    override fun getOptionName(): String {
        return PlsBundle.message("rename.generatedModifiersNameDesc")
    }
    
    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.instance.renameGeneratedModifierNameDesc
    }
    
    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.instance.renameGeneratedModifierNameDesc = enabled
    }
    
    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticGeneratedModifiersNameDescRenamer(element, newName)
    }
}
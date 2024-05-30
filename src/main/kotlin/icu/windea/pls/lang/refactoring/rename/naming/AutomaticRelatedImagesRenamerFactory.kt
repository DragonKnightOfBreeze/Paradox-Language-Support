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
 * 用于在重命名定义时自动重命名相关图片（重命名文件名，如果存在且需要）。
 */
class AutomaticRelatedImagesRenamerFactory : AutomaticRenamerFactory {
    override fun isApplicable(element: PsiElement): Boolean {
        if(element !is ParadoxScriptDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        return definitionInfo.images.isNotEmpty()
    }
    
    override fun getOptionName(): String {
        return PlsBundle.message("rename.relatedImages")
    }
    
    override fun isEnabled(): Boolean {
        return ParadoxRefactoringSettings.getInstance().renameRelatedImages
    }
    
    override fun setEnabled(enabled: Boolean) {
        ParadoxRefactoringSettings.getInstance().renameRelatedImages = enabled
    }
    
    override fun createRenamer(element: PsiElement, newName: String, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        return AutomaticRelatedImagesRenamer(element, newName)
    }
}
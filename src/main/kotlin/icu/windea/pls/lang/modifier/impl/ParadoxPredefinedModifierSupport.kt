package icu.windea.pls.lang.modifier.impl

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

/**
 * 提供对预定义的修正的支持。
 */
class ParadoxPredefinedModifierSupport: ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        TODO("Not yet implemented")
    }
    
    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        TODO("Not yet implemented")
    }
    
    override fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        TODO("Not yet implemented")
    }
}
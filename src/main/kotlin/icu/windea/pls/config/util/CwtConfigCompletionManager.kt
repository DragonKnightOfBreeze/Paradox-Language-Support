package icu.windea.pls.config.util

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.codeInsight.completion.*

object CwtConfigCompletionManager {
    fun initializeContext(parameters: CompletionParameters, context: ProcessingContext, contextElement: PsiElement): Boolean {
        val configGroup = CwtConfigManager.getContainingConfigGroup(parameters.originalFile) ?: return false
        context.configGroup = configGroup
        
        context.parameters = parameters
        context.completionIds = mutableSetOf<String>().synced()
        
        val quoted = contextElement.text.isLeftQuoted()
        val rightQuoted = contextElement.text.isRightQuoted()
        val offsetInParent = parameters.offset - contextElement.startOffset
        val keyword = contextElement.getKeyword(offsetInParent)
        
        context.contextElement = contextElement
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        
        return true
    }
    
    fun addConfigCompletions(context: ProcessingContext, result: CompletionResultSet) {
        //TODO 1.3.18
    }
}

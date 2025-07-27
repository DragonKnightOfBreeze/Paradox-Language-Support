package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.codeInsight.completion.*

class ParadoxCsvExpressionCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxCsvColumn>() ?: return

        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted

        ParadoxCompletionManager.addColumnCompletions(element, context, result)
    }
}

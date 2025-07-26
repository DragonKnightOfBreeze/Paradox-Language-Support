package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvHeaderColumnCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxCsvColumn>() ?: return
        val project = parameters.originalFile.project
        //TODO 2.0.1-dev
    }
}

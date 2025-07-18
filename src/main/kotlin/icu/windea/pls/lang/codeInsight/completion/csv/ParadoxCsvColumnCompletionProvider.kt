package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.csv.psi.ParadoxCsvColumn

class ParadoxCsvColumnCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxCsvColumn>() ?: return
        val project = parameters.originalFile.project
        //TODO 2.0.1-dev
    }
}

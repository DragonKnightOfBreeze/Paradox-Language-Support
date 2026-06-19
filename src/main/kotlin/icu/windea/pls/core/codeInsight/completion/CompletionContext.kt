package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext

interface CompletionContext {
    val parameters: CompletionParameters
    val context: ProcessingContext

    val completionIds: MutableSet<String>
    val file: PsiFile
    val offset: Int
    val editor: Editor
    val project: Project
}


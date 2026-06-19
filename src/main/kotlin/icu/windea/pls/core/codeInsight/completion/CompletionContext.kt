package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted

data class CompletionContext<T : PsiElement>(
    val contextElement: T,
    val parameters: CompletionParameters,
    val context: ProcessingContext,
) {
    val offsetInParent: Int = parameters.offset - contextElement.startOffset
    val keyword: String = contextElement.getKeyword(offsetInParent)
    val leftQuoted: Boolean = contextElement.text.isLeftQuoted()
    val rightQuoted: Boolean = contextElement.text.isRightQuoted()
    val quoted: Boolean get() = leftQuoted

    val file: PsiFile get() = parameters.originalFile
    val offset: Int get() = parameters.offset
    val editor: Editor get() = parameters.editor
    val project: Project get() = parameters.originalFile.project
}

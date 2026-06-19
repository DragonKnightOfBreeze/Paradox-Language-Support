package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted

data class GlobalCompletionContext(
    val contextElement: PsiElement,
    override val parameters: CompletionParameters,
    override val context: ProcessingContext,
) : CompletionContext {
    val offsetInParent: Int = parameters.offset - contextElement.startOffset
    val keyword: String = contextElement.getKeyword(offsetInParent)
    val leftQuoted: Boolean = contextElement.text.isLeftQuoted()
    val rightQuoted: Boolean = contextElement.text.isRightQuoted()
    val quoted: Boolean get() = leftQuoted

    override val completionIds: MutableSet<String> = mutableSetOf<String>().synced()
    override val file: PsiFile get() = parameters.originalFile
    override val offset: Int get() = parameters.offset
    override val editor: Editor get() = parameters.editor
    override val project: Project get() = parameters.originalFile.project

    companion object {
        @JvmStatic
        fun <T : PsiElement> create(contextElement: T, parameters: CompletionParameters, context: ProcessingContext): GlobalCompletionContext {
            return GlobalCompletionContext(contextElement, parameters, context)
        }
    }
}

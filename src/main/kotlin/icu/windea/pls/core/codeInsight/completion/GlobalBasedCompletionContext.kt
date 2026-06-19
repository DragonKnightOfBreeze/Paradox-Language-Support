package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.getKeyword
import icu.windea.pls.lang.isIdentifier

abstract class GlobalBasedCompletionContext: CompletionContext {
    abstract val globalContext: GlobalCompletionContext

    val contextElement: PsiElement get() = globalContext.contextElement
    val offsetInParent: Int get() = globalContext.offsetInParent
    val leftQuoted: Boolean get() = globalContext.leftQuoted
    val rightQuoted: Boolean get() = globalContext.rightQuoted
    val quoted: Boolean get() = globalContext.quoted

    override val context: ProcessingContext get() = globalContext.context
    override val parameters: CompletionParameters get() = globalContext.parameters
    override val completionIds: MutableSet<String> get() = globalContext.completionIds
    override val file: PsiFile get() = globalContext.file
    override val offset: Int get() = globalContext.offset
    override val editor: Editor get() = globalContext.editor
    override val project: Project get() = globalContext.project

    fun isIdentifierKeyword(): Boolean {
        val keyword = keyword
        return keyword.isEmpty() || keyword.isIdentifier()
    }
}

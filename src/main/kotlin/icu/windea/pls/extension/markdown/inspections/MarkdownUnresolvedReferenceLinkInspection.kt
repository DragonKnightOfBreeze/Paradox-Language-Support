package icu.windea.pls.extension.markdown.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.lang.references.paths.*
import org.intellij.plugins.markdown.lang.psi.*
import org.intellij.plugins.markdown.lang.psi.impl.*

//org.intellij.plugins.markdown.lang.references.paths.MarkdownUnresolvedFileReferenceInspection

class MarkdownUnresolvedReferenceLinkInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : MarkdownElementVisitor() {
            override fun visitLinkDestination(linkDestination: MarkdownLinkDestination) {
                checkReference(linkDestination, holder)
            }
        }
    }

    private fun checkReference(element: PsiElement, holder: ProblemsHolder) {
        val references = element.references.asSequence()
        val reference = references.find { it is ParadoxPathReference } ?: return
        val unresolvedReference = reference.takeIf { it.resolve() == null } ?: return
        holder.registerProblem(
            unresolvedReference,
            ProblemsHolder.unresolvedReferenceMessage(unresolvedReference),
            ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
        )
    }
}

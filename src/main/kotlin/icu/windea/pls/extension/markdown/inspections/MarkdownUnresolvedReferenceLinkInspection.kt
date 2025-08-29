package icu.windea.pls.extension.markdown.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.lang.references.paths.ParadoxPathReference
import org.intellij.plugins.markdown.lang.psi.MarkdownElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestination

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

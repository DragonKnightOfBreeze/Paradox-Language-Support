package icu.windea.pls.core.psi

import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPlainText
import com.intellij.psi.PsiPlainTextFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.templateLanguages.OuterLanguageElement

// com.intellij.codeInsight.daemon.impl.InspectionVisitorOptimizer
// com.intellij.psi.BasicInspectionVisitorBean

abstract class PsiFileOnlyVisitor : PsiElementVisitor() {
    final override fun visitBinaryFile(file: PsiBinaryFile) {
        super.visitBinaryFile(file)
    }

    final override fun visitComment(comment: PsiComment) {
        super.visitComment(comment)
    }

    final override fun visitDirectory(dir: PsiDirectory) {
        super.visitDirectory(dir)
    }

    final override fun visitElement(element: PsiElement) {
        super.visitElement(element)
    }

    final override fun visitErrorElement(element: PsiErrorElement) {
        super.visitErrorElement(element)
    }

    @Suppress("RedundantOverride")
    override fun visitFile(file: PsiFile) {
        super.visitFile(file)
    }

    final override fun visitOuterLanguageElement(element: OuterLanguageElement) {
        super.visitOuterLanguageElement(element)
    }

    final override fun visitPlainText(content: PsiPlainText) {
        super.visitPlainText(content)
    }

    final override fun visitPlainTextFile(file: PsiPlainTextFile) {
        super.visitPlainTextFile(file)
    }

    final override fun visitWhiteSpace(space: PsiWhiteSpace) {
        super.visitWhiteSpace(space)
    }
}

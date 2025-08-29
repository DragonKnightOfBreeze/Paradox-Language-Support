package icu.windea.pls.lang.inspections.lints

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.intellij.psi.PsiElement

//com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection

class PlsTigerLintInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {
    companion object {
        const val SHORT_NAME = "PlsTigerLint"
    }

    override fun getBatchSuppressActions(element: PsiElement?): Array<out SuppressQuickFix?> {
        return super.getBatchSuppressActions(element)
    }
}

package icu.windea.pls.lang.inspections.lints

import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.*
import com.intellij.psi.*

//com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection

class PlsTigerLintInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {
    companion object {
        const val SHORT_NAME = "PlsTigerLint"
    }

    override fun getBatchSuppressActions(element: PsiElement?): Array<out SuppressQuickFix?> {
        return super.getBatchSuppressActions(element)
    }
}

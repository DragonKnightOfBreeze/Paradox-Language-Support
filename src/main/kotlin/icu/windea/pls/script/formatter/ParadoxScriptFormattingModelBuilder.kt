package icu.windea.pls.script.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider

class ParadoxScriptFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            ParadoxScriptBlock(formattingContext.psiElement.node, formattingContext.codeStyleSettings),
            formattingContext.codeStyleSettings
        )
    }
}

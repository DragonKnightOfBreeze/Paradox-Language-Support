package icu.windea.pls.cwt.formatter

import com.intellij.formatting.*

class CwtFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            CwtBlock(formattingContext.psiElement.node, formattingContext.codeStyleSettings),
            formattingContext.codeStyleSettings
        )
    }
}

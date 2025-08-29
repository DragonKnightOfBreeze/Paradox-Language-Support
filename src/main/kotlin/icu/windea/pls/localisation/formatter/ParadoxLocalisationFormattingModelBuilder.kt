package icu.windea.pls.localisation.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider

class ParadoxLocalisationFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            ParadoxLocalisationBlock(formattingContext.node, formattingContext.codeStyleSettings),
            formattingContext.codeStyleSettings
        )
    }
}


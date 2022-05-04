package icu.windea.pls.localisation.formatter

import com.intellij.formatting.*

class ParadoxLocalisationFormattingModelBuilder : FormattingModelBuilder {
	override fun createModel(formattingContext: FormattingContext): FormattingModel {
		return FormattingModelProvider.createFormattingModelForPsiFile(
			formattingContext.containingFile,
			ParadoxLocalisationBlock(formattingContext.node, formattingContext.codeStyleSettings, true),
			formattingContext.codeStyleSettings
		)
	}
}


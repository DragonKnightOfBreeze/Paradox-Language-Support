package com.windea.plugin.idea.pls.script.formatter

import com.intellij.formatting.*

class ParadoxScriptFormattingModelBuilder : FormattingModelBuilder {
	override fun createModel(formattingContext: FormattingContext): FormattingModel {
		return FormattingModelProvider.createFormattingModelForPsiFile(
			formattingContext.containingFile,
			ParadoxScriptBlock(formattingContext.psiElement.node, formattingContext.codeStyleSettings),
			formattingContext.codeStyleSettings
		)
	}
}

package com.windea.plugin.idea.pls.cwt.formatter

import com.intellij.formatting.*
import com.windea.plugin.idea.pls.script.formatter.*

class CwtFormattingModelBuilder:FormattingModelBuilder {
	override fun createModel(formattingContext: FormattingContext): FormattingModel {
		return FormattingModelProvider.createFormattingModelForPsiFile(
			formattingContext.containingFile,
			ParadoxScriptBlock(formattingContext.psiElement.node, formattingContext.codeStyleSettings),
			formattingContext.codeStyleSettings
		)
	}
}
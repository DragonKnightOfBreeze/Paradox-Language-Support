package com.windea.plugin.idea.paradox.script.formatter

import com.intellij.formatting.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*

class ParadoxScriptFormattingModelBuilder : FormattingModelBuilder {
	override fun createModel(formattingContext: FormattingContext): FormattingModel {
		return FormattingModelProvider.createFormattingModelForPsiFile(
			formattingContext.containingFile,
			ParadoxScriptBlock(formattingContext.psiElement.node, formattingContext.codeStyleSettings),
			formattingContext.codeStyleSettings
		)
	}
}

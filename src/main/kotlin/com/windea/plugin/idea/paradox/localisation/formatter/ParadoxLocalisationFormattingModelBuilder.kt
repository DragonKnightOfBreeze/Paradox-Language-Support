@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.formatter

import com.intellij.formatting.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.localisation.formatter.*

class ParadoxLocalisationFormattingModelBuilder : FormattingModelBuilder {
	override fun createModel(formattingContext: FormattingContext): FormattingModel {
		return FormattingModelProvider.createFormattingModelForPsiFile(
			formattingContext.containingFile,
			ParadoxLocalisationBlock(formattingContext.node, formattingContext.codeStyleSettings),
			formattingContext.codeStyleSettings
		)
	}
}


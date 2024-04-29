package icu.windea.pls.lang.editor.folding

import com.intellij.application.options.editor.*
import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import icu.windea.pls.*

class ParadoxFoldingOptionsProvider : BeanConfigurable<ParadoxFoldingSettings>, CodeFoldingOptionsProvider {
	constructor(): super(ParadoxFoldingSettings.getInstance(), PlsBundle.message("settings.folding")){
		val settings = instance
		checkBox(PlsBundle.message("settings.folding.parameterConditions"), settings::collapseParameterConditions)
		checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"), settings::collapseInlineMathBlocks)
		checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"), settings::collapseScriptedVariableReferences)
		checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"), settings::collapseVariableOperationExpressions)
	}
}

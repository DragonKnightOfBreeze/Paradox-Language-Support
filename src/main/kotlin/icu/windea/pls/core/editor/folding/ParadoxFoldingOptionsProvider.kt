package icu.windea.pls.core.editor.folding

import com.intellij.application.options.editor.*
import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*

class ParadoxFoldingOptionsProvider : BeanConfigurable<ParadoxFoldingSettings>, CodeFoldingOptionsProvider {
	constructor(): super(service(), PlsBundle.message("settings.folding")){
		val settings = instance!!
		checkBox(PlsBundle.message("settings.folding.parameterConditions"), settings::collapseParameterConditions)
		checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"), settings::collapseInlineMathBlocks)
		checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"), settings::collapseScriptedVariableReferences)
		checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"), settings::collapseVariableOperationExpressions)
	}
}

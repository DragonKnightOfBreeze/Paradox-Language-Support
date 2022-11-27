package icu.windea.pls.core.settings

import com.intellij.openapi.components.*

class ParadoxFoldingSettings {
	var collapseVariableOperationExpressions = false
	
	companion object {
		@JvmStatic
		fun getInstance() = service<ParadoxFoldingSettings>()
	}
}

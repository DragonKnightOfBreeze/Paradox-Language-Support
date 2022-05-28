package icu.windea.pls.localisation.ui.actions.styling

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*

class FloatingToolbarGroup : ActionGroup() {
	override fun isPopup() = true
	
	override fun getChildren(e: AnActionEvent?): Array<AnAction> {
		val project = e?.project ?: getDefaultProject()
		val colorConfigs = getInternalConfig(project).colors
		if(colorConfigs.isEmpty()) return EMPTY_ARRAY
		return colorConfigs.mapToArray { SetColorAction(it) }
	}
}
package icu.windea.pls.core.tool.actions

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.core.*

@Suppress("MissingActionUpdateThread")
class PlsToolsActionGroup: DefaultActionGroup() {
    override fun update(e: AnActionEvent) {
        val place = e.place
        if(place == ActionPlaces.EDITOR_POPUP || place == ActionPlaces.PROJECT_VIEW_POPUP) {
            val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
            val fileInfo = virtualFile?.fileInfo
            e.presentation.isEnabledAndVisible = fileInfo != null
        } else {
            e.presentation.isEnabledAndVisible = true
        }
    }
}
package icu.windea.pls.core.settings

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*

class ParadoxModSettingsDialog(
    val project: Project,
    val modSettings: ParadoxModSettingsState,
): DialogWrapper(project, true) {
    override fun createCenterPanel() = panel { 
        //TODO
    }
}
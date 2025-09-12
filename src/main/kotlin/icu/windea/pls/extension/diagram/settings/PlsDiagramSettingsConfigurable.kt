package icu.windea.pls.extension.diagram.settings

import com.intellij.diagram.DiagramProvider
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.extension.diagram.provider.ParadoxDiagramProvider

class PlsDiagramSettingsConfigurable(
    private val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("settings.diagram")), SearchableConfigurable {
    override fun getId() = "pls.diagram"

    override fun getHelpTopic() = "icu.windea.pls.diagram.settings"

    override fun createPanel(): DialogPanel {
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectSettings"))
            }

            for (diagramSettings in getDiagramSettingsList()) {
                collapsibleGroup(diagramSettings.groupName) {
                    diagramSettings.groupBuilder(this)
                }
            }
        }
    }

    override fun apply() {
        super.apply()
        for (diagramSettings in getDiagramSettingsList()) {
            diagramSettings.state.updateSettings()
        }
    }

    private fun getDiagramSettingsList(): List<ParadoxDiagramSettings<*>> {
        val list = mutableListOf<ParadoxDiagramSettings<*>>()
        for (provider in DiagramProvider.DIAGRAM_PROVIDER.extensionList) {
            if (provider !is ParadoxDiagramProvider) continue
            val settings = provider.getDiagramSettings(project) ?: continue
            list += settings
        }
        return list
    }
}

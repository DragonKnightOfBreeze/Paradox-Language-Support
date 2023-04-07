package icu.windea.pls.extension.diagram.settings

import com.intellij.diagram.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*

class ParadoxDiagramSettingsConfigurable(
    val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("settings.diagram")), SearchableConfigurable {
    override fun getId() = "settings.language.pls.diagram"
    
    override fun createPanel(): DialogPanel {
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectSettings"))
            }
            for(provider in DiagramProvider.DIAGRAM_PROVIDER.extensionList) {
                if(provider !is ParadoxDiagramProvider) continue
                val settings = provider.getDiagramSettings(project) ?: continue
                val text = provider.presentableName
                indent {
                    row {
                        cell(ActionLink(text) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(null, settings.id)
                        })
                    }
                }
            }
        }
    }
}
package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import javax.swing.*

class ParadoxDiagramSettingsConfigurable: BoundConfigurable(PlsBundle.message("settings.diagram"), "settings.language.pls.diagram"), SearchableConfigurable {
    override fun getId() = "settings.language.pls.diagram"
    
    override fun createPanel(): DialogPanel {
        return panel { 
            //TODO
        }
    }
}
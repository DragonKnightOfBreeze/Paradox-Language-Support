package icu.windea.pls.core.tool

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import java.awt.*
import javax.swing.*

class ParadoxModDependencyAddDialog(
    val project: Project,
    parentComponent: Component? = null
) : DialogWrapper(project, parentComponent, true, IdeModalityType.PROJECT) {
    var resultSettings: ParadoxModDependencySettingsState? = null
    
    init {
        title = PlsBundle.message("mod.dependency.add")
        init()
    }
    
    override fun createCenterPanel(): JComponent? {
        return panel { 
            //TODO
        }
    }
}

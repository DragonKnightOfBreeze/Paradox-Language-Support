package icu.windea.pls.core.settings

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*

class ParadoxModSettingsDialog(
    val project: Project,
    val modSettings: ParadoxModSettingsState,
): DialogWrapper(project, true) {
    //name (readonly)
    //version (readonly) supportedVersion? (readonly)
    //comment
    
    //game type (combobox)
    //game directory (filepath text field)
    
    //mod dependencies (foldable group)
    //  mod dependencies table
    //  actions: add (select mod path & import from file), remove, move up, move down, edit
    //  columns: order (int text field), icon (thumbnail), name (readonly), version (readonly), supportedVersion (readonly)
    //  when add or edit a column: show edit dialog (+ mod path)
    
    override fun createCenterPanel() = panel { 
        //TODO
    }
}
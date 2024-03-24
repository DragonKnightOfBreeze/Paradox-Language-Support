package icu.windea.pls.core.settings

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.core.util.*
import javax.swing.*

class ParadoxDefinitionTypeBindingsInCallHierarchyDialog(
    val list: MutableList<Entry<String, String>>
) : DialogWrapper(null, null, false, IdeModalityType.IDE) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }
    
    init {
        title = PlsBundle.message("settings.hierarchy.definitionTypeBindings.title")
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                val keyName = PlsBundle.message("settings.hierarchy.configureDefinitionTypeBindings.key")
                val valueName = PlsBundle.message("settings.hierarchy.definitionTypeBindings.value")
                cell(EntryListTableModel.createStringMapPanel(resultList, keyName, valueName)).align(Align.FILL)
            }.resizableRow()
            row {
                comment(PlsBundle.message("settings.hierarchy.configureDefinitionTypeBindings.comment.1"))
            }
            row {
                comment(PlsBundle.message("ui.comment.definitionTypeExpression"))
            }
        }
    }
}

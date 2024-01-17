package icu.windea.pls.core.settings

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxGameDirectoriesDialog(
    val list: MutableList<Entry<String, String>>
) : DialogWrapper(null, null, false, IdeModalityType.IDE) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }
    
    init {
        title = PlsBundle.message("settings.general.configureDefaultGameDirectories.title")
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                val keyName = PlsBundle.message("settings.general.configureDefaultGameDirectories.key")
                val valueName = PlsBundle.message("settings.general.configureDefaultGameDirectories.value")
                val keyGetter = { s: String -> ParadoxGameType.resolve(s)?.title.orEmpty() }
                cell(EntryListTableModel.createStringMapPanel(resultList, keyName, valueName, keyGetter = keyGetter, keySetter = null) {
                    it.disableAddAction().disableRemoveAction()
                    it.setVisibleRowCount(ParadoxGameType.values.size)
                }).align(Align.FILL)
            }.resizableRow()
        }
    }
}

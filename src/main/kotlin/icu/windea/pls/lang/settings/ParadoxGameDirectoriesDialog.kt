package icu.windea.pls.core.settings

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxGameDirectoriesDialog(
    val list: MutableList<Entry<String, String>>
) : DialogWrapper(null, null, false, IdeModalityType.IDE) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }
    
    val graph = PropertyGraph()
    val properties = list.associateBy({ it.key }, { graph.property(it.value) })
    
    init {
        title = PlsBundle.message("settings.general.configureDefaultGameDirectories.title")
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            properties.forEach f@{ (gameTypeId, gameDirectoryProperty) ->
                val gameType = ParadoxGameType.resolve(gameTypeId) ?: return@f
                val gameDirectory by gameDirectoryProperty
                row {
                    //gameDirectory
                    label(gameType.title + ":").widthGroup("left")
                    val descriptor = ParadoxDirectoryDescriptor()
                        .withTitle(PlsBundle.message("gameDirectory.title"))
                        .asBrowseFolderDescriptor()
                        .apply { putUserData(PlsDataKeys.gameType, gameType) }
                    textFieldWithBrowseButton(null, null, descriptor) { it.path }
                        .bindText(gameDirectoryProperty)
                        .columns(36)
                        .align(Align.FILL)
                        .validationOnApply { ParadoxGameHandler.validateGameDirectory(this, gameType, gameDirectory) }
                }
            }
            val quickGameDirectories = ParadoxGameType.values.associateBy({ it.id }, { ParadoxGameHandler.getQuickGameDirectory(it) })
            row {
                link(PlsBundle.message("gameDirectory.quickSelectAll")) {
                    properties.forEach f@{ (gameTypeId, gameDirectoryProperty) ->
                        var gameDirectory by gameDirectoryProperty
                        val quickGameDirectory = quickGameDirectories[gameTypeId]
                        if(gameDirectory.isNotNullOrEmpty()) return@f
                        gameDirectory = quickGameDirectory ?: return@f
                    }
                }.enabled(quickGameDirectories.isNotEmpty())
            }
        }
    }
    
    override fun doOKAction() {
        super.doOKAction()
        doOk()
    }
    
    private fun doOk() {
        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
    }
}

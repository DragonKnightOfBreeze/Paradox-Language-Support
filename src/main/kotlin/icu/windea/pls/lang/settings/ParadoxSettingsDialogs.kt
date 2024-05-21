package icu.windea.pls.lang.settings

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxGameType.*
import javax.swing.*

class ParadoxGameDirectoriesDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }
    
    val graph = PropertyGraph()
    val properties = list.associateBy({ it.key }, { graph.property(it.value) })
    
    init {
        title = PlsBundle.message("settings.general.defaultGameDirectories.dialog.title")
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            properties.forEach f@{ (gameTypeId, gameDirectoryProperty) ->
                val gameType = ParadoxGameType.resolveById(gameTypeId) ?: return@f
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
            val quickGameDirectories = entries.associateBy({ it.id }, { ParadoxGameHandler.getQuickGameDirectory(it) })
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
        
        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
    }
}

class ParadoxDefinitionTypeBindingsInCallHierarchyDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }
    
    init {
        title = PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.title")
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                val keyName = PlsBundle.message("settings.configure.definitionTypeBindings.dialog.key")
                val valueName = PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.value")
                cell(EntryListTableModel.createStringMapPanel(resultList, keyName, valueName)).align(Align.FILL)
            }.resizableRow()
            row {
                comment(PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.1"))
            }
            row {
                comment(PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.2"))
            }
        }
    }
}

class ParadoxClauseTemplateSettingsDialog: DialogWrapper(null) {
    init {
        title = PlsBundle.message("settings.completion.clauseTemplate.dialog.title")
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val settings = getSettings().completion.clauseTemplate
        return panel {
            //maxExpressionCountInOneLine
            row {
                label(PlsBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine")).applyToComponent {
                    toolTipText = PlsBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine.tooltip")
                }
                intTextField(0..10).bindIntText(settings::maxMemberCountInOneLine)
            }
        }
    }
    
    
}
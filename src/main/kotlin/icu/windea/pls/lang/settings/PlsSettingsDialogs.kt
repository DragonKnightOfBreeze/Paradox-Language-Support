package icu.windea.pls.lang.settings

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import javax.swing.*

@Suppress("UnstableApiUsage")
class DefaultGameDirectoriesDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
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
                val gameType = ParadoxGameType.resolve(gameTypeId) ?: return@f
                val gameDirectory by gameDirectoryProperty
                row {
                    //gameDirectory
                    label(gameType.title + ":").widthGroup("left")
                    val descriptor = ParadoxDirectoryDescriptor()
                        .withTitle(PlsBundle.message("gameDirectory.title"))
                        .asBrowseFolderDescriptor()
                        .apply { putUserData(PlsDataKeys.gameType, gameType) }
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(gameDirectoryProperty)
                        .columns(36)
                        .align(Align.FILL)
                        .validationOnApply { ParadoxCoreManager.validateGameDirectory(this, gameType, gameDirectory) }
                }
            }

            row {
                link(PlsBundle.message("gameDirectory.quickSelectAll")) {
                    properties.forEach f@{ (gameTypeId, gameDirectoryProperty) ->
                        val gameType = ParadoxGameType.resolve(gameTypeId) ?: return@f
                        val quickGameDirectory = ParadoxCoreManager.getQuickGameDirectory(gameType)?.orNull() ?: return@f
                        gameDirectoryProperty.set(quickGameDirectory)
                    }
                }
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
    }
}

class DefinitionTypeBindingsInCallHierarchyDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
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

class ClauseTemplateSettingsDialog : DialogWrapper(null) {
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
                intTextField(1..10).bindIntText(settings::maxMemberCountInOneLine)
            }
        }
    }
}

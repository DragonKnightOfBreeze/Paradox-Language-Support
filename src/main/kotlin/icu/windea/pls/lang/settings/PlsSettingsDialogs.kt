package icu.windea.pls.lang.settings

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.ui.*
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
                row {
                    //gameDirectory
                    label(gameType.title + ":").widthGroup("left")
                    val descriptor = FileChooserDescriptorFactory.singleDir()
                        .withTitle(PlsBundle.message("gameDirectory.title"))
                        .apply { putUserData(PlsDataKeys.gameType, gameType) }
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(gameDirectoryProperty)
                        .columns(COLUMNS_LARGE)
                        .align(Align.FILL)
                        .resizableColumn()
                        .validationOnApply { ParadoxCoreManager.validateGameDirectory(this, gameType, gameDirectoryProperty.get()) }
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
        val newValues = properties.map { it.value.get() }
        if(resultList.map { it.value } == newValues) return super.doOKAction() //unchanged

        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
        super.doOKAction()
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
                comment(PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.1"), MAX_LINE_LENGTH_WORD_WRAP)
            }
            row {
                comment(PlsBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.2"), MAX_LINE_LENGTH_WORD_WRAP)
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
        val settings = PlsFacade.getSettings().completion.clauseTemplate
        return panel {
            //maxExpressionCountInOneLine
            row {
                label(PlsBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine")).applyToComponent {
                    toolTipText = PlsBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine.tip")
                }
                intTextField(1..10).bindIntText(settings::maxMemberCountInOneLine)
            }
        }
    }
}

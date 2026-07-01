package icu.windea.pls.lang.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.core.ui.EntryListTableModel
import icu.windea.pls.core.util.Entry
import icu.windea.pls.lang.actions.ChronicleDataKeys
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.model.ParadoxGameType
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class DefaultGameDirectoriesDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }

    val graph = PropertyGraph()
    val properties = list.associateBy({ it.key }, { graph.property(it.value) })

    init {
        title = ChronicleBundle.message("settings.general.defaultGameDirectories.dialog.title")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            for ((gameTypeId, gameDirectoryProperty) in properties) {
                val gameType = ParadoxGameType.getSpecific(gameTypeId) ?: continue
                val gameTypeProperty = AtomicProperty(gameType)
                row {
                    // gameDirectory
                    label(gameType.title + ":").widthGroup("left")
                    val descriptor = FileChooserDescriptorFactory.singleDir()
                        .withTitle(ChronicleBundle.message("gameDirectory.title"))
                        .apply { putUserData(ChronicleDataKeys.gameTypeProperty, gameTypeProperty) }
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(gameDirectoryProperty)
                        .columns(COLUMNS_LARGE)
                        .align(Align.FILL)
                        .resizableColumn()
                        .validationOnApply { ParadoxGameManager.validateGameDirectory(this, gameType, gameDirectoryProperty.get()) }
                }
            }

            row {
                link(ChronicleBundle.message("gameDirectory.quickSelectAll")) {
                    for ((gameTypeId, gameDirectoryProperty) in properties) {
                        val gameType = ParadoxGameType.getSpecific(gameTypeId) ?: continue
                        val quickGameDirectory = ParadoxGameManager.getQuickGameDirectory(gameType)?.orNull() ?: continue
                        gameDirectoryProperty.set(quickGameDirectory)
                    }
                }
            }
        }
    }

    override fun doOKAction() {
        val newValues = properties.map { it.value.get() }
        if (resultList.map { it.value } == newValues) return super.doOKAction() // unchanged

        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
        super.doOKAction()
    }

    override fun getDimensionServiceKey() = "Pls.DefaultGameDirectoriesDialog"
}

class DefinitionTypeBindingsInCallHierarchyDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }

    init {
        title = ChronicleBundle.message("settings.hierarchy.definitionTypeBindings.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                val keyName = ChronicleBundle.message("settings.configure.definitionTypeBindings.dialog.key")
                val valueName = ChronicleBundle.message("settings.hierarchy.definitionTypeBindings.dialog.value")
                cell(EntryListTableModel.createStringMapPanel(resultList, keyName, valueName)).align(Align.FILL)
            }.resizableRow() // 占据额外的垂直空间
            row {
                comment(ChronicleBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.1"), MAX_LINE_LENGTH_WORD_WRAP)
            }
            row {
                comment(ChronicleBundle.message("settings.hierarchy.definitionTypeBindings.dialog.comment.2"), MAX_LINE_LENGTH_WORD_WRAP)
            }
        }
    }

    override fun getDimensionServiceKey() = "Pls.DefinitionTypeBindingsInCallHierarchyDialog"
}

class ClauseTemplateSettingsDialog : DialogWrapper(null) {
    init {
        title = ChronicleBundle.message("settings.completion.clauseTemplate.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val settings = PlsSettings.getInstance().state.completion.clauseTemplate
        return panel {
            // maxExpressionCountInOneLine
            row {
                label(ChronicleBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine"))
                intTextField(1..10).bindIntText(settings::maxMemberCountInOneLine)
                contextHelp(ChronicleBundle.message("settings.completion.clauseTemplate.dialog.maxMemberCountInOneLine.tip"))
            }
        }
    }

    override fun getDimensionServiceKey() = "Pls.ClauseTemplateSettingsDialog"
}

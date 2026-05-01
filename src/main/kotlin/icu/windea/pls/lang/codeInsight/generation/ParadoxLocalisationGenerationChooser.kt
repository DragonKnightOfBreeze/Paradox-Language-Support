package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.generation.MemberChooserObject
import com.intellij.icons.AllIcons
import com.intellij.ide.util.MemberChooser
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.settings.PlsSettingsStrategies.*
import icu.windea.pls.lang.ui.localeComboBox
import icu.windea.pls.lang.util.ParadoxLocaleManager
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

class ParadoxLocalisationGenerationChooser(
    elements: Array<out ParadoxLocalisationGenerationElement.Item>,
    project: Project,
) : MemberChooser<ParadoxLocalisationGenerationElement.Item>(elements, true, true, project) {
    // NOTE 2.1.8 这里不能直接通过构造参数传入上下文对象
    val context get() = ParadoxLocalisationGenerationManager.currentContext.get()

    override fun isContainerNode(key: MemberChooserObject): Boolean {
        return key !is ParadoxLocalisationGenerationElement.Item
    }

    override fun getAllContainersNodeName(): String {
        return PlsBundle.message("generation.localisation.allContainersNodeName")
    }

    override fun getShowContainersAction(): ShowContainersAction {
        return ShowContainersAction(PlsBundle.lazyMessage("generation.localisation.showContainers"), AllIcons.Actions.GroupBy)
    }

    override fun createSouthPanel(): JComponent {
        // NOTE 2.1.8 hide unexpected bottom left UI components
        myOptionControls = emptyArray()

        return panel {
            row {
                panel { configureSouthOptionsPanel() }
            }
            separator()
            row {
                cell(super.createSouthPanel())
            }
        }
    }

    private fun Panel.configureSouthOptionsPanel() {
        val settings = PlsSettings.getInstance().state.generation

        // moveIntoLocalisationGroups
        row {
            checkBox(PlsBundle.message("settings.generation.moveIntoLocalisationGroups"))
                .bindSelected(settings::moveInfoLocalisationGroups.toAtomicProperty())
        }
        // newLineBetweenLocalisationGroups
        row {
            checkBox(PlsBundle.message("settings.generation.newLineBetweenLocalisationGroups"))
                .bindSelected(settings::newLineBetweenLocalisationGroups.toAtomicProperty())
        }
        // localisationStrategy
        row {
            val property = AtomicProperty(settings.localisationStrategy)
            label(PlsBundle.message("settings.generation.localisationStrategy"))
            comboBox(LocalisationGeneration.entries, textListCellRenderer { it?.text })
                .bindItem(settings::localisationStrategy.toAtomicProperty())
                .bindItem(property)
            textField().bindText(settings::localisationStrategyText.toAtomicProperty(""))
                .visibleIf(property.transform { it == LocalisationGeneration.SpecificText })
            localeComboBox(withAuto = true).bindItem(settings::localisationStrategyLocale.toAtomicProperty(ParadoxLocaleManager.ID_AUTO))
                .visibleIf(property.transform { it == LocalisationGeneration.FromLocale })
        }
    }

    override fun createActions(): Array<out Action> {
        val actions = mutableListOf<Action>()
        actions += okAction
        actions += createSelectAllAction()
        actions += createSelectMissingAction()
        if (context.fromInspection) actions += createSelectMissingAndCheckedAction()
        if (myAllowEmptySelection) actions += createSelectNoneAction()
        actions += cancelAction
        if (helpId != null) actions += helpAction
        return actions.toTypedArray()
    }

    private fun createSelectAllAction(): SelectAction {
        return SelectAction(PlsBundle.message("generation.localisation.select.all")) {
            selectElements(myElements)
        }
    }

    private fun createSelectMissingAction(): SelectAction {
        return SelectAction(PlsBundle.message("generation.localisation.select.missing")) {
            selectElements(myElements.filter { it.info.missing }.toTypedArray())
        }
    }

    private fun createSelectMissingAndCheckedAction(): SelectAction {
        return SelectAction(PlsBundle.message("generation.localisation.select.missingAndChecked")) {
            selectElements(myElements.filter { it.info.missing && it.info.check }.toTypedArray())
        }
    }

    private fun createSelectNoneAction(): SelectAction {
        return SelectAction(PlsBundle.message("generation.localisation.select.none")) {
            myTree.clearSelection()
            doOKAction()
        }
    }

    private class SelectAction(actionName: String, private val action: (ActionEvent) -> Unit) : AbstractAction(actionName) {
        override fun actionPerformed(e: ActionEvent) {
            action(e)
        }
    }
}

package icu.windea.pls.lang.ui.clause

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TableViewSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.letIf
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.ChangeEvent

//com.intellij.refactoring.extractInterface.ExtractInterfaceDialog
//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase

/**
 * 在插入子句内联模版之前显示，用于对要插入的属性进行选取、排序和重复。
 */
class ExpandClauseTemplateDialog(
    val project: Project,
    val editor: Editor,
    val context: ElementsContext
) : DialogWrapper(project) {
    var elementsTableModel: ElementsTableModel

    val multipleGroup = context.descriptorsInfoList.size > 1

    init {
        title = PlsBundle.message("ui.dialog.expandClauseTemplate.title")
        elementsTableModel = createElementsInfoModel()
        init()
    }

    private fun createElementsInfoModel(): ElementsTableModel {
        return ElementsTableModel(context)
    }

    override fun createNorthPanel() = panel {
        //(textField) propertyName
        row {
            val propertyName = context.propertyName
                ?: PlsBundle.message("ui.dialog.expandClauseTemplate.propertyName.none")
            textField()
                .text(propertyName)
                .label(PlsBundle.message("ui.dialog.expandClauseTemplate.propertyName"), LabelPosition.LEFT)
                .align(Align.FILL)
                .columns(COLUMNS_LARGE)
                .enabled(false)
        }
        if (multipleGroup) {
            row {
                comment(PlsBundle.message("ui.dialog.expandClauseTemplate.comment.1"))
            }
        }
    }.withPreferredWidth(600)

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        with(panel) {
            val contentPanel = JPanel(BorderLayout())
            with(contentPanel) {
                val elementsPanel = createElementsPanel()
                add(SeparatorFactory.createSeparator(PlsBundle.message("ui.dialog.expandClauseTemplate.elementsToInsert"), elementsPanel), BorderLayout.NORTH)
                add(elementsPanel, BorderLayout.CENTER)
            }
            add(contentPanel, BorderLayout.CENTER)
        }
        panel.border = JBUI.Borders.emptyTop(5)
        return panel
    }

    private fun createElementsPanel(): JPanel {
        val tableView = object : TableView<ElementDescriptor>(elementsTableModel) {
            override fun editingStopped(e: ChangeEvent?) {
                super.editingStopped(e)
                repaint() // to update disabled cells background
            }
        }
        tableView.setShowGrid(false)
        tableView.rowSelectionAllowed = true
        tableView.columnSelectionAllowed = true
        tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        //快速搜索
        val speedSearch = object : TableViewSpeedSearch<ElementDescriptor>(tableView, null) {
            override fun getItemText(element: ElementDescriptor): String {
                return element.name
            }
        }
        speedSearch.setupListeners()
        val listTable = ElementsListTable(tableView, elementsTableModel, disposable, context)
        val table = listTable.table
        //add, remove, move up, move down, duplicate
        val panel = ToolbarDecorator.createDecorator(table)
            .addExtraAction(ElementsToolbarActions.DuplicateAction(listTable))
            .letIf(multipleGroup) {
                it.addExtraAction(ElementsToolbarActions.SwitchToPrevAction(listTable))
                it.addExtraAction(ElementsToolbarActions.SwitchToNextAction(listTable))
            }
            .createPanel()
        panel.preferredSize = Dimension(panel.preferredSize.width, 540)
        return panel
    }
}


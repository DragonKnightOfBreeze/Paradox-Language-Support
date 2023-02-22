package icu.windea.pls.core.ui

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.awt.*
import javax.swing.*
import javax.swing.event.*

//com.intellij.refactoring.extractInterface.ExtractInterfaceDialog
//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase

/**
 * 在插入子句内联模版之前显示，用于对要插入的属性进行选取、排序和重复。
 */
@Suppress("DialogTitleCapitalization")
class ExpandClauseTemplateDialog(
    val project: Project,
    val editor: Editor,
    val context: ElementDescriptorsContext
) : DialogWithValidation(project) {
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
        if(multipleGroup) {
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
        object : TableViewSpeedSearch<ElementDescriptor>(tableView) {
            override fun getItemText(element: ElementDescriptor): String {
                return element.name
            }
        }
        val elementsList = ElementsListTable(tableView, elementsTableModel, disposable, context, this)
        //add, remove, move up, move down, duplicate
        val panel = ToolbarDecorator.createDecorator(elementsList.table)
            .addExtraAction(ElementsToolbarActions.DuplicateAction(elementsList))
            .letIf(multipleGroup) {
                it.addExtraAction(ElementsToolbarActions.SwitchToPrevAction(elementsList))
                it.addExtraAction(ElementsToolbarActions.SwitchToNextAction(elementsList))
            }
            .createPanel()
        panel.preferredSize = Dimension(panel.preferredSize.width, 540)
        return panel
    }
}


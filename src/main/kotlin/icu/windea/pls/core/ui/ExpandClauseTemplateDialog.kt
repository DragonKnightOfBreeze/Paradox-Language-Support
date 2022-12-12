package icu.windea.pls.core.ui

import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import com.intellij.util.ui.table.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import java.awt.*
import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*

//com.intellij.refactoring.extractInterface.ExtractInterfaceDialog
//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase

/**
 * 在插入子句内联模版之前显示，用于对要插入的属性进行选取、排序和重复。
 */
@Suppress("DialogTitleCapitalization")
class ExpandClauseTemplateDialog(
	val project: Project,
	val editor: Editor,
	val propertyName: String?,
	val descriptors: List<ElementDescriptor>
) : DialogWithValidation(project) {
	val context = ElementDescriptorContext(project, editor, descriptors)
	
	lateinit var elementsList: ElementsListTable
	lateinit var elementsTable: TableView<ElementDescriptor>
	var elementsTableModel: ElementTableModel
	
	init {
		title = PlsBundle.message("ui.dialog.expandClauseTemplate.title")
		elementsTableModel = createElementsInfoModel()
		init()
	}
	
	private fun createElementsInfoModel(): ElementTableModel {
		return ElementTableModel(context)
	}
	
	override fun createNorthPanel() = panel {
		//(textField) propertyName
		row {
			val propertyName = propertyName ?: PlsBundle.message("ui.dialog.expandClauseTemplate.propertyName.none")
			textField()
				.text(propertyName).enabled(false).horizontalAlign(HorizontalAlign.FILL)
				.label(PlsBundle.message("ui.dialog.expandClauseTemplate.propertyName"), LabelPosition.LEFT)
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
		elementsTable = object : TableView<ElementDescriptor>(elementsTableModel) {
			override fun editingStopped(e: ChangeEvent?) {
				super.editingStopped(e)
				repaint() // to update disabled cells background
			}
		}
		elementsTable.setShowGrid(false)
		elementsTable.cellSelectionEnabled = true
		elementsTable.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		elementsTable.selectionModel.setSelectionInterval(0, 0)
		elementsTable.surrendersFocusOnKeystroke = true
		
		elementsList = createElementsListTable()
		//add, remove, move up, move down, duplicate
		val buttonsPanel = ToolbarDecorator.createDecorator(elementsList.table)
			.addExtraAction(DuplicateAction(elementsList))
			.createPanel()
		buttonsPanel.preferredSize = Dimension(buttonsPanel.preferredSize.width, 540)
		return buttonsPanel
	}
	
	private fun createElementsListTable(): ElementsListTable {
		return ElementsListTable(elementsTable, elementsTableModel, disposable, context, this)
	}
	
	class DuplicateAction(
		private val elementsList: ElementsListTable
	): AnAction(PlsBundle.message("ui.dialog.expandClauseTemplate.actions.duplicate"), null, PlsIcons.Actions.DuplicateDescriptor) {
		init {
			shortcutSet = CustomShortcutSet.fromString("alt C")
		}
		
		override fun actionPerformed(e: AnActionEvent) {
			val selectedIndices = elementsList.table.selectionModel.selectedIndices
			val elementsTable = elementsList.elementsTable
			for(row in selectedIndices.reversed()) {
				elementsTable.listTableModel.insertRow(row + 1, elementsTable.getRow(row).copyDescriptor())
			}
		}
		
		override fun getActionUpdateThread() = ActionUpdateThread.EDT
	}
}

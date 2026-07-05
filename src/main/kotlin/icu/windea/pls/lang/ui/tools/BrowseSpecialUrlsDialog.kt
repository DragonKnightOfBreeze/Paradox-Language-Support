package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.PopupHandler
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.registerDoubleClickListener
import icu.windea.pls.ep.tools.SpecialUrlProvider
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.ListSelectionModel

/**
 * @see SpecialUrlProvider
 */
class BrowseSpecialUrlsDialog(
    project: Project?,
    file: VirtualFile? = null,
    gameType: ParadoxGameType? = null,
) : DialogWrapper(project, false, IdeModalityType.MODELESS) { // NOTE modeless dialog
    // com.intellij.diagnostic.specialPaths.BrowseSpecialPathsDialog

    private val defaultSelectedFile = file
    private val selectedFile get() = defaultSelectedFile

    private val defaultSelectedGameType = ParadoxAnalysisManager.getSelectedGameType(defaultSelectedFile, gameType)
    private val selectedGameTypeProperty = AtomicProperty(defaultSelectedGameType)
    private val selectedGameType get() = selectedGameTypeProperty.get()

    private val providers = SpecialUrlProvider.EP_NAME.extensionList

    private val tableModel = ListTableModel(
        arrayOf(
            object : ColumnInfo<SpecialUrlProvider, String>(ChronicleBundle.message("dialog.table.column.description")) {
                override fun valueOf(item: SpecialUrlProvider) = item.text
            },
            object : ColumnInfo<SpecialUrlProvider, String>(ChronicleBundle.message("dialog.table.column.url")) {
                override fun valueOf(item: SpecialUrlProvider) = item.getUrl(file, selectedGameType) ?: ""
            },
        ),
        providers
    )
    private val table = JBTable(tableModel).apply {
        rowSelectionAllowed = true
        columnSelectionAllowed = false
        intercellSpacing = Dimension(0, 0)
        val visibleRowCount = providers.size.coerceIn(1, preferredVisibleRowCount)
        preferredScrollableViewportSize = Dimension(0, rowHeight * visibleRowCount)
        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // 调整列宽
        for ((i, width) in columnPreferredWidths.withIndex()) {
            columnModel.getColumn(i).preferredWidth = JBUI.scale(width)
        }

        // 快速搜索
        TableSpeedSearch.installOn(this).apply { comparator = SpeedSearchComparator(false) }

        // 右键打开提示菜单
        PopupHandler.installPopupMenu(this, createPopupActions(), POPUP_PLACE)

        // 双击打开
        registerDoubleClickListener { open() }
    }

    init {
        title = ChronicleBundle.message("dialog.title.browseSpecialUrls")
        setOKButtonText(ChronicleBundle.message("action.copyAll"))
        setCancelButtonText(ChronicleBundle.message("action.close"))
        init()
        pack()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row(ChronicleBundle.message("dialog.field.selectedGameType")) {
                comboBox(ParadoxGameType.getAllSpecific(), textListCellRenderer { it?.title })
                    .bindItem(selectedGameTypeProperty)
                    .applyToComponent { addActionListener { tableModel.fireTableDataChanged() } }
                button(ChronicleBundle.message("reset")) { selectedGameTypeProperty.set(defaultSelectedGameType) }
                    .align(AlignX.RIGHT)
            }

            row {
                val scrollPane = JBScrollPane(table)
                cell(scrollPane).align(Align.FILL)
            }.resizableRow()
        }.withPreferredSize(preferredDialogWidth, preferredDialogHeight)
    }

    override fun createActions(): Array<out Action?> {
        // copyAll + copy + open + close
        val copyAction = object : AbstractAction(ChronicleBundle.message("action.copy")) {
            override fun actionPerformed(e: ActionEvent?) {
                copy()
            }
        }
        val openAction = object : AbstractAction(ChronicleBundle.message("action.open")) {
            override fun actionPerformed(e: ActionEvent?) {
                open()
            }
        }
        return arrayOf(okAction, copyAction, openAction, cancelAction)
    }

    override fun doOKAction() = copyAll()

    private fun getProvider(): SpecialUrlProvider? {
        val selectedRow = table.selectedRow
        if (selectedRow < 0) return null
        return tableModel.getItem(selectedRow)
    }

    private fun createPopupActions(): ActionGroup {
        val actionGroup = DefaultActionGroup()
        actionGroup.addAction(object : AnAction(ChronicleBundle.message("dialog.table.popup.action.CopyUrl.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                copy()
            }
        })
        actionGroup.addAction(object : AnAction(ChronicleBundle.message("dialog.table.popup.action.OpenUrl.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                open()
            }
        })
        return actionGroup
    }

    private fun copyAll() {
        val tuples = providers.mapNotNull { provider ->
            val url = provider.getUrl(selectedFile, selectedGameType) ?: return@mapNotNull null
            val name = provider.text
            name to url
        }
        if (tuples.isEmpty()) return
        val maxNameLength = tuples.maxOfOrNull { (name) -> name.length } ?: 0
        val text = buildString {
            for ((name, url) in tuples) {
                append(name.padEnd(maxNameLength, ' ')).append("  ").append(url).appendLine()
            }
        }
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }

    private fun copy() {
        val provider = getProvider() ?: return
        val url = provider.getUrl(selectedFile, selectedGameType) ?: return
        SpecialUrlService.getInstance().copyUrl(url)
    }

    private fun open() {
        val provider = getProvider() ?: return
        val url = provider.getUrl(selectedFile, selectedGameType) ?: return
        SpecialUrlService.getInstance().openUrl(url)
    }

    companion object {
        private const val POPUP_PLACE = "BrowseSpecialUrlsDialogPopup"
        private const val preferredVisibleRowCount = 10
        private val columnPreferredWidths = intArrayOf(270, 530) // fit, verified
        private val preferredDialogWidth = columnPreferredWidths.sum()
        private const val preferredDialogHeight = 400 // larger, just okay
    }
}

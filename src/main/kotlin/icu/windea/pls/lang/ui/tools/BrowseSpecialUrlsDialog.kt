package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
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
import com.intellij.util.ui.ListTableModel
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.registerDoubleClickListener
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.ep.tools.SpecialUrlProvider
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType
import java.awt.Dimension
import java.awt.datatransfer.StringSelection

/**
 * @see SpecialUrlProvider
 */
class BrowseSpecialUrlsDialog(
    project: Project?,
    file: VirtualFile? = null,
    gameType: ParadoxGameType? = null,
) : DialogWrapper(project, false, IdeModalityType.MODELESS) { // NOTE modeless dialog
    // com.intellij.diagnostic.specialPaths.BrowseSpecialPathsDialog

    private val selectedFile = file
    private var selectedGameType = ParadoxAnalysisManager.getSelectedGameType(file, gameType)

    private val providers = SpecialUrlProvider.EP_NAME.extensionList

    private val tableModel = ListTableModel(
        arrayOf(
            object : ColumnInfo<SpecialUrlProvider, String>(PlsBundle.message("dialog.table.column.description")) {
                override fun valueOf(item: SpecialUrlProvider) = item.text
            },
            object : ColumnInfo<SpecialUrlProvider, String>(PlsBundle.message("dialog.table.column.url")) {
                override fun valueOf(item: SpecialUrlProvider) = item.getUrl(selectedFile, selectedGameType) ?: ""
            },
        ),
        providers
    )
    private val table = JBTable(tableModel).apply {
        setShowGrid(false)
        rowSelectionAllowed = true
        columnSelectionAllowed = false
        intercellSpacing = Dimension(0, 0)
        autoResizeMode = javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN
        val visibleRowCount = providers.size.coerceIn(1, PREFERRED_VISIBLE_ROW_COUNT)
        preferredScrollableViewportSize = Dimension(0, rowHeight * visibleRowCount)

        // 快速搜索
        TableSpeedSearch.installOn(this).apply { comparator = SpeedSearchComparator(false) }

        // 右键打开提示菜单
        PopupHandler.installPopupMenu(this, createPopupActions(), POPUP_PLACE)

        // 双击打开路径
        registerDoubleClickListener { copy() }
    }

    init {
        title = PlsBundle.message("dialog.title.browseSpecialUrls")
        setOKButtonText(PlsBundle.message("action.copyAll"))
        setCancelButtonText(PlsBundle.message("action.close"))
        init()
        pack()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row(PlsBundle.message("dialog.field.selectedGameType")) {
                comboBox(ParadoxGameType.getAll(), textListCellRenderer { it?.title })
                    .bindItem(::selectedGameType.toAtomicProperty())
                    .applyToComponent { addActionListener { tableModel.fireTableDataChanged() } }
            }

            row {
                val scrollPane = JBScrollPane(table)
                cell(scrollPane).align(Align.FILL)
            }.resizableRow()
        }
    }

    override fun doOKAction() = copyAll()

    override fun getDimensionServiceKey() = "Pls.BrowseSpecialUrlsDialog"

    private fun getProvider(): SpecialUrlProvider? {
        val selectedRow = table.selectedRow
        if (selectedRow < 0) return null
        return tableModel.getItem(selectedRow)
    }

    private fun createPopupActions(): ActionGroup {
        val actionGroup = DefaultActionGroup()
        actionGroup.addAction(object : AnAction(PlsBundle.message("dialog.table.popup.action.copyUrl")) {
            override fun actionPerformed(e: AnActionEvent) {
                val provider = getProvider() ?: return
                val url = provider.getUrl(selectedFile, selectedGameType) ?: return
                SpecialUrlService.getInstance().copyUrl(url)
            }
        })
        actionGroup.addAction(object : AnAction(PlsBundle.message("dialog.table.popup.action.openUrl")) {
            override fun actionPerformed(e: AnActionEvent) {
                val provider = getProvider() ?: return
                val url = provider.getUrl(selectedFile, selectedGameType) ?: return
                SpecialUrlService.getInstance().openUrl(url)
            }
        })
        return actionGroup
    }

    private fun copy() {
        val provider = getProvider() ?: return
        val url = provider.getUrl(selectedFile, selectedGameType) ?: return
        SpecialUrlService.getInstance().openUrl(url)
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

    companion object {
        private const val POPUP_PLACE = "BrowseSpecialUrlsDialogPopup"
        private const val PREFERRED_VISIBLE_ROW_COUNT = 10
    }
}

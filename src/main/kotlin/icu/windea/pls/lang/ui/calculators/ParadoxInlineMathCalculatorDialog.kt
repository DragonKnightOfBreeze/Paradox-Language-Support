package icu.windea.pls.lang.ui.calculators

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.TextTransferable
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.calculators.ParadoxInlineMathCalculator
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.table.TableCellEditor

class ParadoxInlineMathCalculatorDialog(
    val project: Project,
    val element: ParadoxScriptInlineMath
) : DialogWrapper(project, true) {
    private val calculator = ParadoxInlineMathCalculator()
    private val argumentList = calculator.resolveArguments(element).values.toMutableList()
    private var initialized = false
    private var currentOutputText = ""
    private val resultTextArea by lazy {
        JTextArea().apply {
            minimumSize = Dimension(PREFERRED_TEXT_WIDTH, minimumSize.height)
            preferredSize = Dimension(PREFERRED_TEXT_WIDTH, preferredSize.height)
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isOpaque = false
        }
    }

    val result: String get() = currentOutputText

    init {
        title = PlsBundle.message("ui.dialog.calculator.inlineMath.title")
        setOKButtonText(PlsBundle.message("ui.dialog.calculator.inlineMath.action.copy"))
        setCancelButtonText(PlsBundle.message("ui.dialog.calculator.inlineMath.action.close"))
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val expressionText = getExpressionText(element)
        val inlineMathPrefix = "@[ "
        val inlineMathSuffix = " ]"
        val inlineMathText = inlineMathPrefix + expressionText + inlineMathSuffix
        val expressionDocument = EditorFactory.getInstance().createDocument(inlineMathText)
        val expressionField = EditorTextField(expressionDocument, project, ParadoxScriptFileType, true, true).apply {
            setPreferredWidth(PREFERRED_TEXT_WIDTH)
            addSettingsProvider { editor ->
                // 折叠前后缀，不显示
                editor.foldingModel.runBatchFoldingOperation {
                    editor.foldingModel.clearFoldRegions()
                    editor.foldingModel.addFoldRegion(0, inlineMathPrefix.length, "")?.apply { isExpanded = false }
                    editor.foldingModel.addFoldRegion(inlineMathText.length - inlineMathSuffix.length, inlineMathText.length, "")?.apply { isExpanded = false }
                }
            }
        }

        val tableModel = ArgumentsTableModel(argumentList)
        val table = JBTable(tableModel).apply {
            setShowGrid(false)
            rowSelectionAllowed = false
            columnSelectionAllowed = false
            intercellSpacing = Dimension(0, 0)
            val visibleRowCount = argumentList.size.coerceIn(1, PREFERRED_ARGUMENT_SIZE)
            preferredScrollableViewportSize = Dimension(0, rowHeight * visibleRowCount)
        }

        // 快速搜索
        TableSpeedSearch.installOn(table) { e ->
            val element = e as ParadoxInlineMathCalculator.Argument
            element.expression
        }

        val panel = panel {
            row(PlsBundle.message("ui.dialog.calculator.inlineMath.label.expression")) {
                cell(expressionField)
                    .align(Align.FILL)
            }

            row {
                val scrollPane = JBScrollPane(table)
                cell(scrollPane)
                    .align(Align.FILL)
            }.resizableRow()

            row(PlsBundle.message("ui.dialog.calculator.inlineMath.label.result")) {
                val scrollPane = JBScrollPane().apply { setViewportView(resultTextArea) }
                cell(scrollPane)
                    .align(Align.FILL)
            }
        }.withPreferredWidth(PREFERRED_DIALOG_WIDTH)

        // 实时计算
        tableModel.addTableModelListener { updateResultText() }
        updateResultText()

        return panel
    }

    override fun doOKAction() {
        CopyPasteManager.getInstance().setContents(TextTransferable(currentOutputText as CharSequence))
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return resultTextArea
    }

    private fun updateResultText() {
        val args = argumentList
            .mapNotNull { a ->
                val v = a.value.trim().orNull() ?: return@mapNotNull null
                a.expression to v
            }
            .toMap()

        val output = getOutput(args)
        resultTextArea.text = output
        currentOutputText = output
    }

    private fun getOutput(args: Map<String, String>): String {
        try {
            val result = calculator.calculate(element, args)
            initialized = true
            return result.resolveValue().toString()
        } catch (e: Throwable) {
            val message = e.message.orEmpty().ifEmpty { e::class.java.simpleName }
            if (!initialized && e is IllegalArgumentException && message.startsWith("Missing arguments:")) {
                // 存在缺失的传参，此时仍然显示为空字符串
                return ""
            }
            initialized = true
            return message
        }
    }

    private fun getExpressionText(element: ParadoxScriptInlineMath): String {
        val tokenText = element.tokenElement?.text?.trim().orEmpty()
        if (tokenText.startsWith("@[")) {
            val noPrefix = tokenText.removePrefix("@[")
            val noSuffix = noPrefix.removeSuffix("]")
            return noSuffix.trim()
        }
        return tokenText
    }

    override fun getDimensionServiceKey() = "Pls.ParadoxInlineMathCalculatorDialog"

    class ArgumentsTableModel(items: MutableList<ParadoxInlineMathCalculator.Argument>) : ListTableModel<ParadoxInlineMathCalculator.Argument>(
        arrayOf(
            object : ColumnInfo<ParadoxInlineMathCalculator.Argument, String>(PlsBundle.message("ui.dialog.calculator.inlineMath.table.column.expression")) {
                override fun valueOf(item: ParadoxInlineMathCalculator.Argument): String = item.expression
            },
            object : ColumnInfo<ParadoxInlineMathCalculator.Argument, String>(PlsBundle.message("ui.dialog.calculator.inlineMath.table.column.value")) {
                override fun isCellEditable(item: ParadoxInlineMathCalculator.Argument?): Boolean = true

                override fun valueOf(item: ParadoxInlineMathCalculator.Argument): String = item.value

                override fun setValue(item: ParadoxInlineMathCalculator.Argument, value: String?) {
                    item.value = value.orEmpty()
                }

                override fun getEditor(item: ParadoxInlineMathCalculator.Argument?): TableCellEditor {
                    return DefaultCellEditor(JBTextField())
                }
            },
            object : ColumnInfo<ParadoxInlineMathCalculator.Argument, String>(PlsBundle.message("ui.dialog.calculator.inlineMath.table.column.defaultValue")) {
                override fun valueOf(item: ParadoxInlineMathCalculator.Argument): String = item.defaultValue
            },
        ),
        items
    )

    companion object {
        private const val PREFERRED_DIALOG_WIDTH = 600
        private const val PREFERRED_TEXT_WIDTH = 200
        private const val PREFERRED_ARGUMENT_SIZE = 5
    }
}

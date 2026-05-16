package icu.windea.pls.lang.ui.script

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import com.intellij.ui.SpeedSearchComparator
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
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.JTextArea
import javax.swing.table.TableCellEditor

class ParadoxInlineMathEvaluatorDialog(
    private val project: Project,
    private val element: ParadoxScriptInlineMath
) : DialogWrapper(project, false, IdeModalityType.MODELESS) { // NOTE modeless dialog
    private val evaluator = ParadoxInlineMathEvaluator()
    private val argumentList = evaluator.resolveArguments(element).values.toMutableList()
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

    private val expressionField = run {
        val expressionText = getExpressionText(element)
        val inlineMathText = INLINE_MATH_PREFIX + expressionText + INLINE_MATH_SUFFIX
        val expressionDocument = EditorFactory.getInstance().createDocument(inlineMathText)
        EditorTextField(expressionDocument, project, ParadoxScriptFileType, true, true).apply {
            setPreferredWidth(PREFERRED_TEXT_WIDTH)
            addSettingsProvider { editor ->
                // 折叠前后缀，不显示
                editor.foldingModel.runBatchFoldingOperation {
                    editor.foldingModel.clearFoldRegions()
                    editor.foldingModel.addFoldRegion(0, INLINE_MATH_PREFIX.length, "")?.apply { isExpanded = false }
                    editor.foldingModel.addFoldRegion(inlineMathText.length - INLINE_MATH_SUFFIX.length, inlineMathText.length, "")?.apply { isExpanded = false }
                }
            }
        }
    }
    private val tableModel = ListTableModel(
        arrayOf(
            object : ColumnInfo<ParadoxInlineMathEvaluator.Argument, String>(PlsBundle.message("ui.dialog.evaluator.inlineMath.table.column.expression")) {
                override fun valueOf(item: ParadoxInlineMathEvaluator.Argument): String = item.expression
            },
            object : ColumnInfo<ParadoxInlineMathEvaluator.Argument, String>(PlsBundle.message("ui.dialog.evaluator.inlineMath.table.column.value")) {
                override fun isCellEditable(item: ParadoxInlineMathEvaluator.Argument?): Boolean = true

                override fun valueOf(item: ParadoxInlineMathEvaluator.Argument): String = item.value

                override fun setValue(item: ParadoxInlineMathEvaluator.Argument, value: String?) {
                    item.value = value.orEmpty()
                }

                override fun getEditor(item: ParadoxInlineMathEvaluator.Argument?): TableCellEditor {
                    return DefaultCellEditor(JBTextField())
                }
            },
            object : ColumnInfo<ParadoxInlineMathEvaluator.Argument, String>(PlsBundle.message("ui.dialog.evaluator.inlineMath.table.column.defaultValue")) {
                override fun valueOf(item: ParadoxInlineMathEvaluator.Argument): String = item.defaultValue
            },
        ),
        argumentList
    )
    private val table = JBTable(tableModel).apply {
        setShowGrid(false)
        rowSelectionAllowed = false
        columnSelectionAllowed = false
        intercellSpacing = Dimension(0, 0)
        val visibleRowCount = argumentList.size.coerceIn(1, PREFERRED_ARGUMENT_SIZE)
        preferredScrollableViewportSize = Dimension(0, rowHeight * visibleRowCount)

        // 快速搜索
        TableSpeedSearch.installOn(this) { e ->
            val element = e as ParadoxInlineMathEvaluator.Argument
            element.expression
        }.apply { comparator = SpeedSearchComparator(false) }
    }

    val result: String get() = currentOutputText

    init {
        title = PlsBundle.message("ui.dialog.evaluator.inlineMath.title")
        setOKButtonText(PlsBundle.message("action.copy"))
        setCancelButtonText(PlsBundle.message("action.close"))
        init()
        pack()
    }

    override fun createCenterPanel(): DialogPanel {
        val panel = panel {
            row(PlsBundle.message("ui.dialog.evaluator.inlineMath.label.expression")) {
                cell(expressionField).align(Align.FILL)
            }

            row {
                val scrollPane = JBScrollPane(table)
                cell(scrollPane).align(Align.FILL)
            }.resizableRow()

            row(PlsBundle.message("ui.dialog.evaluator.inlineMath.label.result")) {
                val scrollPane = JBScrollPane().apply { setViewportView(resultTextArea) }
                cell(scrollPane).align(Align.FILL)
            }
        }.withPreferredWidth(PREFERRED_DIALOG_WIDTH)

        // 实时求值
        tableModel.addTableModelListener { updateResultText() }
        updateResultText()

        return panel
    }

    override fun doOKAction() = copyResultText()

    override fun getPreferredFocusedComponent() = resultTextArea

    override fun getDimensionServiceKey() = "Pls.ParadoxInlineMathEvaluatorDialog"

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

    private fun copyResultText() {
        CopyPasteManager.getInstance().setContents(TextTransferable(currentOutputText as CharSequence))
    }

    private fun getOutput(args: Map<String, String>): String {
        try {
            val result = evaluator.evaluate(element, args)
            initialized = true
            return result.formatted()
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

    companion object {
        private const val INLINE_MATH_PREFIX = "@[ "
        private const val INLINE_MATH_SUFFIX = " ]"
        private const val PREFERRED_DIALOG_WIDTH = 600
        private const val PREFERRED_TEXT_WIDTH = 200
        private const val PREFERRED_ARGUMENT_SIZE = 5
    }
}

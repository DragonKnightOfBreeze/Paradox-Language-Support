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
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.TextTransferable
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.errorDetails
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.model.ParadoxInlineMathArgument
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.JTextArea
import javax.swing.table.TableCellEditor

class ParadoxInlineMathEvaluatorDialog(
    project: Project,
    element: ParadoxScriptInlineMath,
) : DialogWrapper(project, false, IdeModalityType.MODELESS) { // NOTE modeless dialog
    private val elementPointer = element.createPointer(project)
    private val element: ParadoxScriptInlineMath? get() = elementPointer.element

    private val evaluator = ParadoxInlineMathExpressionEvaluator()
    private val argumentList = evaluator.resolveArguments(element).values.toMutableList()
    private var isInitialized = false
    private var isValid = element.isValid

    private var currentResult: MathResult? = null
    private var currentResultText: String? = null

    @Suppress("unused")
    val result: MathResult? get() = currentResult
    @Suppress("unused")
    val resultText: String? get() = currentResultText

    private val resultTextArea by lazy {
        JTextArea().apply {
            minimumSize = Dimension(preferredTextWidth, minimumSize.height)
            preferredSize = Dimension(preferredTextWidth, preferredSize.height)
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isOpaque = false
        }
    }
    private val expressionField = run {
        val expressionText = getExpressionText(element)
        val inlineMathText = inlineMathPrefix + expressionText + inlineMathSuffix
        val expressionDocument = EditorFactory.getInstance().createDocument(inlineMathText)
        EditorTextField(expressionDocument, project, ParadoxScriptFileType, true, true).apply {
            setPreferredWidth(preferredTextWidth)
            addSettingsProvider { editor ->
                // 折叠前后缀，不显示
                editor.foldingModel.runBatchFoldingOperation {
                    editor.foldingModel.clearFoldRegions()
                    editor.foldingModel.addFoldRegion(0, inlineMathPrefix.length, "")?.apply { isExpanded = false }
                    editor.foldingModel.addFoldRegion(inlineMathText.length - inlineMathSuffix.length, inlineMathText.length, "")?.apply { isExpanded = false }
                }
            }
        }
    }
    private val tableModel = ListTableModel(
        arrayOf(
            object : ColumnInfo<ParadoxInlineMathArgument, String>(ChronicleBundle.message("ui.dialog.evaluator.inlineMath.table.column.expression")) {
                override fun valueOf(item: ParadoxInlineMathArgument): String = item.expression
            },
            object : ColumnInfo<ParadoxInlineMathArgument, String>(ChronicleBundle.message("ui.dialog.evaluator.inlineMath.table.column.value")) {
                override fun isCellEditable(item: ParadoxInlineMathArgument?): Boolean = true

                override fun valueOf(item: ParadoxInlineMathArgument): String = item.value

                override fun setValue(item: ParadoxInlineMathArgument, value: String?) {
                    item.value = value.orEmpty()
                }

                override fun getEditor(item: ParadoxInlineMathArgument?): TableCellEditor {
                    return DefaultCellEditor(JBTextField())
                }
            },
            object : ColumnInfo<ParadoxInlineMathArgument, String>(ChronicleBundle.message("ui.dialog.evaluator.inlineMath.table.column.defaultValue")) {
                override fun valueOf(item: ParadoxInlineMathArgument): String = item.defaultValue
            },
        ),
        argumentList
    )
    private val table = JBTable(tableModel).apply {
        rowSelectionAllowed = false
        columnSelectionAllowed = false
        intercellSpacing = Dimension(0, 0)
        val visibleRowCount = argumentList.size.coerceIn(1, preferredArgumentSize)
        preferredScrollableViewportSize = Dimension(0, rowHeight * visibleRowCount)

        // 快速搜索
        TableSpeedSearch.installOn(this) { e ->
            val element = e as ParadoxInlineMathArgument
            element.expression
        }.apply { comparator = SpeedSearchComparator(false) }
    }

    init {
        title = ChronicleBundle.message("ui.dialog.evaluator.inlineMath.title")
        setOKButtonText(ChronicleBundle.message("action.copy"))
        setCancelButtonText(ChronicleBundle.message("action.close"))
        init()
        pack()
    }

    override fun createCenterPanel(): DialogPanel {
        val panel = panel {
            row(ChronicleBundle.message("ui.dialog.evaluator.inlineMath.label.expression")) {
                cell(expressionField).align(Align.FILL)
            }

            row {
                val scrollPane = JBScrollPane(table)
                cell(scrollPane).align(Align.FILL)
            }.resizableRow()

            row(ChronicleBundle.message("ui.dialog.evaluator.inlineMath.label.result")) {
                val scrollPane = JBScrollPane().apply { setViewportView(resultTextArea) }
                cell(scrollPane).align(Align.FILL)
            }
        }.withPreferredWidth(preferredDialogWidth)

        // 实时求值
        tableModel.addTableModelListener { updateResultText() }
        updateResultText()

        return panel
    }

    override fun doOKAction() = copyResultText()

    override fun getPreferredFocusedComponent() = table

    override fun getDimensionServiceKey() = "Pls.ParadoxInlineMathEvaluatorDialog"

    private fun updateResultText() {
        val args = argumentList
            .mapNotNull { a ->
                val v = a.value.trim().orNull() ?: return@mapNotNull null
                a.expression to v
            }
            .toMap()
        evaluate(args)
        resultTextArea.text = currentResultText.orEmpty()
    }

    private fun copyResultText() {
        CopyPasteManager.getInstance().setContents(TextTransferable(currentResultText.orEmpty() as CharSequence))
    }

    private fun evaluate(args: Map<String, String>) {
        if (!isValid) {
            currentResult = null
            currentResultText = ChronicleBundle.message("ui.dialog.evaluator.inlineMath.message.invalid")
            return
        }

        val element = element
        if (element == null) {
            isValid = false
            currentResult = null
            currentResultText = ChronicleBundle.message("ui.dialog.evaluator.inlineMath.message.invalid")
            return
        }

        try {
            val result = evaluator.evaluate(element, args)
            isInitialized = true
            currentResult = result
            currentResultText = result.formatted()
        } catch (e: Throwable) {
            val message = e.message.orEmpty()
            if (!isInitialized && e is IllegalArgumentException && message.startsWith("Missing arguments:")) {
                // 存在缺失的传参，此时显示为空字符串
                currentResult = null
                currentResultText = ""
                return
            }
            isInitialized = true
            currentResult = null
            currentResultText = ChronicleBundle.message("ui.dialog.evaluator.inlineMath.message.exception") + message.errorDetails
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
        private const val inlineMathPrefix = "@[ "
        private const val inlineMathSuffix = " ]"
        private const val preferredArgumentSize = 5
        private val preferredDialogWidth = JBUI.scale(600)
        private val preferredTextWidth = JBUI.scale(200)
    }
}

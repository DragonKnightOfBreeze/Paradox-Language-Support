package icu.windea.pls.integrations.settings

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.PlsBundle
import icu.windea.pls.integrations.lints.PlsTigerLintResult
import java.awt.event.ItemEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JList

/**
 * Tiger 高亮映射配置对话框（置信度 × 严重度）。
 *
 * - 父级下拉（Weak/Reasonable/Strong）批量设置对应5个严重度（Tips/Untidy/Warning/Error/Fatal）。
 * - 当 5 个子项不一致时，父级显示为 Mixed。
 * - 选项显示 IDE 高亮级别对应的图标与名称。
 */
class PlsTigerHighlightDialog(
    private val state: PlsIntegrationsSettingsState.LintState.TigerHighlightState
) : DialogWrapper(null, true) {

    private data class ParentEntry(val severity: HighlightSeverity?, val label: String, val icon: Icon?)

    private val childCombos = mutableMapOf<Pair<PlsTigerLintResult.Confidence, PlsTigerLintResult.Severity>, ComboBox<HighlightSeverity>>()
    // key: confidence, value: parent combo
    private val parentCombos = mutableMapOf<PlsTigerLintResult.Confidence, ComboBox<ParentEntry>>()

    private val severityOptions = listOf(
        HighlightSeverity.INFORMATION,
        HighlightSeverity.WEAK_WARNING,
        HighlightSeverity.WARNING,
        HighlightSeverity.ERROR,
    )

    private val parentOptions: List<ParentEntry> = buildList {
        add(ParentEntry(null, PlsBundle.message("settings.integrations.lint.tigerHighlight.mixed"), AllIcons.Nodes.Folder))
        severityOptions.forEach { sev ->
            val level = HighlightDisplayLevel.Companion.find(sev) ?: HighlightDisplayLevel.Companion.WARNING
            add(ParentEntry(sev, level.name, level.icon))
        }
    }

    init {
        title = PlsBundle.message("settings.integrations.lint.tigerHighlight.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            comment(PlsBundle.message("settings.integrations.lint.tigerHighlight.dialog.comment"))
        }
        addConfidenceSection(this, PlsTigerLintResult.Confidence.Weak)
        addConfidenceSection(this, PlsTigerLintResult.Confidence.Reasonable)
        addConfidenceSection(this, PlsTigerLintResult.Confidence.Strong)
    }

    private fun addConfidenceSection(panel: Panel, confidence: PlsTigerLintResult.Confidence) {
        panel.group(confLabel(confidence)) {
            // parent combo
            lateinit var parent: ComboBox<ParentEntry>
            row(confLabel(confidence)) {
                val parentRenderer = object : SimpleListCellRenderer<ParentEntry>() {
                    override fun customize(list: JList<out ParentEntry>, value: ParentEntry?, index: Int, selected: Boolean, hasFocus: Boolean) {
                        if (value == null) {
                            icon = null
                            text = ""
                        } else {
                            icon = value.icon
                            text = value.label
                        }
                    }
                }
                comboBox(parentOptions, parentRenderer)
                    .applyToComponent {
                        parent = this
                        addItemListener { e ->
                            if (e.stateChange == ItemEvent.SELECTED) {
                                (selectedItem as? ParentEntry)?.severity?.let { sev ->
                                    // batch set children
                                    updateAllChildrenFor(confidence, sev)
                                    // also refresh parent selection (now uniform)
                                    setParentSelection(confidence)
                                }
                            }
                        }
                    }
                button(PlsBundle.message("settings.integrations.lint.tigerHighlight.resetParent")) {
                    resetChildrenToDefaults(confidence)
                    setParentSelection(confidence)
                }
            }
            parentCombos[confidence] = parent

            // children rows (Tips/Untidy/Warning/Error/Fatal)
            PlsTigerLintResult.Severity.entries.forEach { sev ->
                addChildRow(this, confidence, sev)
            }

            // init selections
            setChildrenSelections(confidence)
            setParentSelection(confidence)
        }
    }

    private fun addChildRow(panel: Panel, confidence: PlsTigerLintResult.Confidence, tigerSeverity: PlsTigerLintResult.Severity) {
        lateinit var child: ComboBox<HighlightSeverity>
        panel.row(sevLabel(tigerSeverity)) {
            val childRenderer = object : SimpleListCellRenderer<HighlightSeverity>() {
                override fun customize(list: JList<out HighlightSeverity>, value: HighlightSeverity?, index: Int, selected: Boolean, hasFocus: Boolean) {
                    if (value == null) {
                        icon = null
                        text = ""
                    } else {
                        val level = HighlightDisplayLevel.Companion.find(value) ?: HighlightDisplayLevel.Companion.WARNING
                        icon = level.icon
                        text = level.name
                    }
                }
            }
            comboBox(severityOptions, childRenderer).applyToComponent {
                child = this
                addItemListener { e ->
                    if (e.stateChange == ItemEvent.SELECTED) {
                        // child changed -> reflect to parent (maybe mixed)
                        setParentSelection(confidence)
                    }
                }
            }.align(Align.Companion.FILL)
        }
        childCombos[confidence to tigerSeverity] = child
    }

    private fun setChildrenSelections(confidence: PlsTigerLintResult.Confidence) {
        PlsTigerLintResult.Severity.entries.forEach { sev ->
            childCombos[confidence to sev]?.selectedItem = byName(getStateValue(confidence, sev))
        }
    }

    private fun setParentSelection(confidence: PlsTigerLintResult.Confidence) {
        val children = PlsTigerLintResult.Severity.entries
            .mapNotNull { childCombos[confidence to it]?.selectedItem as? HighlightSeverity }
        val allSame = children.isNotEmpty() && children.distinct().size == 1
        val parent = parentCombos[confidence] ?: return
        if (allSame) {
            val sev = children.first()
            parent.selectedItem = parentOptions.firstOrNull { it.severity == sev }
        } else {
            parent.selectedItem = parentOptions.firstOrNull { it.severity == null }
        }
    }

    private fun updateAllChildrenFor(confidence: PlsTigerLintResult.Confidence, sev: HighlightSeverity) {
        PlsTigerLintResult.Severity.entries.forEach { s ->
            childCombos[confidence to s]?.selectedItem = sev
        }
    }

    private fun resetChildrenToDefaults(confidence: PlsTigerLintResult.Confidence) {
        PlsTigerLintResult.Severity.entries.forEach { s ->
            childCombos[confidence to s]?.selectedItem = defaultSeverity(confidence, s)
        }
    }

    private fun defaultSeverity(confidence: PlsTigerLintResult.Confidence, tigerSeverity: PlsTigerLintResult.Severity): HighlightSeverity {
        return when (tigerSeverity) {
            PlsTigerLintResult.Severity.Tips -> when (confidence) {
                PlsTigerLintResult.Confidence.Weak -> HighlightSeverity.INFORMATION
                PlsTigerLintResult.Confidence.Reasonable -> HighlightSeverity.INFORMATION
                PlsTigerLintResult.Confidence.Strong -> HighlightSeverity.WEAK_WARNING
            }
            PlsTigerLintResult.Severity.Untidy -> when (confidence) {
                PlsTigerLintResult.Confidence.Weak -> HighlightSeverity.WEAK_WARNING
                PlsTigerLintResult.Confidence.Reasonable -> HighlightSeverity.WEAK_WARNING
                PlsTigerLintResult.Confidence.Strong -> HighlightSeverity.WARNING
            }
            PlsTigerLintResult.Severity.Warning -> when (confidence) {
                PlsTigerLintResult.Confidence.Weak -> HighlightSeverity.WARNING
                PlsTigerLintResult.Confidence.Reasonable -> HighlightSeverity.WARNING
                PlsTigerLintResult.Confidence.Strong -> HighlightSeverity.ERROR
            }
            PlsTigerLintResult.Severity.Error -> HighlightSeverity.ERROR
            PlsTigerLintResult.Severity.Fatal -> HighlightSeverity.ERROR
        }
    }

    override fun doOKAction() {
        // write back to state
        PlsTigerLintResult.Confidence.entries.forEach { conf ->
            PlsTigerLintResult.Severity.entries.forEach { sev ->
                val hs = childCombos[conf to sev]?.selectedItem as HighlightSeverity
                setStateValue(conf, sev, hs.name)
            }
        }
        super.doOKAction()
    }

    private fun confLabel(conf: PlsTigerLintResult.Confidence): String = when (conf) {
        PlsTigerLintResult.Confidence.Weak -> PlsBundle.message("settings.integrations.lint.tigerHighlight.weak")
        PlsTigerLintResult.Confidence.Reasonable -> PlsBundle.message("settings.integrations.lint.tigerHighlight.reasonable")
        PlsTigerLintResult.Confidence.Strong -> PlsBundle.message("settings.integrations.lint.tigerHighlight.strong")
    }

    private fun sevLabel(sev: PlsTigerLintResult.Severity): String = when (sev) {
        PlsTigerLintResult.Severity.Tips -> PlsBundle.message("settings.integrations.lint.tigerHighlight.tips")
        PlsTigerLintResult.Severity.Untidy -> PlsBundle.message("settings.integrations.lint.tigerHighlight.untidy")
        PlsTigerLintResult.Severity.Warning -> PlsBundle.message("settings.integrations.lint.tigerHighlight.warning")
        PlsTigerLintResult.Severity.Error -> PlsBundle.message("settings.integrations.lint.tigerHighlight.error")
        PlsTigerLintResult.Severity.Fatal -> PlsBundle.message("settings.integrations.lint.tigerHighlight.fatal")
    }

    private fun byName(name: String?): HighlightSeverity = when (name?.uppercase()) {
        HighlightSeverity.INFORMATION.name -> HighlightSeverity.INFORMATION
        HighlightSeverity.WEAK_WARNING.name -> HighlightSeverity.WEAK_WARNING
        HighlightSeverity.WARNING.name -> HighlightSeverity.WARNING
        HighlightSeverity.ERROR.name -> HighlightSeverity.ERROR
        else -> HighlightSeverity.WARNING
    }

    private fun getStateValue(conf: PlsTigerLintResult.Confidence, sev: PlsTigerLintResult.Severity): String? = when (conf) {
        PlsTigerLintResult.Confidence.Weak -> when (sev) {
            PlsTigerLintResult.Severity.Tips -> state.tipsWeak
            PlsTigerLintResult.Severity.Untidy -> state.untidyWeak
            PlsTigerLintResult.Severity.Warning -> state.warningWeak
            PlsTigerLintResult.Severity.Error -> state.errorWeak
            PlsTigerLintResult.Severity.Fatal -> state.fatalWeak
        }
        PlsTigerLintResult.Confidence.Reasonable -> when (sev) {
            PlsTigerLintResult.Severity.Tips -> state.tipsReasonable
            PlsTigerLintResult.Severity.Untidy -> state.untidyReasonable
            PlsTigerLintResult.Severity.Warning -> state.warningReasonable
            PlsTigerLintResult.Severity.Error -> state.errorReasonable
            PlsTigerLintResult.Severity.Fatal -> state.fatalReasonable
        }
        PlsTigerLintResult.Confidence.Strong -> when (sev) {
            PlsTigerLintResult.Severity.Tips -> state.tipsStrong
            PlsTigerLintResult.Severity.Untidy -> state.untidyStrong
            PlsTigerLintResult.Severity.Warning -> state.warningStrong
            PlsTigerLintResult.Severity.Error -> state.errorStrong
            PlsTigerLintResult.Severity.Fatal -> state.fatalStrong
        }
    }

    private fun setStateValue(conf: PlsTigerLintResult.Confidence, sev: PlsTigerLintResult.Severity, value: String) {
        when (conf) {
            PlsTigerLintResult.Confidence.Weak -> when (sev) {
                PlsTigerLintResult.Severity.Tips -> state.tipsWeak = value
                PlsTigerLintResult.Severity.Untidy -> state.untidyWeak = value
                PlsTigerLintResult.Severity.Warning -> state.warningWeak = value
                PlsTigerLintResult.Severity.Error -> state.errorWeak = value
                PlsTigerLintResult.Severity.Fatal -> state.fatalWeak = value
            }
            PlsTigerLintResult.Confidence.Reasonable -> when (sev) {
                PlsTigerLintResult.Severity.Tips -> state.tipsReasonable = value
                PlsTigerLintResult.Severity.Untidy -> state.untidyReasonable = value
                PlsTigerLintResult.Severity.Warning -> state.warningReasonable = value
                PlsTigerLintResult.Severity.Error -> state.errorReasonable = value
                PlsTigerLintResult.Severity.Fatal -> state.fatalReasonable = value
            }
            PlsTigerLintResult.Confidence.Strong -> when (sev) {
                PlsTigerLintResult.Severity.Tips -> state.tipsStrong = value
                PlsTigerLintResult.Severity.Untidy -> state.untidyStrong = value
                PlsTigerLintResult.Severity.Warning -> state.warningStrong = value
                PlsTigerLintResult.Severity.Error -> state.errorStrong = value
                PlsTigerLintResult.Severity.Fatal -> state.fatalStrong = value
            }
        }
    }
}

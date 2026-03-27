package icu.windea.pls.integrations.settings

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.listCellRenderer
import icu.windea.pls.integrations.PlsIntegrationsBundle
import icu.windea.pls.integrations.lints.LintHighlightSeverity
import icu.windea.pls.integrations.lints.TigerLintResult.*
import icu.windea.pls.integrations.lints.TigerLintToolUtil
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * Tiger 高亮映射配置对话框（严重度 x 置信度）。
 *
 * - 总计 3 + 3 * 5 = 18 个 comboBox。
 * - 当 5 个子项不一致时，父级显示为混合级别。
 * - 选项显示 IDE 高亮级别对应的图标与名称。
 * - 点击确定按钮后再保存设置并刷新文件。
 */
class TigerHighlightDialog : DialogWrapper(null, true) {
    // com.intellij.profile.codeInspection.ui.ScopesPanel

    // 使用 com.intellij.openapi.observable.properties.GraphProperty 来处理属性绑定关系
    // 对这些属性的更改不会立刻同步到插件设置，点击确定按钮后才会

    private val propertyGraph = PropertyGraph()
    // severity -> confidence -> highlight severity option
    private val propertyGroup = Severity.entries.associateWith { severity ->
        Confidence.entries.associateWith { confidence ->
            val p = TigerLintToolUtil.getConfiguredHighlightSeverity(confidence, severity)
            propertyGraph.property(p.get())
        }
    }
    // severity -> merged highlight severity option (null means mixed)
    private val mergedProperties = propertyGroup.mapValues { (_, map) ->
        map.values.merged()
    }

    init {
        title = PlsIntegrationsBundle.message("settings.integrations.lint.tigerHighlight.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            comment(PlsIntegrationsBundle.message("settings.integrations.lint.tigerHighlight.dialog.comment"))
        }
        createMapping()
    }

    // 方案1：通过分割线分成5组，每组两行，第一行包括严重度的总cb、重置按钮，第二行包括各个置信度的cb
    // private fun Panel.createMapping() {
    //     val m = OnceMarker()
    //     mergedProperties.forEach { (severity, mergedOption) ->
    //         lateinit var mergedCb: ComboBox<LintHighlightSeverity>
    //
    //         if (m.mark()) separator()
    //         row {
    //             label(PlsBundle.message("lint.tiger.severity")).widthGroup("tiger.label.0")
    //             label(TigerLintToolManager.getSeverityDisplayName(severity)).widthGroup("tiger.label.1")
    //             highlightSeverityComboBox(mergedOption).applyToComponent { mergedCb = this }
    //             button(PlsBundle.message("settings.integrations.lint.tigerHighlight.reset")) { resetOptionsToDefaults(severity) }
    //         }
    //         var i = 0
    //         row {
    //             label(PlsBundle.message("lint.tiger.confidence")).widthGroup("tiger.label.0")
    //             propertyGroup.getValue(severity).forEach { (confidence, option) ->
    //                 i++
    //                 label(TigerLintToolManager.getConfidenceDisplayName(confidence)).widthGroup("tiger.label.$i")
    //                 highlightSeverityComboBox(option)
    //                 forceRefreshMergedOption(option, mergedCb)
    //             }
    //         }
    //     }
    // }

    // 方案2：采用表格形式，行是严重度，列是置信度，重置按钮在每一行的所有cb之后
    private fun Panel.createMapping() {
        val severityPrefix = PlsIntegrationsBundle.message("lint.tiger.severity")
        val confidencePrefix = PlsIntegrationsBundle.message("lint.tiger.confidence")

        row {
            label("").widthGroup("tiger.c0")
            label(confidencePrefix + PlsIntegrationsBundle.message("lint.tiger.confidence.all")).widthGroup("tiger.c1")
            Confidence.entries.forEachIndexed { i, e -> label(confidencePrefix + TigerLintToolUtil.getConfidenceDisplayName(e)).widthGroup("tiger.c${i + 2}") }
        }
        Severity.entries.forEach { severity ->
            row {
                lateinit var mergedCb: ComboBox<LintHighlightSeverity>

                label(severityPrefix + TigerLintToolUtil.getSeverityDisplayName(severity)).widthGroup("tiger.c0")
                val mergedOption = mergedProperties.getValue(severity)
                highlightSeverityComboBox(mergedOption).widthGroup("tiger.c1").applyToComponent { mergedCb = this }
                var i = 0
                propertyGroup.getValue(severity).forEach { (_, option) ->
                    i++
                    highlightSeverityComboBox(option).widthGroup("tiger.c${i + 1}")
                    forceRefreshMergedOption(option, mergedCb)
                }
                button(PlsIntegrationsBundle.message("settings.integrations.lint.tigerHighlight.reset")) { resetOptionsToDefaults(severity) }
            }
        }
    }

    private fun Collection<GraphProperty<LintHighlightSeverity>>.merged(): GraphProperty<LintHighlightSeverity> {
        val mp = propertyGraph.property(mergedValue())
        forEach { p ->
            // 传入 deleteWhenModified = false 后，循环更新会被 PropertyGraph 自动处理且继续保留
            mp.dependsOn(p, false) { mergedValue() }
            p.dependsOn(mp, false) { mp.get().takeIf { it != LintHighlightSeverity.Merged } ?: p.get() }
        }
        return mp
    }

    private fun Collection<GraphProperty<LintHighlightSeverity>>.mergedValue(): LintHighlightSeverity {
        return this.mapTo(mutableSetOf()) { it.get() }.singleOrNull() ?: LintHighlightSeverity.Merged
    }

    @Suppress("UnstableApiUsage")
    private fun Row.highlightSeverityComboBox(property: GraphProperty<LintHighlightSeverity>): Cell<ComboBox<LintHighlightSeverity>> {
        // 模型中不包含 Merged，但在选中区域根据属性值为 Merged 时显示“混合”
        val renderer = listCellRenderer<LintHighlightSeverity?> {
            // index == -1 表示渲染选中值；此时如果属性值为 Merged，则强制显示“混合”
            val toShow = if (index == -1 && property.get() == LintHighlightSeverity.Merged) LintHighlightSeverity.Merged else value
            toShow?.icon?.let { icon(it) }
            toShow?.displayName?.let { text(it) }
        }
        return comboBox(LintHighlightSeverity.getAll(), renderer).bindItem(property)
    }

    private fun forceRefreshMergedOption(property: GraphProperty<LintHighlightSeverity>, mergedCb: ComboBox<LintHighlightSeverity>) {
        // 注意：需要在 mergedOption 变化时主动触发重绘，否则 UI 仅在重新聚焦时才会刷新选中区渲染
        property.afterChange {
            SwingUtilities.invokeLater {
                (mergedCb.model as? CollectionComboBoxModel<LintHighlightSeverity>)?.update()
                mergedCb.revalidate()
                mergedCb.repaint()
            }
        }
    }

    private fun resetOptionsToDefaults(severity: Severity) {
        propertyGroup.getValue(severity).forEach { (confidence, option) ->
            val dv = TigerLintToolUtil.getDefaultHighlightSeverity(confidence, severity)
            option.set(dv) // 这会导致 mp.fireChangeEvent() 被调用多次，但是问题不大
        }
    }

    override fun doOKAction() {
        // 同步属性更改到插件设置
        propertyGroup.forEach { (severity, options) ->
            options.forEach { (confidence, option) ->
                TigerLintToolUtil.getConfiguredHighlightSeverity(confidence, severity).set(option.get())
            }
        }
        super.doOKAction()
    }

    override fun getDimensionServiceKey() = "Pls.TigerHighlightDialog"
}

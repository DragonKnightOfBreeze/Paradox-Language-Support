package icu.windea.pls.integrations.settings

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.listCellRenderer
import icu.windea.pls.PlsBundle
import icu.windea.pls.integrations.lints.PlsLintHighlightSeverity
import icu.windea.pls.integrations.lints.PlsTigerLintManager
import icu.windea.pls.integrations.lints.PlsTigerLintResult.Confidence
import icu.windea.pls.integrations.lints.PlsTigerLintResult.Severity
import javax.swing.JComponent

/**
 * Tiger 高亮映射配置对话框（严重度 x 置信度）。
 *
 * - 总计 3 + 3 * 5 = 18 个 comboBox。
 * - 当 5 个子项不一致时，父级显示为混合级别。
 * - 选项显示 IDE 高亮级别对应的图标与名称。
 * - 点击确定按钮后再保存设置并刷新文件。
 */
class PlsTigerHighlightDialog : DialogWrapper(null, true) {
    // com.intellij.profile.codeInspection.ui.ScopesPanel

    // 使用 com.intellij.openapi.observable.properties.GraphProperty 来处理属性绑定关系
    // 对这些属性的更改不会立刻同步到插件设置，点击确定按钮后才会

    private val propertyGraph = PropertyGraph()
    // severity -> confidence -> highlight severity option
    private val propertyGroup = Severity.entries.associateWith { severity ->
        Confidence.entries.associateWith { confidence ->
            val p = PlsTigerLintManager.getConfiguredHighlightSeverity(confidence, severity)
            propertyGraph.property(p.get())
        }
    }
    // severity -> merged highlight severity option (null means mixed)
    private val mergedProperties = propertyGroup.mapValues { (_, map) ->
        map.values.merged()
    }

    init {
        title = PlsBundle.message("settings.integrations.lint.tigerHighlight.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            comment(PlsBundle.message("settings.integrations.lint.tigerHighlight.dialog.comment"))
        }
        row {
            comment(PlsBundle.message("settings.integrations.lint.tigerHighlight.dialog.comment1"))
        }
        createMapping()
    }

    // 方案1：通过分割线分成5组，每组两行，第一行包括严重度的总cb、重置按钮，第二行包括各个置信度的cb
    // private fun Panel.createMapping() {
    //     var appendSeparator = false
    //     mergedProperties.forEach { (severity, mergedOption) ->
    //         if (appendSeparator) separator() else appendSeparator = true
    //         row {
    //             label(PlsBundle.message("lint.tiger.severity")).widthGroup("tiger.label.0")
    //             label(PlsTigerLintManager.getSeverityDisplayName(severity)).widthGroup("tiger.label.1")
    //             highlightSeverityComboBox(withMerged = true).bindItem(mergedOption)
    //             button(PlsBundle.message("settings.integrations.lint.tigerHighlight.reset")) { resetOptionsToDefaults(severity) }
    //         }
    //         var i = 0
    //         row {
    //             label(PlsBundle.message("lint.tiger.confidence")).widthGroup("tiger.label.0")
    //             propertyGroup.getValue(severity).forEach { (confidence, option) ->
    //                 i++
    //                 label(PlsTigerLintManager.getConfidenceDisplayName(confidence)).widthGroup("tiger.label.$i")
    //                 highlightSeverityComboBox().bindItem(option)
    //             }
    //         }
    //     }
    // }

    // 方案2：采用表格形式，行是严重度，列是置信度，重置按钮在每一行的所有cb之后
    private fun Panel.createMapping() {
        val severityPrefix = PlsBundle.message("lint.tiger.severity")
        val confidencePrefix = PlsBundle.message("lint.tiger.confidence")

        row {
            label("").widthGroup("tiger.c0")
            label(confidencePrefix + PlsBundle.message("lint.tiger.confidence.all")).widthGroup("tiger.c1")
            Confidence.entries.forEachIndexed { i, e -> label(confidencePrefix + PlsTigerLintManager.getConfidenceDisplayName(e)).widthGroup("tiger.c${i + 2}") }
        }
        Severity.entries.forEach { severity ->
            row {
                label(severityPrefix + PlsTigerLintManager.getSeverityDisplayName(severity)).widthGroup("tiger.c0")
                val mergedOption = mergedProperties.getValue(severity)
                highlightSeverityComboBox(withMerged = true).bindItem(mergedOption).widthGroup("tiger.c1")
                var i = 0
                propertyGroup.getValue(severity).forEach { (_, option) ->
                    i++
                    highlightSeverityComboBox().bindItem(option).widthGroup("tiger.c${i + 1}")
                }
                button(PlsBundle.message("settings.integrations.lint.tigerHighlight.reset")) { resetOptionsToDefaults(severity) }
            }
        }
    }

    private fun Collection<GraphProperty<PlsLintHighlightSeverity>>.merged(): GraphProperty<PlsLintHighlightSeverity> {
        val mp = propertyGraph.property(mergedValue())
        forEach { p ->
            // 传入 deleteWhenModified = false 后，循环更新会被 PropertyGraph 自动处理且继续保留
            mp.dependsOn(p, false) { mergedValue() }
            p.dependsOn(mp, false) { mp.get().takeIf { it != PlsLintHighlightSeverity.Merged } ?: p.get() }
        }
        return mp
    }

    private fun Collection<GraphProperty<PlsLintHighlightSeverity>>.mergedValue(): PlsLintHighlightSeverity {
        return this.mapTo(mutableSetOf()) { it.get() }.singleOrNull() ?: PlsLintHighlightSeverity.Merged
    }

    @Suppress("UnstableApiUsage")
    private fun highlightSeverityRender() = listCellRenderer<PlsLintHighlightSeverity?> {
        value?.icon?.let { icon(it) }
        value?.displayName?.let { text(it) }
    }

    private fun Row.highlightSeverityComboBox(withMerged: Boolean = false) = comboBox(PlsLintHighlightSeverity.getAll(withMerged), highlightSeverityRender())

    private fun resetOptionsToDefaults(severity: Severity) {
        propertyGroup.getValue(severity).forEach { (confidence, option) ->
            val dv = PlsTigerLintManager.getDefaultHighlightSeverity(confidence, severity)
            option.set(dv) // 这会导致 mp.fireChangeEvent() 被调用多次，但是问题不大
        }
    }

    override fun doOKAction() {
        // 同步属性更改到插件设置
        propertyGroup.forEach { (severity, options) ->
            options.forEach { (confidence, option) ->
                PlsTigerLintManager.getConfiguredHighlightSeverity(confidence, severity).set(option.get())
            }
        }
        super.doOKAction()
    }
}

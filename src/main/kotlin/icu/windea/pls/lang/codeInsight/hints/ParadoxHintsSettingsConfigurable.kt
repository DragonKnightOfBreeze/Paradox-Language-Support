package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.ui.toAtomicProperty
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class ParadoxHintsSettingsConfigurable(
    private val provider: ParadoxHintsProvider,
    private val settings: ParadoxHintsSettings,
) : ImmediateConfigurable {
    override fun createComponent(listener: ChangeListener): JComponent = panel {
        // NOTE 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        if (provider.showScopeContextInfo) createScopeContextInfoRow()
    }

    private fun Panel.createScopeContextInfoRow() {
        row {
            checkBox(ChronicleBundle.message("hints.settings.showScopeContextOnlyIfIsChanged"))
                .bindSelected(settings::showScopeContextOnlyIfIsChanged.toAtomicProperty())
        }
    }
}

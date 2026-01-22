package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class ParadoxHintsSettingsConfigurable(
    private val provider: ParadoxHintsProvider,
    private val settings: ParadoxHintsSettings,
) : ImmediateConfigurable {
    override fun createComponent(listener: ChangeListener): JComponent = panel {
        // NOTE 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        if (provider.showScopeContextInfo) createScopeContextInfoRow()
        if (provider.renderLocalisation) createLocalisationTextLengthLimitRow()
        if (provider.renderIcon) createIconHeightLimitRow()
    }

    private fun Panel.createScopeContextInfoRow() {
        row {
            checkBox(PlsBundle.message("hints.settings.showScopeContextOnlyIfIsChanged"))
                .bindSelected(settings::showScopeContextOnlyIfIsChanged.toAtomicProperty())
        }
    }

    private fun Panel.createLocalisationTextLengthLimitRow() {
        row {
            label(PlsBundle.message("hints.settings.localisationTextLengthLimit")).widthGroup("left")
            textField()
                .bindIntText(settings::localisationTextLengthLimit.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
            contextHelp(PlsBundle.message("hints.settings.localisationTextLengthLimit.tip"))
        }
    }

    private fun Panel.createIconHeightLimitRow() {
        row {
            label(PlsBundle.message("hints.settings.iconHeightLimit")).widthGroup("left")
            textField()
                .bindIntText(settings::iconHeightLimit.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
            contextHelp(PlsBundle.message("hints.settings.iconHeightLimit.tip"))
        }
    }
}

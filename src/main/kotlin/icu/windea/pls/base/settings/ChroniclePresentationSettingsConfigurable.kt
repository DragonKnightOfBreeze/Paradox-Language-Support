package icu.windea.pls.base.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.base.ChronicleBaseBundle

class ChroniclePresentationSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings.presentation")), SearchableConfigurable {
    override fun getId() = "chronicle.presentation"

    override fun getHelpTopic() = null

    override fun createPanel(): DialogPanel {
        return panel {
            group(ChronicleBaseBundle.message("settings.presentation.general")) {
                val settings = ChroniclePresentationSettings.getInstance().state
                // presentableTextLengthLimit
                row {
                    label(ChronicleBaseBundle.message("settings.presentation.presentableTextLengthLimit")).widthGroup("chronicle.presentation.general")
                    intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::presentableTextLengthLimit)
                    contextHelp(ChronicleBaseBundle.message("settings.presentation.presentableTextLengthLimit.tip"))
                }
            }
            group(ChronicleBaseBundle.message("settings.presentation.quickDoc")) { configureGroupForQuickDoc() }
            group(ChronicleBaseBundle.message("settings.presentation.inlay")) { configureGroupForInlay() }
        }
    }

    private fun Panel.configureGroupForQuickDoc() {
        val group = "chronicle.presentation.quickDoc"
        val settings = ChroniclePresentationSettings.getInstance().state

        // quickDocLocalisationTextFontSize
        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocLocalisationTextFontSize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocLocalisationTextFontSize)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocLocalisationTextFontSize.tip"))
        }
        // quickDocMaxImageDisplaySize
        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocMaxImageDisplaySize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocMaxImageDisplaySize)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocMaxImageDisplaySize.tip"))
        }
        // quickDocTextIconHeightLimit
        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocTextIconHeightLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocTextIconHeightLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocTextIconHeightLimit.tip"))
        }
    }

    private fun Panel.configureGroupForInlay() {
        val group = "chronicle.presentation.inlay"
        val settings = ChroniclePresentationSettings.getInstance().state

        // inlayLocalisationTextLengthLimit
        row {
            label(ChronicleBaseBundle.message("settings.presentation.inlayLocalisationTextLengthLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::inlayLocalisationTextLengthLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.inlayLocalisationTextLengthLimit.tip"))
        }
        // inlayTextIconHeightLimit
        row {
            label(ChronicleBaseBundle.message("settings.presentation.inlayTextIconHeightLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::inlayTextIconHeightLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.inlayTextIconHeightLimit.tip"))
        }
    }
}

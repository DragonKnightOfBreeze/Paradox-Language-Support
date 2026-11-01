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
                row {
                    label(ChronicleBaseBundle.message("settings.presentation.presentableTextLimit")).widthGroup("chronicle.presentation.general")
                    intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::presentableTextLimit)
                    contextHelp(ChronicleBaseBundle.message("settings.presentation.presentableTextLimit.tip"))
                }
            }
            group(ChronicleBaseBundle.message("settings.presentation.quickDoc")) { configureGroupForQuickDoc() }
            group(ChronicleBaseBundle.message("settings.presentation.inlay")) { configureGroupForInlay() }
        }
    }

    private fun Panel.configureGroupForQuickDoc() {
        val group = "chronicle.presentation.quickDoc"
        val settings = ChroniclePresentationSettings.getInstance().state

        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocLocalisationTextFontSize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocLocalisationTextFontSize)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocLocalisationTextFontSize.tip"))
        }
        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocMaxImageSize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocMaxImageSize)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocMaxImageSize.tip"))
        }
        row {
            label(ChronicleBaseBundle.message("settings.presentation.quickDocTextIconSizeLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::quickDocTextIconSizeLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.quickDocTextIconSizeLimit.tip"))
        }
    }

    private fun Panel.configureGroupForInlay() {
        val group = "chronicle.presentation.inlay"
        val settings = ChroniclePresentationSettings.getInstance().state

        row {
            label(ChronicleBaseBundle.message("settings.presentation.inlayLocalisationTextLengthLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::inlayLocalisationTextLengthLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.inlayLocalisationTextLengthLimit.tip"))
        }
        row {
            label(ChronicleBaseBundle.message("settings.presentation.inlayIconHeightLimit")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::inlayIconHeightLimit)
            contextHelp(ChronicleBaseBundle.message("settings.presentation.inlayIconHeightLimit.tip"))
        }
    }
}

package icu.windea.pls.extension.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.extension.PlsExtensionsBundle
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.constants.PlsHelpTopics

class PlsExtensionsSettingsConfigurable : BoundConfigurable(PlsExtensionsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls.extensions"

    override fun getHelpTopic() = PlsHelpTopics.extensionsSettings

    // private val callbackLock = CallbackLock()

    override fun createPanel(): DialogPanel {
        // callbackLock.reset()
        val settings = PlsExtensionsSettings.getInstance().state
        return panel {
            // markdown
            group(PlsExtensionsBundle.message("settings.markdown")) {
                val markdownSettings = settings.markdown

                // resolveLinks
                row {
                    checkBox(PlsExtensionsBundle.message("settings.markdown.resolveLinks")).bindSelected(markdownSettings::resolveLinks)
                    contextHelp(PlsExtensionsBundle.message("settings.markdown.resolveLinks.tip"))
                    browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-link"))
                }
                // resolveInlineCodes
                row {
                    checkBox(PlsExtensionsBundle.message("settings.markdown.resolveInlineCodes")).bindSelected(markdownSettings::resolveInlineCodes)
                    contextHelp(PlsExtensionsBundle.message("settings.markdown.resolveInlineCodes.tip"))
                    browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-inline-code"))
                }
                // injectCodeBlocks
                row {
                    checkBox(PlsExtensionsBundle.message("settings.markdown.injectCodeBlocks")).bindSelected(markdownSettings::injectCodeBlocks)
                    contextHelp(PlsExtensionsBundle.message("settings.markdown.injectCodeBlocks.tip"))
                    browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-code-block"))
                }
            }
        }
    }
}

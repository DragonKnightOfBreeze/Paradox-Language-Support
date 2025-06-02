package icu.windea.pls.integrations.settings

import com.intellij.ide.DataManager
import com.intellij.openapi.options.*
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.integrations.*

class PlsIntegrationsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.integrations")), SearchableConfigurable {
    override fun getId() = "pls.integrations"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getIntegrationsSettings()
        return panel {
            //image tools
            group(PlsBundle.message("settings.integrations.image")) {
                row {
                    comment(PlsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.texconv")).selected(true).enabled(false)
                        .comment(PlsBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Texconv.url)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.magick")).bindSelected(settings.image::enableMagick)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Magick.url)
                }.visible(false) //TODO 2.0.0-dev
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.paint.net")).bindSelected(settings.image::enablePaintNet)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.PaintNet.url)
                }.visible(false) //TODO 2.0.0-dev
            }
            //translation tools
            group(PlsBundle.message("settings.integrations.translation")) {
                row {
                    comment(PlsBundle.message("settings.integrations.translation.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.tp")).selected(true).enabled(false)
                        .comment(PlsBundle.message("settings.integrations.translation.from.tp.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.TranslationPlugin.url)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.ai")).selected(true).enabled(false)
                    link(PlsBundle.message("settings.integrations.translation.from.ai.link")) {
                        DataManager.getInstance().dataContextFromFocusAsync.then {
                            Settings.KEY.getData(it)?.let { settings ->
                                //settings.find(PlsAiSettingsConfigurable::class.java)?.let { configurable ->
                                //    settings.select(configurable)
                                //}
                            }
                        }
                    }
                }.visible(false) //TODO 2.0.0-dev
            }
            //lint tools
            group(PlsBundle.message("settings.integrations.lint")) {
                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.lint.tiger")).bindSelected(settings.lint::enableTiger)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Tiger.url)
                }
            }.visible(false) //TODO 2.0.0-dev
        }
    }
}

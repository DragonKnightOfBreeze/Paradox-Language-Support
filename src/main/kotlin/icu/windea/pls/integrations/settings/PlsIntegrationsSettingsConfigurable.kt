package icu.windea.pls.integrations.settings

import com.intellij.ide.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.options.*
import com.intellij.openapi.options.ex.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.selected
import icu.windea.pls.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.integrations.*
import icu.windea.pls.integrations.image.providers.*

@Suppress("UnstableApiUsage")
class PlsIntegrationsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.integrations")), SearchableConfigurable {
    override fun getId() = "pls.integrations"

    private val groupNameImage = "pls.integrations.image"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getIntegrationsSettings()
        return panel {
            //image tools
            group(PlsBundle.message("settings.integrations.image")) {
                lateinit var cbMagick: JBCheckBox

                row {
                    comment(PlsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.texconv")).selected(true).enabled(false)
                        .comment(PlsBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Texconv.url)
                }
                //enableMagick
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.magick")).bindSelected(settings.image::enableMagick)
                        .comment(PlsBundle.message("settings.integrations.image.from.magick.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                        .applyToComponent { cbMagick = this }
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Magick.url)
                }
                //magickPath
                row {
                    label(PlsBundle.message("settings.integrations.image.magickPath")).widthGroup(groupNameImage)
                    val descriptor = FileChooserDescriptorFactory.singleFile()
                        .withTitle(PlsBundle.message("settings.integrations.image.magickPath.title"))
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(settings.image::magickPath.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                        .align(Align.FILL)
                        .validationOnInput { validateMagickPath(this, it) }
                }.enabledIf(cbMagick.selected)
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
                            //直接转到AI设置页面
                            Settings.KEY.getData(it)?.let { settings ->
                                settings.find(PlsAiSettingsConfigurable::class.java)?.let { configurable ->
                                    settings.select(configurable)
                                }
                            }

                            //这会嵌套打开AI设置页面
                            //ShowSettingsUtil.getInstance().showSettingsDialog(null, PlsAiSettingsConfigurable::class.java)
                        }
                    }
                }
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
            }.visible(false) //TODO 2.0.0-dev+
        }
    }

    private fun validateMagickPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = PlsImageToolProvider.EP_NAME.findExtension(PlsMagickToolProvider::class.java) ?: return null
        if (tool.validatePath(path)) return null
        return builder.warning(PlsBundle.message("settings.integrations.image.magickPath.incorrect"))
    }
}

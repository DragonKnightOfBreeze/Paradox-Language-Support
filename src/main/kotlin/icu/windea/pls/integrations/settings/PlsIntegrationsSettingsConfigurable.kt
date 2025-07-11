package icu.windea.pls.integrations.settings

import com.intellij.ide.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.options.*
import com.intellij.openapi.options.ex.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import icu.windea.pls.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.core.tupleOf
import icu.windea.pls.integrations.*
import icu.windea.pls.model.*

@Suppress("UnstableApiUsage")
class PlsIntegrationsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.integrations")), SearchableConfigurable {
    override fun getId() = "pls.integrations"

    private val groupNameImage = "pls.integrations.image"
    private val groupNameLint = "pls.integrations.lint"
    private val callbackLock = mutableSetOf<String>()

    override fun createPanel(): DialogPanel {
        callbackLock.clear()
        val settings = PlsFacade.getIntegrationsSettings()
        return panel {
            //image tools
            group(PlsBundle.message("settings.integrations.image")) {
                lateinit var cbMagick: JBCheckBox

                row {
                    comment(PlsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.image.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.image.comment2"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.texconv")).bindSelected(settings.image::enableTexconv)
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
                        .validationOnInput { PlsIntegrationsSettingsManager.validateMagickPath(this, it) }
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
                lateinit var cbTiger: JBCheckBox

                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                //enableTiger
                row {
                    checkBox(PlsBundle.message("settings.integrations.lint.tiger")).bindSelected(settings.lint::enableTiger)
                        .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock) }
                        .applyToComponent { cbTiger = this }
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Tiger.url)
                }
                val map = buildMap {
                    put(ParadoxGameType.Ck3, tupleOf("ck3-tiger", settings.lint::ck3TigerPath, settings.lint::ck3TigerConfPath))
                    put(ParadoxGameType.Ir, tupleOf("imperator-tiger", settings.lint::irTigerPath, settings.lint::irTigerConfPath))
                    put(ParadoxGameType.Vic3, tupleOf("vic3-tiger", settings.lint::vic3TigerPath, settings.lint::vic3TigerConfPath))
                }

                map.forEach { gameType, tuple ->
                    val (name, pathProp, confPathProp) = tuple

                    row {
                        label(PlsBundle.message("settings.integrations.lint.tigerPath", name)).widthGroup(groupNameLint)
                        val descriptor = FileChooserDescriptorFactory.singleFile()
                            .withTitle(PlsBundle.message("settings.integrations.lint.tigerPath.title", name))
                        textFieldWithBrowseButton(descriptor, null)
                            .bindText(pathProp.toNonNullableProperty(""))
                            .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                            .align(Align.FILL)
                            .validationOnInput { PlsIntegrationsSettingsManager.validateTigerPath(this, it, gameType) }
                            .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
                    }.enabledIf(cbTiger.selected)
                    row {
                        label(PlsBundle.message("settings.integrations.lint.tigerConfPath", name)).widthGroup(groupNameLint)
                        val descriptor = FileChooserDescriptorFactory.singleFile()
                            //.withExtensionFilter("conf") //这里不预先按扩展名过滤
                            .withTitle(PlsBundle.message("settings.integrations.lint.tigerConfPath.title", name))
                        textFieldWithBrowseButton(descriptor, null)
                            .bindText(confPathProp.toNonNullableProperty(""))
                            .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                            .validationOnInput { PlsIntegrationsSettingsManager.validateTigerConfPath(this, it, gameType) }
                            .align(Align.FILL)
                            .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
                    }.enabledIf(cbTiger.selected)
                }
            }
        }
    }
}

package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.selected
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.providers.ChatModelProviderType

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"
    private val groupNameAnthropic = "pls.ai.anthropic"
    private val groupNameLocal = "pls.ai.local"

    override fun createPanel(): DialogPanel {
        val settings = PlsAiFacade.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                contextHelp(PlsBundle.message("settings.ai.enable.tip"))
            }
            //providerType
            row {
                label(PlsBundle.message("settings.ai.providerType"))
                comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text })
                    .bindItem(settings::providerType.toNullableProperty())
            }
            //withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
            }

            //features
            group(PlsBundle.message("settings.ai.features")) {
                //localisationBatchSize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationChunkSize"))
                    intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings.features::localisationChunkSize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationChunkSize.tip"))
                }
                //localisationMemorySize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationMemorySize"))
                    intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings.features::localisationMemorySize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationMemorySize.tip"))
                }
            }

            //openAI
            collapsibleGroup(PlsBundle.message("settings.ai.openAI")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiModelName) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.openAI::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiEndpoint) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.openAI::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .validationOnInput { PlsAiSettingsManager.validateOpenAiApiKey(this, it) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    passwordField().bindText(settings.openAI::apiKeyEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultOpenAiApiKeyEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.openAI.fromEnv")).bindSelected(settings.openAI::fromEnv)
                        .applyToComponent { envCheckBox = this }
                }
            }

            //anthropic
            collapsibleGroup(PlsBundle.message("settings.ai.anthropic")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.anthropic.modelName")).widthGroup(groupNameAnthropic)
                    textField().bindText(settings.anthropic::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicModelName) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.anthropic::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiEndpoint")).widthGroup(groupNameAnthropic)
                    textField().bindText(settings.anthropic::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiEndpoint) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.anthropic::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiKey")).widthGroup(groupNameAnthropic)
                    passwordField().bindText(settings.anthropic::apiKey.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .validationOnInput { PlsAiSettingsManager.validateAnthropicApiKey(this, it) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    passwordField().bindText(settings.anthropic::apiKeyEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultAnthropicApiKeyEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.openAI.fromEnv")).bindSelected(settings.anthropic::fromEnv)
                        .applyToComponent { envCheckBox = this }
                }
            }

            //local (Ollama)
            collapsibleGroup(PlsBundle.message("settings.ai.local")) {
                lateinit var envCheckBox: JBCheckBox

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.local.modelName")).widthGroup(groupNameLocal)
                    textField().bindText(settings.local::modelName.toNonNullableProperty(""))
                        .columns(COLUMNS_MEDIUM)

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.local::modelNameEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalModelNameEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.local.apiEndpoint")).widthGroup(groupNameLocal)
                    textField().bindText(settings.local::apiEndpoint.toNonNullableProperty(""))
                        .columns(COLUMNS_LARGE)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalApiEndpoint) }

                    label(PlsBundle.message("settings.ai.openAI.env"))
                        .visibleIf(envCheckBox.selected)
                    textField().bindText(settings.local::apiEndpointEnv.toNonNullableProperty(""))
                        .columns(COLUMNS_SHORT)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.defaultLocalApiEndpointEnv) }
                        .visibleIf(envCheckBox.selected)
                }
                row {
                    checkBox(PlsBundle.message("settings.ai.openAI.fromEnv")).bindSelected(settings.local::fromEnv)
                        .applyToComponent { envCheckBox = this }
                }
            }
        }
    }
}

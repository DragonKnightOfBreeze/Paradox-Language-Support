package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiConstants
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.providers.AnthropicChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.ai.providers.LocalChatModelProvider
import icu.windea.pls.ai.providers.OpenAiChatModelProvider

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun getHelpTopic() = "icu.windea.pls.ai.settings"

    override fun createPanel(): DialogPanel {
        val settings = PlsAiFacade.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                contextHelp(PlsBundle.message("settings.ai.enable.tip"))
            }
            //withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                contextHelp(PlsBundle.message("settings.ai.withContext.tip"))
            }
            //providerType
            row {
                label(PlsBundle.message("settings.ai.providerType"))
                comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text })
                    .bindItem(settings::providerType.toNullableProperty())
            }

            //features
            collapsibleGroup(PlsBundle.message("settings.ai.features")) {
                val featuresSettings = settings.features
                val group = "pls.ai.features"

                //localisationBatchSize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationChunkSize")).widthGroup(group)
                    intTextField(1..Int.MAX_VALUE, 1).bindIntText(featuresSettings::localisationChunkSize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationChunkSize.tip"))
                }
                //localisationMemorySize
                row {
                    label(PlsBundle.message("settings.ai.features.localisationMemorySize")).widthGroup(group)
                    intTextField(0..Int.MAX_VALUE, 1).bindIntText(featuresSettings::localisationMemorySize)
                    contextHelp(PlsBundle.message("settings.ai.features.localisationMemorySize.tip"))
                }
            }

            //openAI
            collapsibleGroup(PlsBundle.message("settings.ai.openAI")) {
                val openAiSettings = settings.openAI
                val openAiProperties = OpenAiChatModelProvider.Options.AtomicProperties()
                val group = "pls.ai.openAI"

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(openAiProperties.modelName)
                        .bindText(openAiSettings::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultModelName) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(openAiProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(openAiProperties.fromEnv)
                        .bindText(openAiProperties.modelNameEnv)
                        .bindText(openAiSettings::modelNameEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultModelNameEnv) }
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(openAiProperties.apiEndpoint)
                        .bindText(openAiSettings::apiEndpoint.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(openAiProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(openAiProperties.fromEnv)
                        .bindText(openAiProperties.apiEndpointEnv)
                        .bindText(openAiSettings::apiEndpointEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiEndpointEnv) }
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(group)
                    passwordField().columns(COLUMNS_MEDIUM)
                        .bindText(openAiProperties.apiKey)
                        .bindText(openAiSettings::apiKey.toNonNullableProperty(""))
                        .validationOnInput { validateApiKey(this, it, openAiProperties.fromEnv.get()) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(openAiProperties.fromEnv)
                    passwordField().columns(COLUMNS_SHORT).visibleIf(openAiProperties.fromEnv)
                        .bindText(openAiProperties.apiKeyEnv)
                        .bindText(openAiSettings::apiKeyEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiKeyEnv) }
                }
                row {
                    button(PlsBundle.message("settings.ai.test")) {
                        // 注意这里需要基于可能尚未保存的配置项进行测试
                        val options = OpenAiChatModelProvider.Options.fromProperties(openAiProperties.toProperties())
                        val r = OpenAiChatModelProvider.INSTANCE.checkStatus(options)
                        showTestMessage(r)
                    }
                    checkBox(PlsBundle.message("settings.ai.fromEnv"))
                        .bindSelected(openAiProperties.fromEnv)
                        .bindSelected(openAiSettings::fromEnv)
                }
            }

            //anthropic
            collapsibleGroup(PlsBundle.message("settings.ai.anthropic")) {
                val group = "pls.ai.anthropic"
                val anthropicProperties = AnthropicChatModelProvider.Options.AtomicProperties()
                val anthropicSettings = settings.anthropic

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.anthropic.modelName")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(anthropicProperties.modelName)
                        .bindText(anthropicSettings::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultModelName) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(anthropicProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(anthropicProperties.fromEnv)
                        .bindText(anthropicProperties.modelNameEnv)
                        .bindText(anthropicSettings::modelNameEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultModelNameEnv) }
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiEndpoint")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(anthropicProperties.apiEndpoint)
                        .bindText(anthropicSettings::apiEndpoint.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(anthropicProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(anthropicProperties.fromEnv)
                        .bindText(anthropicProperties.apiEndpointEnv)
                        .bindText(anthropicSettings::apiEndpointEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiEndpointEnv) }
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.anthropic.apiKey")).widthGroup(group)
                    passwordField().columns(COLUMNS_MEDIUM)
                        .bindText(anthropicProperties.apiKey)
                        .bindText(anthropicSettings::apiKey.toNonNullableProperty(""))
                        .validationOnInput { validateApiKey(this, it, anthropicProperties.fromEnv.get()) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(anthropicProperties.fromEnv)
                    passwordField().columns(COLUMNS_SHORT).visibleIf(anthropicProperties.fromEnv)
                        .bindText(anthropicProperties.apiKeyEnv)
                        .bindText(anthropicSettings::apiKeyEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiKeyEnv) }
                }
                row {
                    button(PlsBundle.message("settings.ai.test")) {
                        // 注意这里需要基于可能尚未保存的配置项进行测试
                        val options = AnthropicChatModelProvider.Options.fromProperties(anthropicProperties.toProperties())
                        val r = AnthropicChatModelProvider.INSTANCE.checkStatus(options)
                        showTestMessage(r)
                    }
                    checkBox(PlsBundle.message("settings.ai.fromEnv"))
                        .bindSelected(anthropicProperties.fromEnv)
                        .bindSelected(anthropicSettings::fromEnv)
                }
            }

            //local (Ollama)
            collapsibleGroup(PlsBundle.message("settings.ai.local")) {
                val group = "pls.ai.local"
                val localProperties = LocalChatModelProvider.Options.AtomicProperties()
                val localSettings = settings.local

                //modelName
                row {
                    label(PlsBundle.message("settings.ai.local.modelName")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(localProperties.modelName)
                        .bindText(localSettings::modelName.toNonNullableProperty(""))
                        .validationOnInput { validateModelName(this, it, localProperties.fromEnv.get()) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(localProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(localProperties.fromEnv)
                        .bindText(localProperties.modelNameEnv)
                        .bindText(localSettings::modelNameEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultModelNameEnv) }
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.local.apiEndpoint")).widthGroup(group)
                    textField().columns(COLUMNS_MEDIUM)
                        .bindText(localProperties.apiEndpoint)
                        .bindText(localSettings::apiEndpoint.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultApiEndpoint) }

                    label(PlsBundle.message("settings.ai.env")).visibleIf(localProperties.fromEnv)
                    textField().columns(COLUMNS_SHORT).visibleIf(localProperties.fromEnv)
                        .bindText(localProperties.apiEndpointEnv)
                        .bindText(localSettings::apiEndpointEnv.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultApiEndpointEnv) }
                }
                row {
                    button(PlsBundle.message("settings.ai.test")) {
                        // 注意这里需要基于可能尚未保存的配置项进行测试
                        val options = LocalChatModelProvider.Options.fromProperties(localProperties.toProperties())
                        val r = LocalChatModelProvider.INSTANCE.checkStatus(options)
                        showTestMessage(r)
                    }
                    checkBox(PlsBundle.message("settings.ai.fromEnv"))
                        .bindSelected(localProperties.fromEnv)
                        .bindSelected(localSettings::fromEnv)
                }
            }
        }
    }

    private fun validateModelName(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
        if (!skip && field.text.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingModelName"))
        return null
    }

    // private fun validateApiEndpoint(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
    //     if (!skip && field.text.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingApiEndpoint"))
    //     return null
    // }

    private fun validateApiKey(builder: ValidationInfoBuilder, field: JBPasswordField, skip: Boolean): ValidationInfo? {
        // 目前仅在输入时验证，不在应用时验证
        // 如果启用 AI 集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (!skip && field.password.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingApiKey"))
        return null
    }

    private fun showTestMessage(statusResult: ChatModelProvider.StatusResult) {
        if (statusResult.status) {
            Messages.showInfoMessage(statusResult.message, statusResult.title)
        } else {
            Messages.showWarningDialog(statusResult.message, statusResult.title)
        }
    }
}
